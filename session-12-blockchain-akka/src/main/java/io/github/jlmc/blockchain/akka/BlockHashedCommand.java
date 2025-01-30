package io.github.jlmc.blockchain.akka;

import akka.actor.typed.ActorRef;
import io.github.jlmc.blockchain.entities.Block;

public record BlockHashedCommand(Block block, ActorRef<WorkerBehavior.Command> worker) {
}
