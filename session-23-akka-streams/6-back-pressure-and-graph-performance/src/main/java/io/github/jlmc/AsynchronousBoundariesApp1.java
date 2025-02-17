package io.github.jlmc;

import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ThreadLocalRandom;

public class AsynchronousBoundariesApp1 {


    record NumberNextPrimePair(BigInteger number, BigInteger nextPrimeNumber) {
    }

    public static void main(String[] args) {
        Instant startTime = Instant.now();

        ActorSystem<?> actorSystem = ActorSystem.create(Behaviors.empty(), "actorSystem");

        Source<Integer, NotUsed> source = Source.range(1, 10);

        Flow<Integer, BigInteger, NotUsed> numberGenerator = Flow.of(Integer.class)
                .map(it -> {
                    BigInteger number = new BigInteger(3000, ThreadLocalRandom.current());
                    actorSystem.log().debug("it <{}> flow input generate the number <{}>", it, number);
                    return number;
                });

        Flow<BigInteger, NumberNextPrimePair, NotUsed> primeGenerator =
                Flow.of(BigInteger.class)
                        .map(number -> {

                            // actorSystem.log().debug("number {} resolving next prime number", number);
                            BigInteger nextPrimeNumber = number.nextProbablePrime();
                            actorSystem.log().debug("generated prime number for {} resolved next prime number {}", number, nextPrimeNumber);

                            return new NumberNextPrimePair(number, nextPrimeNumber);
                        });


        var groupResults =
                Flow.of(NumberNextPrimePair.class)
                        .grouped(10)
                        .map(list -> {

                            return list.stream().sorted(Comparator.comparing(NumberNextPrimePair::number)).toList();
                        })
                //.mapConcat(i -> i)

                ;


        Sink<List<NumberNextPrimePair>, CompletionStage<Done>> printSkin = Sink.foreach(System.out::println);


        var resultCompletedPromise =
                source.via(numberGenerator)
                        .async() // Asynchronous boundary
                        .via(primeGenerator)
                        .async() // Asynchronous boundary
                        .via(groupResults)
                        .toMat(printSkin, Keep.right())
                        .run(actorSystem);


        resultCompletedPromise.whenComplete((result, throwable) -> {

            if (throwable != null) {
                System.out.println("Some problem happens: " + throwable.getMessage());

            } else {
                System.out.println("App completed successfully");
            }


            actorSystem.terminate();

            Instant endTime = Instant.now();

            Duration between = Duration.between(startTime, endTime);

            System.out.println("App completed in " + between.toMillis() + " ms");

        });

    }
}
