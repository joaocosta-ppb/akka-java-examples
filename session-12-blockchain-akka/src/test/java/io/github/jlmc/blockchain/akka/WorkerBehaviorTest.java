package io.github.jlmc.blockchain.akka;

import akka.actor.testkit.typed.CapturedLogEvent;
import akka.actor.testkit.typed.javadsl.BehaviorTestKit;
import io.github.jlmc.blockchain.entities.Block;
import io.github.jlmc.blockchain.entities.Transaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkerBehaviorTest {

    @Test
    void miningFailsIfNonceOutOfRange() {
        Block block = getBlock();

        BehaviorTestKit<WorkerBehavior.Command> sut = BehaviorTestKit.create(WorkerBehavior.create());
        WorkerBehavior.StartMiningCommand startMiningCommand = new WorkerBehavior.StartMiningCommand(block, 0, 5);

        sut.run(startMiningCommand);

        List<CapturedLogEvent> allLogEntries = sut.getAllLogEntries();
        assertEquals(2, allLogEntries.size());
        CapturedLogEvent capturedLogEvent = allLogEntries.getLast();
        Assertions.assertTrue(capturedLogEvent.message().contains("Block NOT Hashed"));
        Assertions.assertEquals(Level.DEBUG, capturedLogEvent.level());
    }

    @Test
    void miningPassesIfNonceInTheRange() {
        Block block = getBlock();

        BehaviorTestKit<WorkerBehavior.Command> sut = BehaviorTestKit.create(WorkerBehavior.create());
        WorkerBehavior.StartMiningCommand startMiningCommand = new WorkerBehavior.StartMiningCommand(block, (741343 - 999), 5);

        sut.run(startMiningCommand);

        List<CapturedLogEvent> allLogEntries = sut.getAllLogEntries();
        assertEquals(2, allLogEntries.size());
        CapturedLogEvent capturedLogEvent = allLogEntries.getLast();
        Assertions.assertEquals("Block Hashed with nonce: 741343 and hash 00000707e3b716f5c4fd06a8111aadd30971bd874f4754f49f11471083632196", capturedLogEvent.message());
        Assertions.assertEquals(Level.DEBUG, capturedLogEvent.level());
    }

    private static Block getBlock() {
        final long timestamp = LocalDateTime.of(2015, 6, 22, 14, 21).toInstant(ZoneOffset.UTC).toEpochMilli();
        final int transactionId = 0;
        final int customerNumber = 1732;
        final double amount = 103.27D;
        final String lastHash = "0";

        Transaction transaction = new Transaction(transactionId,
                timestamp, customerNumber, amount);

        return new Block(transaction, lastHash);
    }
}