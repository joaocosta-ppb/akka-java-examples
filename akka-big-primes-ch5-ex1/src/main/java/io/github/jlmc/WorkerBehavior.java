package io.github.jlmc;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.concurrent.ThreadLocalRandom;

public class WorkerBehavior extends AbstractBehavior<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerBehavior.class);

    private WorkerBehavior(ActorContext<String> context) {
        super(context);
    }

    public static Behavior<String> createWorkerBehavior() {
        LOGGER.info("Creating worker behavior");
        return Behaviors.setup(WorkerBehavior::new);
    }

    @Override
    public Receive<String> createReceive() {
        LOGGER.info("Creating worker receive");
        return newReceiveBuilder()
                .onMessageEquals(Messages.START, this::startHandler)
                .onAnyMessage(this::fullbackHandler)
                .build();
    }

    private Behavior<String> startHandler() {
        LOGGER.info("Received start request");

        BigInteger bigInteger = new BigInteger(2000, ThreadLocalRandom.current());
        System.out.println(bigInteger.nextProbablePrime());

        return this;
    }

    private Behavior<String> fullbackHandler(String s) {
        LOGGER.debug("Fullback handler: {} ", s);
        return this;
    }
}
