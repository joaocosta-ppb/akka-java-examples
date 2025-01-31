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

    //private BigInteger calculatedNumber;

    /**
     * The command must implement serializable, for cluster reasons.
     */
    public interface Command extends Serializable {
    }

    public record CalculatePrimeCommand(ActorRef<ManagerBehavior.Command> sender) implements Command {
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
        return commandReceiveWhenThereIsNoCalculatedPrimeNumber();
    }

    private Receive<Command> commandReceiveWhenThereIsNoCalculatedPrimeNumber() {
        return newReceiveBuilder()
                .onMessage(CalculatePrimeCommand.class, command -> {
                    BigInteger  randomNumber = new BigInteger(2000, ThreadLocalRandom.current());
                    BigInteger prime = randomNumber.nextProbablePrime();

                    if (ThreadLocalRandom.current().nextBoolean()) {
                        command.sender().tell(new ManagerBehavior.ResultCommand(prime, getContext().getSelf().path().toString()));
                    } else {
                        getContext().getLog().warn("worker '{}' not communicate top level the Prime number: {}!", getContext().getSelf().path(), prime);
                    }


                    return createReceiveWhenAlreadyHaveCalculatedPrimeNumber(prime);
                })
                .onAnyMessage(this::fullbackHandler)
                .build();
    }

    private Receive<Command> createReceiveWhenAlreadyHaveCalculatedPrimeNumber(final BigInteger number) {
        return newReceiveBuilder()
                .onMessage(CalculatePrimeCommand.class, command -> {
                    String path = getContext().getSelf().path().toString();
                    command.sender().tell(new ManagerBehavior.ResultCommand(number, path));
                    return Behaviors.same();
                })
                .build();
    }

    private Behavior<Command> fullbackHandler(Command s) {
        LOGGER.debug("Fullback handler: {} ", s);
        return this;
    }
}
