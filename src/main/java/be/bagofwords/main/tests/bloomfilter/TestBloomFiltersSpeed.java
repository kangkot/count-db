package be.bagofwords.main.tests.bloomfilter;

import be.bagofwords.db.bloomfilter.LongBloomFilter;
import be.bagofwords.db.data.LongCountsBloomFilter;
import be.bagofwords.ui.UI;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;

public class TestBloomFiltersSpeed {

    private static final int NUM_OF_VALUES = 1000000;

    public static void main(String[] args) {
        LongBloomFilter bloomFilter1 = new LongBloomFilter(NUM_OF_VALUES, 0.001);

        BloomFilter<Long> bloomFilter2 = BloomFilter.create((from, into) -> into.putLong(from), NUM_OF_VALUES, 0.001);

        LongCountsBloomFilter bloomFilter3 = new LongCountsBloomFilter(NUM_OF_VALUES, 0.001);
        UI.write("Writing values for filter 1 took " + putValues(bloomFilter1));
        UI.write("Writing values for filter 2 took " + putValues(bloomFilter2));
        UI.write("Writing values for filter 3 took " + putValues(bloomFilter3));

        UI.write("Reading values for filter 1 took " + readValues(bloomFilter1));
        UI.write("Reading values for filter 2 took " + readValues(bloomFilter2));
        UI.write("Reading values for filter 3 took " + readValues(bloomFilter3));
    }

    private static long readValues(LongBloomFilter bloomFilter1) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < NUM_OF_VALUES; i++) {
            if (i % 3 == 0) {
                bloomFilter1.mightContain(i);
            }
        }
        return System.currentTimeMillis() - start;
    }

    private static long readValues(BloomFilter<Long> bloomFilter2) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < NUM_OF_VALUES; i++) {
            if (i % 3 == 0) {
                bloomFilter2.mightContain((long) i);
            }
        }
        return System.currentTimeMillis() - start;
    }

    private static long readValues(LongCountsBloomFilter bloomFilter3) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < NUM_OF_VALUES; i++) {
            if (i % 3 == 0) {
                bloomFilter3.getMaxCount(i);
            }
        }
        return System.currentTimeMillis() - start;
    }

    private static long putValues(LongCountsBloomFilter bloomFilter3) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < NUM_OF_VALUES; i++) {
            if (i % 3 == 0) {
                bloomFilter3.addCount((long) i, 12);
            }
        }
        return System.currentTimeMillis() - start;
    }

    private static long putValues(BloomFilter<Long> bloomFilter2) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < NUM_OF_VALUES; i++) {
            if (i % 3 == 0) {
                bloomFilter2.put((long) i);
            }
        }
        return System.currentTimeMillis() - start;
    }

    private static long putValues(LongBloomFilter bloomFilter1) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < NUM_OF_VALUES; i++) {
            if (i % 3 == 0) {
                bloomFilter1.put(i);
            }
        }
        return System.currentTimeMillis() - start;
    }

}
