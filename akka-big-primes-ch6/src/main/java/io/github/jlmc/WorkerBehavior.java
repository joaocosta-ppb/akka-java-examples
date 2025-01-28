package io.github.jlmc;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.concurrent.ThreadLocalRandom;

public class WorkerBehavior extends AbstractBehavior<WorkerBehavior.Command> {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerBehavior.class);

    private BigInteger calculatedNumber;

    /**
     * The command must implement serializable, for cluster reasons.
     */
    public record Command(String message, ActorRef<ManagerBehavior.Command> sender) implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    private WorkerBehavior(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> createWorkerBehavior() {
        LOGGER.info("Creating worker behavior");
        return Behaviors.setup(WorkerBehavior::new);
    }

    @Override
    public Receive<Command> createReceive() {
        LOGGER.info("Creating worker receive");
        String path = getContext().getSelf().path().toString();
        return newReceiveBuilder()
                .onAnyMessage(command -> {
                    if (Messages.START.equals(command.message)) {
                        if (calculatedNumber == null) {
                            calculatedNumber = new BigInteger(2000, ThreadLocalRandom.current());
                            //System.out.println(bigInteger.nextProbablePrime());

                        }
                        command.sender().tell(new ManagerBehavior.ResultCommand(calculatedNumber, path));
                    }

                    return this;
                })
                //.onMessageEquals(Messages.START, this::startHandler)
                .onAnyMessage(this::fullbackHandler)
                .build();
    }


    private Behavior<Command> fullbackHandler(Command s) {
        LOGGER.debug("Fullback handler: {} ", s);
        return this;
    }
}
