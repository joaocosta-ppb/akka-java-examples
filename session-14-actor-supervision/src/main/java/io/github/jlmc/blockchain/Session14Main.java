package io.github.jlmc.blockchain;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import io.github.jlmc.blockchain.akka.controller.ManagerBehavior;
import io.github.jlmc.blockchain.entities.Block;
import io.github.jlmc.blockchain.entities.Transaction;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.CompletionStage;

public class Session14Main {

    public static void main(String[] args) {

        Block block = getBlock();

        ActorSystem<ManagerBehavior.Command> manager = ActorSystem.create(ManagerBehavior.create(), "BlockchainManager");

        CompletionStage<Map<String, Object>> completed =
                AskPattern.ask(
                        manager,
                        me -> new ManagerBehavior.MineBlockCommand(me, block, 5, 5),
                        Duration.ofMinutes(30),
                        manager.scheduler()
                );

        completed.whenComplete((response, throwable) -> {
            if (response != null) {
                System.out.println("Finished processing.");
                System.out.println(response);
            } else {
                System.out.println("System didn't respond in time");
            }
            manager.terminate();
        });

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
