package io.github.jlmc;

import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ThreadLocalRandom;

public class MaterializedValuesApp {

    private static final int TOTAL_OF_ELEMENTS = 1000;

    public static void main(String[] args) {
        ActorSystem<?> actorSystem = ActorSystem.create(Behaviors.empty(), "actorSystem");

        //Materializer materializer = Materializer.createMaterializer(actorSystem);


        Source<Lottery, NotUsed> rangeSource =
                Source.range(0, TOTAL_OF_ELEMENTS)
                        .map(it -> new Lottery(it, generateRandomNumber()));


        Flow<Lottery, Lottery, NotUsed> greaterThan200Filter = Flow.of(Lottery.class)
                .filter(it -> it.id() > 200);

        Flow<Lottery, Lottery, NotUsed> isNumberEvenFilter = Flow.of(Lottery.class)
                .filter(it -> it.number() % 2 == 0);


        Sink<Lottery, CompletionStage<Done>> sink =
                Sink.foreach(System.out::println);

        Sink<Lottery, CompletionStage<Integer>> sinkWithCounter =
                Sink.fold(0, (count, lottery) -> count + lottery.number());

        // alternatively

        var s = Sink.reduce((Lottery a, Lottery b) -> a.sum(b));

        CompletionStage<Lottery> result =
                rangeSource
                        .via(greaterThan200Filter)
                        .via(isNumberEvenFilter)
                        //.runWith(sinkWithCounter, materializer);
                        //.toMat(sinkWithCounter, Keep.right())
                        .toMat(s, Keep.right())
                        .run(actorSystem);

        //result.toCompletableFuture().get();

        result.whenComplete((resultValue, throwable) -> {
            if (throwable != null) {
                System.out.println("Error: " + throwable.getMessage());
            } else {
                System.out.println("Result: " + resultValue);
            }

            actorSystem.terminate();
        });

        /*
        CompletionStage<Done> result2 =
                rangeSource
                        .toMat(Sink.ignore(), Keep.right())
                        .run(actorSystem);
        result2.whenComplete((value, throwable) -> {
            actorSystem.terminate();
        });
         */

    }

    private static int generateRandomNumber() {
        return ThreadLocalRandom.current().nextInt(1, 1000) + 1;
    }

    record Lottery(int id, int number) {

        Lottery sum(Lottery other) {
            return new Lottery(other.id + id, other.number + number);
        }
    }

}
