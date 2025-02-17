package io.github.jlmc;

import akka.NotUsed;
import akka.Done;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ThreadLocalRandom;

public class TakeAndLimitStreamApp {
    public static void main(String[] args) {
        ActorSystem<?> actorSystem = ActorSystem.create(Behaviors.empty(),"actorSystem");

        Source<Integer, NotUsed> source = Source.repeat(1)
                .map(x -> ThreadLocalRandom.current().nextInt(1000) +1 );

        Flow<Integer, Integer, NotUsed> greaterThan200Filter =
                Flow.of(Integer.class)
                        .filter ( x -> x > 200);
        Flow<Integer, Integer, NotUsed> evenNumberFilter =
                Flow.of(Integer.class)
                        .filter( x -> x % 2 == 0);

        Sink<Integer, CompletionStage<Done>> sink = Sink.foreach (System.out::println);

        Sink<Integer, CompletionStage<Integer>> sinkWithCounter = Sink
                .fold( 0 , (counter, value) -> {
                    System.out.println(value);
                    return counter + 1;
                });

        Sink<Integer, CompletionStage<Integer>> sinkWithSum = Sink
                .reduce( (firstValue, secondValue) -> {
                    System.out.println(secondValue);
                    return firstValue + secondValue;
                });

        CompletionStage<Integer> result = source
                .take(100)
               // .throttle(1, Duration.ofSeconds(1))
                //.takeWithin(Duration.ofSeconds(5))
                .via(greaterThan200Filter.limit(110)) // if there are more than the defined limited value an exception will be throws
                .viaMat(evenNumberFilter.takeWhile( value -> value < 900) , Keep.right())
                .toMat(sinkWithSum, Keep.right())
                .run(actorSystem);

        result.whenComplete( (value, throwable) -> {
            if (throwable == null) {
                System.out.println("The graph's materialized value is " + value);
            }
            else {
                System.out.println("Something went wrong " + throwable);
            }
            actorSystem.terminate();
        });

//        CompletionStage<Done> result2 = source.toMat(sink, Keep.right()).run(actorSystem);
//
//        result2.whenComplete( (value, throwable) -> {
//            //actorSystem.terminate();
//        });


    }
}
