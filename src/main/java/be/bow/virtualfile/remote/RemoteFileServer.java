package be.bow.virtualfile.remote;

import be.bow.application.BaseServer;
import be.bow.application.annotations.BowComponent;
import be.bow.db.application.environment.RemoteCountDBEnvironmentProperties;
import be.bow.ui.UI;
import be.bow.util.WrappedSocketConnection;
import be.bow.virtualfile.VirtualFile;
import be.bow.virtualfile.VirtualFileService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@BowComponent
public class RemoteFileServer extends BaseServer {

    private VirtualFileService virtualFileService;

    @Autowired
    public RemoteFileServer(VirtualFileService virtualFileService, RemoteCountDBEnvironmentProperties properties) {
        super("RemoteFileServer", properties.getVirtualFileServerPort());
        this.virtualFileService = virtualFileService;
    }

    @Override
    protected BaseServer.SocketRequestHandler createSocketRequestHandler(WrappedSocketConnection connection) throws IOException {
        return new SocketRequestHandler(connection);
    }


    private class SocketRequestHandler extends BaseServer.SocketRequestHandler {

        private long totalNumberOfRequests = 0;

        public SocketRequestHandler(WrappedSocketConnection connection) throws IOException {
            super(connection);
        }

        @Override
        protected void reportUnexpectedError(Exception ex) {
            UI.writeError("Exception in socket request handler of remote file server", ex);
        }

        @Override
        public long getTotalNumberOfRequests() {
            return totalNumberOfRequests;
        }

        @Override
        protected void handleRequests() throws Exception {
            //We only handle a single request:
            byte actionAsByte = connection.readByte();
            try {
                Action action = Action.values()[actionAsByte];
                if (action == Action.INPUT_STREAM) {
                    String relPath = connection.readString();
                    VirtualFile file = virtualFileService.getRootDirectory().getFile(relPath);
                    if (file.exists()) {
                        InputStream is = file.createInputStream();
                        connection.writeLong(LONG_OK);
                        IOUtils.copy(is, connection.getOs());
                        connection.flush();
                    } else {
                        connection.writeLong(LONG_ERROR);
                        connection.writeString("Could not find file " + relPath);
                        connection.flush();
                    }
                } else if (action == Action.OUTPUT_STREAM) {
                    String relPath = connection.readString();
                    VirtualFile file = virtualFileService.getRootDirectory().getFile(relPath);
                    OutputStream os = file.createOutputStream();
                    connection.writeLong(LONG_OK);
                    connection.flush();
                    IOUtils.copy(connection.getIs(), os);
                } else if (action == Action.EXISTS) {
                    String relPath = connection.readString();
                    VirtualFile file = virtualFileService.getRootDirectory().getFile(relPath);
                    connection.writeBoolean(file.exists());
                    connection.flush();
                } else {
                    connection.writeLong(LONG_ERROR);
                    connection.writeString("Unknown command " + action);
                    connection.flush();
                }
            } catch (Exception exp) {
                //try to send error message to client (only works if the client happens to be checking for LONG_ERROR
                connection.writeLong(LONG_ERROR);
                connection.writeString(exp.getMessage());
                connection.flush();
                throw exp; //throw to caller method to close this connection
            }
        }
    }

    public static enum Action {
        INPUT_STREAM, OUTPUT_STREAM, EXISTS
    }
}
