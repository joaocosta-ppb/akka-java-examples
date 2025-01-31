package io.github.jlmc.blockchain.akka.worker;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.github.jlmc.blockchain.entities.Block;
import io.github.jlmc.blockchain.entities.HashResult;
import io.github.jlmc.blockchain.hash.MineCalculator;

import java.io.Serial;
import java.io.Serializable;

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

        HashResult hashResult = MineCalculator.mineBlock(command.block(), command.difficulty(), command.startNonce(), command.endNonce());

        if (hashResult != null && hashResult.isCompleted()) {
            Block blockHash = command.block().withHashResult(hashResult);

             getContext().getLog().debug("Block Hashed with nonce: {} and hash {}", blockHash.getNonce(), blockHash.getHash());

            if (command.controller() != null) {
                ActorRef<Command> self = getContext().getSelf();

                BlockHashedCommand blockHashedCommand = new BlockHashedCommand(blockHash, self);
                command.controller().tell(blockHashedCommand);
            }


        } else {
            getContext().getLog().debug("Block NOT Hashed: [{}]", command.block());
        }

        return Behaviors.same();
    }

    public interface Command extends Serializable {
        //@Serial
        //private static final long serialVersionUID = 1L;
    }

    public record StartMiningCommand(Block block, int startNonce, int difficulty, int endNonce, ActorRef<BlockHashedCommand> controller) implements Command {
        @Serial
        private static final long serialVersionUID = 1L;
    }

}
