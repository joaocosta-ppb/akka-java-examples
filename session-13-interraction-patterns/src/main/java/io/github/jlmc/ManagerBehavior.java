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
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class ManagerBehavior extends AbstractBehavior<ManagerBehavior.Command> {

    public interface Command extends Serializable {
    }

    public record InstructionCommand(String message, ActorRef<Collection<BigInteger>> replayTo) implements Command {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    public record ResultCommand(BigInteger number, String workerPath) implements Command {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    record NoResponseReceivedCommand(ActorRef<WorkerBehavior.Command> worker) implements Command {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagerBehavior.class);
    private final int numberOfWorkers;

    private final SortedSet<BigInteger> numbers = new TreeSet<>();

    private ActorRef<Collection<BigInteger>> replayTo;

    private ManagerBehavior(ActorContext<Command> context, int numberOfWorkers) {
        super(context);
        this.numberOfWorkers = numberOfWorkers;
    }

    public static Behavior<Command> createNewInstance(int numberOfWorkers) {
        LOGGER.info("Creating new ManagerBehavior, {} workers", numberOfWorkers);
        return Behaviors.setup((c) -> new ManagerBehavior(c, numberOfWorkers));
    }


    @Override
    public Receive<Command> createReceive() {
        LOGGER.info("Creating ManagerBehavior Received");
        return newReceiveBuilder()
                .onMessage(InstructionCommand.class, command -> {
                    if (Messages.START.equals(command.message())) {
                        startHandler(command.replayTo());
                    } else {
                        LOGGER.info("Ignoring message {}", command.message());
                    }

                    return Behaviors.same();
                })
                .onMessage(ResultCommand.class, command -> {
                    addNumber(command.workerPath(), command.number());
                    return Behaviors.same();
                })
                .onMessage(NoResponseReceivedCommand.class, command -> {
                    // retry
                    getContext().getLog().info("No response received, Retrying with the worker: {}", command.worker().path());
                    askWorkerForPrimeNumber(command.worker());
                    return Behaviors.same();
                })
                .onAnyMessage(this::fullbackHandler)
                .build();
    }

    private void addNumber(String workerPath, BigInteger number) {
        LOGGER.info("Received from {} the result {}", workerPath, number);
        numbers.add(number);

        if (numbers.size() == 20) {
            LOGGER.info("Received all the result numbers => {}", numbers);
            // send all the prime numbers to the top level
            replayTo.tell(List.copyOf(numbers));
        }
    }

    private void startHandler(ActorRef<Collection<BigInteger>> replayTo) {
        getContext().getLog().info("Starting ManagerBehavior");

        this.replayTo = replayTo;

        ActorContext<Command> context = getContext();

        for (int i = 0; i < numberOfWorkers; i++) {
            LOGGER.info("Instantiate worker #{}", i);
            Behavior<WorkerBehavior.Command> child = WorkerBehavior.createWorkerBehavior();

            ActorRef<WorkerBehavior.Command> worker = context.spawn(child, WorkerBehavior.class.getSimpleName() + "-" + i);

            LOGGER.info("Sending {} to worker #{}", Messages.START, worker.path());
            //worker.tell(new WorkerBehavior.CalculatePrimeCommand(context.getSelf()));
            askWorkerForPrimeNumber(worker);

            //spawn.tell(new WorkerBehavior.CalculatePrimeCommand(context.getSelf()));
            //spawn.tell(new WorkerBehavior.CalculatePrimeCommand(context.getSelf()));
            //spawn.tell(new WorkerBehavior.CalculatePrimeCommand(context.getSelf()));
        }
    }




    private void askWorkerForPrimeNumber(ActorRef<WorkerBehavior.Command> worker) {

        getContext().ask(
                ManagerBehavior.Command.class,
                worker,
                Duration.ofSeconds(5L),
                me -> new WorkerBehavior.CalculatePrimeCommand(me),
                (response, throwable) -> {
                    if (response != null) {
                        return response;
                    }

                    getContext().getLog().warn("An error occurred while asking for prime number {}", worker.path(), throwable);
                    return new NoResponseReceivedCommand(worker);
                });
    }


    private Behavior<Command> fullbackHandler(Command s) {
        LOGGER.debug("Fullback handler: {} ", s);
        return this;
    }
}
