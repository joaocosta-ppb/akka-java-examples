package io.github.jlmc;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;

public class Main {
    public static void main(String[] args) {
        Behavior<String> firstSimpleBehavior = FirstSimpleBehavior.createFirstSimpleBehavior();

        ActorSystem<String> actorSystem = ActorSystem.create(firstSimpleBehavior, "First-Actor-System");

        actorSystem.tell("Hello are you there?");
        actorSystem.tell("This is the second message.");
    }
}
