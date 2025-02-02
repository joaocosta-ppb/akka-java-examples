package io.github.jlmc.blockchain.akka.controller;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.github.jlmc.blockchain.akka.worker.WorkerBehavior;
import io.github.jlmc.blockchain.entities.Block;

import java.io.Serializable;
import java.util.Map;

import static io.github.jlmc.blockchain.akka.Constants.SPLIT_SIZE;

public class ManagerBehavior extends AbstractBehavior<ManagerBehavior.Command> {

    ActorRef<Map<String, Object>> replayTo;
    long currentNonce = 0;

    private ManagerBehavior(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(ManagerBehavior::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()

                .onMessage(MineBlockCommand.class, command -> {

                    // create worker actors
                    this.replayTo = command.replayTo();

                    for (int i = 0; i < command.numberOfWorkers(); i++) {
                        startNextWorker(command);
                    }

                    //command.replayTo().tell(Map.of("command", "AAAAA"));
                    return Behaviors.same();
                })
                .onMessage(BlockHashedSucessfulCommand.class, command -> {
                    Block blockHashed = command.block();
                    ActorRef<WorkerBehavior.Command> worker = command.worker();

                    replayTo.tell(Map.of("result", blockHashed));

                    return Behaviors.ignore();
                })
                .build();
    }

    private void startNextWorker(MineBlockCommand command) {
        String workerId = "worker-%d".formatted(currentNonce);
        var worker = getContext().spawn(WorkerBehavior.create(), workerId);

        worker.tell(new WorkerBehavior.StartMiningCommand(
                command.block,
                (int) (currentNonce * SPLIT_SIZE),
                command.difficultly,
                getContext().getSelf()));

        currentNonce++;
    }

    public interface Command extends Serializable {
    }

    public record MineBlockCommand(ActorRef<Map<String, Object>> replayTo, Block block, int difficultly,
                                   int numberOfWorkers) implements Command {
    }

    public record BlockHashedSucessfulCommand(Block block, ActorRef<WorkerBehavior.Command> worker) implements Command {
    }
}
