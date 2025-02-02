package io.github.jlmc.blockchain.control;

import io.github.jlmc.blockchain.entities.Block;
import io.github.jlmc.blockchain.entities.HashResult;
import io.github.jlmc.blockchain.entities.Transaction;
import io.github.jlmc.blockchain.hash.MineCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

class MineCalculatorTest {

    @Test
    void calculate() {
        final long timestamp = LocalDateTime.of(2015, 6, 22, 14, 21).toInstant(ZoneOffset.UTC).toEpochMilli();
        final int transactionId = 0;
        final int customerNumber = 1732;
        final double amount = 103.27D;
        final String lastHash = "0";
        final int difficultyLevel = 5;

        Transaction transaction = new Transaction(transactionId,
                timestamp, customerNumber, amount);

        var block =  new Block(transaction, lastHash);

        HashResult hashResult = MineCalculator.mineBlock(block, difficultyLevel, 0, 100000000);
        Block hashBlock = block.withHashResult(hashResult);

        Assertions.assertEquals("00000f05ab26d5815d2a70f94510de2ef5a11db426a1a67d38e459e38008eb59", hashBlock.getHash());
        Assertions.assertEquals(lastHash, hashBlock.getPreviousHash());
        Assertions.assertEquals(14503, hashBlock.getNonce());
        Assertions.assertSame(transaction, hashBlock.getTransaction());
        Assertions.assertTrue(MineCalculator.validateBlock(hashBlock));
    }
}