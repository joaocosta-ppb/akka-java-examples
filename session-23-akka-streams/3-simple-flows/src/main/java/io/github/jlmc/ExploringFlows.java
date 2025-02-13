package io.github.jlmc;

import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.time.Duration;
import java.util.concurrent.CompletionStage;


/**
 * <pre>
 *  ---------     -------                  --------
 * | Source | => | Flow |=> (3),(2),(1) => | Skin |
 * ---------     -------                   -------
 * </pre>
 */
public class ExploringFlows {
    public static void main(String[] args) {
        ActorSystem actorSystem = ActorSystem.create(Behaviors.empty(), "ExploringFlows");

        Source<Integer, NotUsed> numbersSource = Source.range(1, 1000); //.throttle(1, Duration.ofSeconds(1));

        Sink<String, CompletionStage<Done>> printSkin = Sink.foreach(System.out::println);


        Flow<Integer, String, NotUsed> flow =
                Flow.of(Integer.class)
                        .filter(value -> value % 17 == 0)
                        .map(incomingValue -> "The next value is " + incomingValue) // convert the integer;
        ;


        numbersSource.via(flow).to(printSkin).run(actorSystem);
    }
}
