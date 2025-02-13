package io.github.jlmc;


import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.Arrays;
import java.util.List;

public class CombiningFlows {

    static final List<String> SENTENCES_PT = List.of(
            "Casaco impermeável Mayoral, 98 cm, ideal para chuva.",
            "Vendo casaco infantil Mayoral, ótimo estado!",
            "Blusão corta-vento, tamanho 98 cm, super conservado.",
            "Casaco azul Mayoral, confortável e estiloso.",
            "Jaqueta infantil impermeável, perfeita para frio."
    );

    static final List<String> SENTENCES_EN = List.of(
            "Soccer is the most popular sport in the world.",
            "Running every morning improves endurance and health.",
            "Basketball requires speed, agility, and teamwork.",
            "Swimming is a great full-body workout.",
            "Tennis matches can be both intense and strategic."
    );

    public static void main(String[] args) {
        ActorSystem<Object> actorSystem = ActorSystem.create(Behaviors.empty(), "ExploringFlows");

        Source<String, NotUsed> sourcePT = Source.from(SENTENCES_PT);
        Source<String, NotUsed> sourceEN = Source.from(SENTENCES_EN);

        Source<String, NotUsed> concat = sourcePT.concat(sourceEN);


        Flow<String,  String, NotUsed> flow1 =
                Flow.of(String.class)
                //.map(sentence -> sentence.split(" "));
                .mapConcat(sentence -> Arrays.stream(sentence.split(" ")).toList());


        Source<String, NotUsed> howManyWordsSource = concat.via(flow1);

        howManyWordsSource.to(Sink.foreach(System.out::println)).run(actorSystem);
    }
}
