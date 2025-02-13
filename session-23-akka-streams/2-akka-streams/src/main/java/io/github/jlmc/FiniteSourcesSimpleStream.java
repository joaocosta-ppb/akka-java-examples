package io.github.jlmc;

import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * <pre>
 *  ---------     -------                  --------
 * | Source | => | Flow |=> (3),(2),(1) => | Skin |
 * ---------     -------                   -------
 * </pre>
 */
public class FiniteSourcesSimpleStream {
    public static void main(String[] args) {
        // Generate a source, it takes two data types,
        //  1. the first (Integer) it is the input type
        //  2. The materialize value type
        //Source<Integer, NotUsed> source = Source.range(1, 10);
        //Source<Integer, NotUsed> source = Source.single(17); // useful for example for test proposes

        List<String> names = List.of(
                "Iron Man",
                "Spider-Man",
                "Captain America",
                "Thor",
                "Hulk",
                "Black Widow",
                "Doctor Strange",
                "Black Panther",
                "Scarlet Witch",
                "Ant-Man"
        );
        Source<String, NotUsed> source = Source.from(names);

        //
        Flow<String, String, NotUsed> flow =
                Flow.of(String.class)
                        .map(incomingValue -> "The next value is " + incomingValue) // convert the integer;
        ;

        // Create a sink, Define what to do with the data
        Sink<String, CompletionStage<Done>> sink =
                Sink.foreach(value -> {
                    System.out.println("==> " + value);
                });

        // putting all together, to create the graph

        RunnableGraph<NotUsed> graph =
                source.via(flow)
                        //.via(another- flow)
                        .to(sink);


        // create the akka actor
        ActorSystem<Object> system = ActorSystem.create(Behaviors.empty(), "Simple-Stream");

        // execute the graph in akka actor
        graph.run(system);
    }
}
