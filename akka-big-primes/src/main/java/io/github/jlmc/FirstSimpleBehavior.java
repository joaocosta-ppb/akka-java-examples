package io.github.jlmc;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class FirstSimpleBehavior extends AbstractBehavior<String> {

    private FirstSimpleBehavior(ActorContext<String> context) {
        super(context);
    }

    public static Behavior<String> createFirstSimpleBehavior() {
        return Behaviors.setup(FirstSimpleBehavior::new);
    }


    @Override
    public Receive<String> createReceive() {
        // 1. receive the message and response
        var m =
                newReceiveBuilder()
                        .onAnyMessage(message -> processText(message))
                        .build();

        return m;
    }

    private Behavior<String> processText(String text) {
        System.out.println("I receive the Text: " + text);

        return this;
    }
}
