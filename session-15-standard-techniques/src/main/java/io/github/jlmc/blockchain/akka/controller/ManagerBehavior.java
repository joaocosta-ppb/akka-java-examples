package io.github.jlmc.blockchain.akka.controller;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.github.jlmc.blockchain.akka.worker.WorkerBehavior;
import io.github.jlmc.blockchain.entities.Block;

import java.io.Serializable;
import java.util.UUID;

import static io.github.jlmc.blockchain.akka.Constants.SPLIT_SIZE;

public class ManagerBehavior extends AbstractBehavior<ManagerBehavior.Command> {

    private ActorRef<Command> replayTo;
    private long currentNonce = 0;
    private Block blockToHash;
    private int difficultly;
    private boolean currentlyMining = false;

    private ManagerBehavior(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(ManagerBehavior::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onSignal(Terminated.class, signal -> {
                    // Every this a worker terminate the manager receives this signal
                    getContext().getLog().info("Child {} has terminated.", signal.getRef().path().name());

                    // we can create a new worker to replace the one that has terminated.
                    startNextWorker(blockToHash, difficultly);

                    return this;
                })
                .onMessage(MineBlockCommand.class, command -> {

                    // create worker actors
                    this.replayTo = command.replayTo();
                    this.blockToHash = command.block();
                    this.difficultly = command.difficultly();
                    this.currentlyMining = true;

                    for (int i = 0; i < command.numberOfWorkers(); i++) {
                        startNextWorker(blockToHash, difficultly);
                    }

                    return Behaviors.same();
                })
                .onMessage(BlockHashedSucessfulCommand.class, command -> {
                    stopAllActiveWorkerChild();

                    Block blockHashed = command.block();

                    this.currentlyMining = false;

                    // send the result to the parent
                    ActorRef<Command> self = getContext().getSelf();
                    replayTo.tell(new ResultCommand(blockHashed, self));

                    return Behaviors.same();
                })
                .build();
    }

    private void stopAllActiveWorkerChild() {
        // close all active worker
        for (ActorRef<Void> child : getContext().getChildren()) {
            getContext().stop(child);
        }
    }

    private void startNextWorker(Block block, int difficultly) {
        if (!currentlyMining) {
            return;
        }

        String workerId = "worker-%d-%s".formatted(currentNonce, UUID.randomUUID());
        int startNonce = (int) currentNonce * SPLIT_SIZE;


        getContext().getLog().info("About to start mining with nonces starting at {} on the worker id {}", startNonce, workerId);

        Behavior<WorkerBehavior.Command> workerBehavior =
                    Behaviors.supervise(WorkerBehavior.create()).onFailure(SupervisorStrategy.resume());

        //ActorRef<WorkerBehavior.Command> worker = getContext().spawn(WorkerBehavior.create(), workerId);
        ActorRef<WorkerBehavior.Command> worker = getContext().spawn(workerBehavior, workerId);
        getContext().watch(worker);
        worker.tell(new WorkerBehavior.StartMiningCommand(block, startNonce, difficultly, getContext().getSelf()));
        currentNonce++;
    }

    public interface Command extends Serializable {
    }

    public record MineBlockCommand(ActorRef<Command> replayTo, Block block, int difficultly,
                                   int numberOfWorkers) implements Command {
    }

    public record BlockHashedSucessfulCommand(Block block, ActorRef<WorkerBehavior.Command> worker) implements Command {
    }

    public record ResultCommand(Block block, ActorRef<ManagerBehavior.Command> worker) implements Command {}
}
