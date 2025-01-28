package io.github.jlmc;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.IntStream;

public class ManagerBehavior extends AbstractBehavior<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagerBehavior.class);
    private final int numberOfWorkers;

    private ManagerBehavior(ActorContext<String> context, int numberOfWorkers) {
        super(context);
        this.numberOfWorkers = numberOfWorkers;
    }

    public static Behavior<String> createNewInstance(int numberOfWorkers) {
        LOGGER.info("Creating new ManagerBehavior, {} workers", numberOfWorkers);
        return Behaviors.setup((c) -> new ManagerBehavior(c, numberOfWorkers));
    }


    @Override
    public Receive<String> createReceive() {
        LOGGER.info("Creating ManagerBehavior Received");
        return newReceiveBuilder()
                .onMessageEquals(Messages.START, this::startHandler)
                .onAnyMessage(this::fullbackHandler)
                .build();
    }

    private Behavior<String> startHandler() {
        LOGGER.info("Starting ManagerBehavior");

        ActorContext<String> context = getContext();
        IntStream.range(0, numberOfWorkers)
                .mapToObj(i -> {

                    LOGGER.info("Instantiate worker #{}", i);
                    Behavior<String> child = WorkerBehavior.createWorkerBehavior();

                    return context.spawn(child, WorkerBehavior.class.getSimpleName() + "-" + i);
                })
                .forEach(actorRef -> {
                    String start = Messages.START;
                    LOGGER.info("Sending {} to worker #{}", start,  actorRef.path());
                    actorRef.tell(start);
                });

        return this;
    }

    private Behavior<String> fullbackHandler(String s) {
        LOGGER.debug("Fullback handler: {} ", s);
        return this;
    }
}
