package io.github.jlmc;

import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletionStage;


/**
 * <pre>
 *  ---------     -------                  --------
 * | Source | => | Flow |=> (3),(2),(1) => | Skin |
 * ---------     -------                   -------
 * </pre>
 */
public class ExploringFlowsGeneric {
    public static void main(String[] args) {
        ActorSystem actorSystem = ActorSystem.create(Behaviors.empty(), "ExploringFlows");

        Source<Integer, NotUsed> numbersSource = Source.range(1, 1000); //.throttle(1, Duration.ofSeconds(1));


        Flow<Integer, String, NotUsed> flow =
                Flow.of(Integer.class)
                        .filter(value -> value % 17 == 0)
                        .mapConcat(param -> List.of(param, param + 1, param + 2))
                        .map(incomingValue -> "" + incomingValue) // convert the integer;
        ;

        //Flow<String, Members, NotUsed> grouped = Flow.of(String.class).grouped(3).map(list -> new Members(list));
        Flow<String, List<String>, NotUsed> grouped = Flow.of(String.class).grouped(3);
        //Flow<List, Integer, NotUsed> flow1 = Flow.of(List.class).mapConcat(items -> items);
        Flow<String, Integer, NotUsed> stringIntegerNotUsedFlow = grouped.mapConcat(value -> value.stream().map(Integer::valueOf).sorted(Comparator.reverseOrder()).toList());

        Sink<Integer, CompletionStage<Done>> sink = Sink.foreach(System.out::println);

        numbersSource.via(flow).via(stringIntegerNotUsedFlow).to(sink).run(actorSystem);
    }
}
