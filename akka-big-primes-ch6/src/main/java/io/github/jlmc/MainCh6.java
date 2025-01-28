package io.github.jlmc;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;

/**
 * ## Exercise 1
 * Create a ManagerBehavior class
 * Receive String messages
 * Expect a message called "start"
 * When start is received:
 *      - Spawn 20 instances of WorkerBehavior
 *      - To each worker send the message "start"
 * Create the main method to instantiate the system and send the start message to the ManagerBehavior.
 */
public class MainCh6 {
    public static void main(String[] args) {
        Behavior<String> mb = ManagerBehavior.createNewInstance(20);

        ActorSystem<String> actorSystem = ActorSystem.create(mb, "Manager");

        actorSystem.tell("lets-go");
        actorSystem.tell(Messages.START);
    }
}
