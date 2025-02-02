package io.github.jlmc.blockchain.akka.worker;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.github.jlmc.blockchain.akka.controller.ManagerBehavior;
import io.github.jlmc.blockchain.entities.Block;
import io.github.jlmc.blockchain.entities.HashResult;
import io.github.jlmc.blockchain.hash.MineCalculator;

import java.io.Serial;
import java.io.Serializable;

import static io.github.jlmc.blockchain.akka.Constants.SPLIT_SIZE;

public class WorkerBehavior extends AbstractBehavior<WorkerBehavior.Command> {

    private WorkerBehavior(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(WorkerBehavior::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartMiningCommand.class, this::handlerCommand)
                .build();
    }

    private Behavior<Command> handlerCommand(StartMiningCommand command) {
        getContext().getLog().debug("Received command: {}", command);

        var endNonce =  command.startNonce() + SPLIT_SIZE;

        HashResult hashResult = MineCalculator.mineBlock(command.block(), command.difficulty(), command.startNonce(), endNonce);

        if (hashResult != null && hashResult.isCompleted()) {
            Block blockHash = command.block().withHashResult(hashResult);

             getContext().getLog().debug("Block Hashed with nonce: {} and hash {}", blockHash.getNonce(), blockHash.getHash());

            if (command.replayTo() != null) {
                ActorRef<Command> self = getContext().getSelf();

                ManagerBehavior.BlockHashedSucessfulCommand blockHashedCommand = new ManagerBehavior.BlockHashedSucessfulCommand(blockHash, self);
                command.replayTo().tell(blockHashedCommand);
            }

        } else {
            getContext().getLog().debug("Block NOT Hashed: [{}] [{}] ", getContext().getSelf().path(), command.block());
        }

        return Behaviors.same();
    }

    public interface Command extends Serializable {
        //@Serial
        //private static final long serialVersionUID = 1L;
    }

    public record StartMiningCommand(Block block, int startNonce, int difficulty, ActorRef<ManagerBehavior.Command> replayTo) implements Command {
        @Serial
        private static final long serialVersionUID = 1L;
    }

}
