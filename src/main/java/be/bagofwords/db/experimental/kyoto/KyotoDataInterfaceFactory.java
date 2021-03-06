package be.bagofwords.db.experimental.kyoto;

import be.bagofwords.application.ApplicationContext;
import be.bagofwords.db.CoreDataInterface;
import be.bagofwords.db.DataInterfaceFactory;
import be.bagofwords.db.combinator.Combinator;
import be.bagofwords.util.Utils;

import java.io.File;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 9/17/14.
 */
public class KyotoDataInterfaceFactory extends DataInterfaceFactory {

    private final String directory;

    public KyotoDataInterfaceFactory(ApplicationContext context) {
        super(context);
        this.directory = context.getConfig("data_directory");
        File libFile = findLibFile();
        if (libFile == null) {
            throw new RuntimeException("Could not find libkyotocabinet.so.16");
        }
        System.load(libFile.getAbsolutePath());
        Utils.addLibraryPath(libFile.getParentFile().getAbsolutePath());
    }

    private File findLibFile() {
        File libFile = new File("./lib/kyotocabinet-1.24/linux-x86_64/libkyotocabinet.so.16");
        if (libFile.exists()) {
            return libFile;
        }
        libFile = new File("./count-db/lib/kyotocabinet-1.24/linux-x86_64/libkyotocabinet.so.16");
        if (libFile.exists()) {
            return libFile;
        }
        return null;
    }


    @Override
    public <T extends Object> CoreDataInterface<T> createBaseDataInterface(String nameOfSubset, Class<T> objectClass, Combinator<T> combinator, boolean isTemporaryDataInterface) {
        return new KyotoDataInterface<>(nameOfSubset, directory, objectClass, combinator, isTemporaryDataInterface);
    }
}
