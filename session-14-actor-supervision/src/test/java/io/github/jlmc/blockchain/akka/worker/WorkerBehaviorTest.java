package io.github.jlmc.blockchain.akka.worker;

import akka.actor.testkit.typed.CapturedLogEvent;
import akka.actor.testkit.typed.javadsl.BehaviorTestKit;
import akka.actor.testkit.typed.javadsl.TestInbox;
import akka.actor.typed.Behavior;
import io.github.jlmc.blockchain.entities.Block;
import io.github.jlmc.blockchain.entities.HashResult;
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

        TestInbox<BlockHashedCommand> testInbox = TestInbox.create();

        BehaviorTestKit<WorkerBehavior.Command> sut = BehaviorTestKit.create(WorkerBehavior.create());
        WorkerBehavior.StartMiningCommand startMiningCommand = new WorkerBehavior.StartMiningCommand(block, 0, 5, 100, testInbox.getRef());

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

        TestInbox<BlockHashedCommand> testInbox = TestInbox.create();

        BehaviorTestKit<WorkerBehavior.Command> sut = BehaviorTestKit.create(WorkerBehavior.create());
        WorkerBehavior.StartMiningCommand startMiningCommand = new WorkerBehavior.StartMiningCommand(block, (741343 - 999), 5, 1_000_000, testInbox.getRef());

        sut.run(startMiningCommand);

        List<CapturedLogEvent> allLogEntries = sut.getAllLogEntries();
        assertEquals(2, allLogEntries.size());
        CapturedLogEvent capturedLogEvent = allLogEntries.getLast();
        Assertions.assertEquals("Block Hashed with nonce: 741343 and hash 00000707e3b716f5c4fd06a8111aadd30971bd874f4754f49f11471083632196", capturedLogEvent.message());
        Assertions.assertEquals(Level.DEBUG, capturedLogEvent.level());
    }

    @Test
    void messageReceiveIfNonceInTheRange() {
        Block block = getBlock();

        TestInbox<BlockHashedCommand> testInbox = TestInbox.create();

        Behavior<WorkerBehavior.Command> initialBehavior = WorkerBehavior.create();
        BehaviorTestKit<WorkerBehavior.Command> sut = BehaviorTestKit.create(initialBehavior);
        WorkerBehavior.StartMiningCommand startMiningCommand = new WorkerBehavior.StartMiningCommand(block, (741343 - 999), 5, 1_000_000, testInbox.getRef());

        sut.run(startMiningCommand);

        List<CapturedLogEvent> allLogEntries = sut.getAllLogEntries();
        assertEquals(2, allLogEntries.size());
        CapturedLogEvent capturedLogEvent = allLogEntries.getLast();
        Assertions.assertEquals("Block Hashed with nonce: 741343 and hash 00000707e3b716f5c4fd06a8111aadd30971bd874f4754f49f11471083632196", capturedLogEvent.message());
        Assertions.assertEquals(Level.DEBUG, capturedLogEvent.level());

        Block expectedBlock = block.withHashResult(HashResult.of("00000707e3b716f5c4fd06a8111aadd30971bd874f4754f49f11471083632196", 741343));
        BlockHashedCommand blockHashedCommand = testInbox.receiveMessage();
        Assertions.assertEquals(expectedBlock, blockHashedCommand.block());
        //testInbox.expectMessage(new BlockHashedCommand(expectedBlock, sut.getRef()));
    }

    @Test
    void noMessageIsReceivedIfNonceOutOfRange() {
        Block block = getBlock();

        TestInbox<BlockHashedCommand> testInbox = TestInbox.create();

        BehaviorTestKit<WorkerBehavior.Command> sut = BehaviorTestKit.create(WorkerBehavior.create());
        WorkerBehavior.StartMiningCommand startMiningCommand = new WorkerBehavior.StartMiningCommand(block, 0, 5, 100, testInbox.getRef());

        sut.run(startMiningCommand);

        List<CapturedLogEvent> allLogEntries = sut.getAllLogEntries();
        assertEquals(2, allLogEntries.size());
        CapturedLogEvent capturedLogEvent = allLogEntries.getLast();
        Assertions.assertTrue(capturedLogEvent.message().contains("Block NOT Hashed"));
        Assertions.assertEquals(Level.DEBUG, capturedLogEvent.level());

        List<BlockHashedCommand> allReceived = testInbox.getAllReceived();
        Assertions.assertTrue(allReceived.isEmpty());
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