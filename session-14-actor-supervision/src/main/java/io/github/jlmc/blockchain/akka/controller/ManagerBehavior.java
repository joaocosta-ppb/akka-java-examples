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
import java.util.Map;

import static io.github.jlmc.blockchain.akka.Constants.SPLIT_SIZE;

public class ManagerBehavior extends AbstractBehavior<ManagerBehavior.Command> {

    private ActorRef<Map<String, Object>> replayTo;
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
                .onSignal(Terminated.class, handler -> {
                    // Every this a worker terminate the manager receives this signal

                    // we can create a new worker to replace the one that has terminated.
                    startNextWorker(blockToHash, difficultly);

                    return Behaviors.same();
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
                    ActorRef<WorkerBehavior.Command> worker = command.worker();

                    this.currentlyMining = false;

                    // send the result to the parent
                    replayTo.tell(Map.of("result", blockHashed));

                    return Behaviors.ignore();
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

        String workerId = "worker-%d".formatted(currentNonce);

        //
        var supervisedWorker =
                Behaviors.supervise(WorkerBehavior.create())
                        // when worker crashed
                        .onFailure(SupervisorStrategy.resume());

        ActorRef<WorkerBehavior.Command> worker = getContext().spawn(supervisedWorker, workerId);

        // declaring a watch of the worker
        getContext().watch(worker);

        // tell the work to start it job
        worker.tell(new WorkerBehavior.StartMiningCommand(
                block,
                (int) (currentNonce * SPLIT_SIZE),
                difficultly,
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
