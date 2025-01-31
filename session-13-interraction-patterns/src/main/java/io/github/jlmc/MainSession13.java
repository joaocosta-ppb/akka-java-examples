package io.github.jlmc;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AskPattern;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.IntStream;

public class MainSession13 {
    public static void main(String[] args) {
        Behavior<ManagerBehavior.Command> mb = ManagerBehavior.createNewInstance(20);

        ActorSystem<ManagerBehavior.Command> manager = ActorSystem.create(mb, "Manager");

        CompletionStage<Collection<BigInteger>> completed =
                AskPattern.ask(
                        manager,
                        me -> new ManagerBehavior.InstructionCommand(Messages.START, me),
                        Duration.ofSeconds(30),
                        manager.scheduler()
                );

        completed.whenComplete((response, throwable) -> {
            if (response != null) {
                displayResult(response);
            } else {
                System.out.println("System didn't respond in time");
            }
            manager.terminate();
        });

    }

    private static void displayResult(Collection<BigInteger> numbers) {
        // Find max width for column alignment
        int maxWidth =
                numbers.stream()
                        .map(BigInteger::toString)
                        .mapToInt(String::length)
                        .max()
                        .orElse(10); // Default width if empty

        // Table header
        String header = String.format("| %-3s | %-" + maxWidth + "s |", "#", "Number");
        String separator = "|-----|" + "-".repeat(maxWidth + 2) + "|"; // Dynamic width separator

        System.out.println(header);
        System.out.println(separator);


        List<BigInteger> orderNumbers = List.copyOf(numbers);
        // Print rows using Streams
        IntStream.range(0, orderNumbers.size())
                .mapToObj(i -> String.format("| %-3d | %-" + maxWidth + "s |", i + 1, orderNumbers.get(i)))
                .forEach(System.out::println);
    }
}
