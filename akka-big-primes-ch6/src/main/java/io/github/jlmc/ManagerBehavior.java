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
import java.util.SortedSet;
import java.util.TreeSet;

public class ManagerBehavior extends AbstractBehavior<ManagerBehavior.Command> {

    public interface Command extends Serializable {
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static class InstructionCommand implements Command {
        @Serial
        private static final long serialVersionUID = 1L;
        private final String message;

        public InstructionCommand(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static class ResultCommand implements Command {
        @Serial
        private static final long serialVersionUID = 1L;
        private final BigInteger number;
        private final String workerPath;

        public ResultCommand(BigInteger number, String workerPath) {
            this. number = number;
            this.workerPath = workerPath;
        }

        public BigInteger getNumber() {
            return number;
        }

        public String getWorkerPath() {
            return workerPath;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagerBehavior.class);
    private final int numberOfWorkers;

    private final SortedSet<BigInteger> numbers = new TreeSet<>();

    private ManagerBehavior(ActorContext<ManagerBehavior.Command> context, int numberOfWorkers) {
        super(context);
        this.numberOfWorkers = numberOfWorkers;
    }

    public static Behavior<Command> createNewInstance(int numberOfWorkers) {
        LOGGER.info("Creating new ManagerBehavior, {} workers", numberOfWorkers);
        return Behaviors.setup((c) -> new ManagerBehavior(c, numberOfWorkers));
    }


    @Override
    public Receive<ManagerBehavior.Command> createReceive() {
        LOGGER.info("Creating ManagerBehavior Received");
        return newReceiveBuilder()
                .onMessage(InstructionCommand.class, command -> {
                    if (Messages.START.equals(command.getMessage())) {
                        startHandler();
                    } else {
                        LOGGER.info("Ignoring message {}", command.getMessage());
                    }

                    return this;
                })
                .onMessage(ResultCommand.class, command -> {
                    addNumber(command.getWorkerPath(), command.getNumber());
                    return this;
                })
                .onAnyMessage(this::fullbackHandler)
                .build();
    }

    private void addNumber(String workerPath, BigInteger number) {
        LOGGER.info("Received from {} the result {}", workerPath, number);
        numbers.add(number);

        if (numbers.size() == 20) {
            LOGGER.info("Received all the result numbers => {}", numbers);
        }
    }

    private void startHandler() {
        LOGGER.info("Starting ManagerBehavior");
        ActorContext<ManagerBehavior.Command> context = getContext();

        for (int i = 0; i < numberOfWorkers; i++) {
            LOGGER.info("Instantiate worker #{}", i);
            Behavior<WorkerBehavior.Command> child = WorkerBehavior.createWorkerBehavior();

            ActorRef<WorkerBehavior.Command> spawn = context.spawn(child, WorkerBehavior.class.getSimpleName() + "-" + i);

            LOGGER.info("Sending {} to worker #{}", Messages.START,  spawn.path());
            spawn.tell(new WorkerBehavior.Command(Messages.START, context.getSelf()));
            spawn.tell(new WorkerBehavior.Command(Messages.START, context.getSelf()));
            spawn.tell(new WorkerBehavior.Command(Messages.START, context.getSelf()));
        }
    }

    private Behavior<ManagerBehavior.Command> fullbackHandler(Command s) {
        LOGGER.debug("Fullback handler: {} ", s);
        return this;
    }
}
