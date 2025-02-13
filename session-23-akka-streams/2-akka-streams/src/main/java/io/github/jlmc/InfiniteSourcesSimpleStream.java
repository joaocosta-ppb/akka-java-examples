package io.github.jlmc;

import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.IntStream;

/**
 * <pre>
 *  ---------     -------                  --------
 * | Source | => | Flow |=> (3),(2),(1) => | Skin |
 * ---------     -------                   -------
 * </pre>
 */
public class InfiniteSourcesSimpleStream {
    public static void main(String[] args) {
        // Generate a source, it takes two data types,
        //  1. the first (Integer) it is the input type
        //  2. The materialize value type
        //Source<Integer, NotUsed> source = Source.range(1, 10);
        //Source<Integer, NotUsed> source = Source.single(17); // useful for example for test proposes

        // source always of the same value
        Source<String, NotUsed> sameSource = Source.repeat( "3.14");

        // source of items of a collection repeatable
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
        Source<String, NotUsed> cycle = Source.cycle(names::iterator);

        //
        Iterator<String> infiniteRange = IntStream.iterate(0, i -> i +1)
                .mapToObj(i -> "Hero => " + i)
                .iterator();
        //Source<String, NotUsed> infiniteRangeSource = Source.fromIterator(() -> infiniteRange);
        Source<String, NotUsed> infiniteRangeSource =
                Source.fromIterator(() -> infiniteRange)
                        .throttle(1, Duration.ofSeconds(5)) // send a element at max 3 seconds
                        .take(5) // stop the at a fixed number elements
        ;

        //
        Source<String, NotUsed> source = infiniteRangeSource;

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
