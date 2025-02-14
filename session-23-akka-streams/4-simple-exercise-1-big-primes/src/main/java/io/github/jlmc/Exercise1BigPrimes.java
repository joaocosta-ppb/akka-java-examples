package io.github.jlmc;

import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ThreadLocalRandom;

/**
 * # Exercise 1
 * 1. Source - emit 10 elements.
 * 2. Convert element to a random BigInteger `new BigInteger(2000, new Random())`.
 * 3. Find the next probable prime + print it
 * 4. Group the primes into a List
 *
 * <pre>
 * --------            -----------------------              -------------------------                ----------------              --------------
 * \ Source \ => () => | Flow (to BigInteger) | => (BI) => | Flow (to prime + print) | => (prime) => \ Flow to List | => (list) => \ Flow (sort) | => (list) => Sink
 * ----------          -----------------------             ---------------------------               ----------------               --------------
 * </pre>
 *
 */
public class Exercise1BigPrimes {

    public static final int TOTAL_OF_ELEMENTS = 10;


    public static void main(String[] args) {

        Source<Integer, NotUsed> mySource = Source.range(1, TOTAL_OF_ELEMENTS);

        // flow 1 - Flow (to BigInteger)
        Flow<Integer, BigInteger, NotUsed> flow1ConvertToBigDecimal = Flow.of(Integer.class)
                .map(i -> new BigInteger(2000, ThreadLocalRandom.current()));

        // flow 2 - Flow (to prime + print)
        Flow<BigInteger, BigInteger, NotUsed> flow2FindNextPrime = Flow.of(BigInteger.class)
                .map(bi -> {
                    BigInteger prime = bi.nextProbablePrime();
                    System.out.printf("next prime of %d is %s%n", bi, prime);

                    return prime;
                });

        // flow 3 - Flow to List
        Flow<BigInteger, List<BigInteger>, NotUsed> flow3CollectAllPrimes = Flow.of(BigInteger.class)
                .grouped(TOTAL_OF_ELEMENTS)
                .map(list -> {
                    System.out.println("sorting all primes in each group");
                    return list.stream().sorted(BigInteger::compareTo).toList();
                });


        //flow1ConvertToBigDecimal.via(flow2FindNextPrime).via(flow3CollectAllPrimes)
        Sink<List<BigInteger>, CompletionStage<Done>> sink = Sink.foreach(System.out::println);

        ActorSystem<?> actorSystem = ActorSystem.create(Behaviors.empty(), "actorSystem");
        mySource.via(flow1ConvertToBigDecimal)
                .via(flow2FindNextPrime)
                .via(flow3CollectAllPrimes)
                .to(sink)
                .run(actorSystem);
    }

}
