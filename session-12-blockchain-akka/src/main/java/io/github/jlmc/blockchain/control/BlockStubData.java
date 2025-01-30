package io.github.jlmc.blockchain.control;

import io.github.jlmc.blockchain.entities.Block;
import io.github.jlmc.blockchain.entities.Transaction;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class BlockStubData {

    private static final long[] TIMESTAMPS = {
            LocalDateTime.of(2015, 6, 22, 14, 21).toInstant(ZoneOffset.UTC).toEpochMilli(),
            LocalDateTime.of(2015, 6, 22, 14, 27).toInstant(ZoneOffset.UTC).toEpochMilli(),
            LocalDateTime.of(2015, 6, 22, 14, 29).toInstant(ZoneOffset.UTC).toEpochMilli(),
            LocalDateTime.of(2015, 6, 22, 14, 33).toInstant(ZoneOffset.UTC).toEpochMilli(),
            LocalDateTime.of(2015, 6, 22, 14, 38).toInstant(ZoneOffset.UTC).toEpochMilli(),
            LocalDateTime.of(2015, 6, 22, 14, 41).toInstant(ZoneOffset.UTC).toEpochMilli(),
            LocalDateTime.of(2015, 6, 22, 14, 46).toInstant(ZoneOffset.UTC).toEpochMilli(),
            LocalDateTime.of(2015, 6, 22, 14, 47).toInstant(ZoneOffset.UTC).toEpochMilli(),
            LocalDateTime.of(2015, 6, 22, 14, 51).toInstant(ZoneOffset.UTC).toEpochMilli(),
            LocalDateTime.of(2015, 6, 22, 14, 55).toInstant(ZoneOffset.UTC).toEpochMilli()
    };

    private static final int[] CUSTOMER_IDS = {1732, 1650, 2209, 4545, 324, 1944, 6565, 1805, 1765, 7001};
    private static final double[] AMOUNTS = {103.27, 66.54, -21.09, 44.65, 177.99, 189.02, 17.00, 32.99, 60.00, -10.00};

    public static Block getNextBlock(int id, String lastHash) {
        Transaction transaction = new Transaction(id, TIMESTAMPS[id], CUSTOMER_IDS[id], AMOUNTS[id]);

        return new Block(transaction, lastHash);
    }

}
