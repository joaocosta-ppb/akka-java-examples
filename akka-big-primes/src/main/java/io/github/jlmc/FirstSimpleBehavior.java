package io.github.jlmc;

import akka.actor.typed.ActorRef;
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
        return newReceiveBuilder()
                .onMessageEquals("Ping", this::pingHandler)
                .onMessageEquals("create-child", this::createNewChildHandler)
                .onAnyMessage(this::processText)
                .build();
    }

    private Behavior<String> createNewChildHandler() {


        ActorContext<String> context = super.getContext();

        System.out.println("My path is " + getContext().getSelf().path() + " I going to create a child");


        Behavior<String> child = createFirstSimpleBehavior();
        ActorRef<String> child1 = context.spawn(child, "child-1");

        child1.tell("Who are you?");

        return this;
    }

    private Behavior<String> pingHandler() {
        System.out.println("My path is " + getContext().getSelf().path() + " => ACK, Yes I'm here...");
        return this;
    }

    private Behavior<String> processText(String text) {
        System.out.println("My path is " + getContext().getSelf().path() + " and I received the text: " + text);

        return this;
    }
}
