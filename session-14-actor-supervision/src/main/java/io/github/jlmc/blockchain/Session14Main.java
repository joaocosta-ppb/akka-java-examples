package io.github.jlmc.blockchain;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import io.github.jlmc.blockchain.akka.controller.ManagerBehavior;
import io.github.jlmc.blockchain.control.BlockStubData;
import io.github.jlmc.blockchain.entities.Block;
import io.github.jlmc.blockchain.entities.BlockChain;
import io.github.jlmc.blockchain.entities.BlockChainValidationException;
import io.github.jlmc.blockchain.hash.MineCalculator;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

public class Session14Main {


    static BlockChain blocks = BlockChain.empty();
    static ActorSystem<ManagerBehavior.Command> actorSystem;
    int difficultyLevel = 5;
    long start = System.currentTimeMillis();

    public static void mineBlocks() {

        actorSystem = ActorSystem.create(ManagerBehavior.create(), "BlockChainMiner");
        mineNextBlock();
    }

    private static void mineNextBlock() {
        int nextBlockId = blocks.size();

        if (nextBlockId < 10) {
            String lastHash = nextBlockId > 0 ? blocks.getLastHash() : "0";
            Block block = BlockStubData.getNextBlock(nextBlockId, lastHash);


            CompletionStage<ManagerBehavior.Command> results =
                    AskPattern.ask(
                            actorSystem,
                            me -> new ManagerBehavior.MineBlockCommand(me, block, 5, 10),
                            Duration.ofMinutes(30),
                            actorSystem.scheduler()
                    );


            results.whenComplete((reply, failure) -> {
                if (reply instanceof ManagerBehavior.ResultCommand resultCommand) {
                    Block o = resultCommand.block();

                    try {
                        blocks.add(o);
                        System.out.println("Block added with hash : " + o.getHash());
                        System.out.println("Block added with nonce: " + o.getNonce());
                        mineNextBlock();
                    } catch (BlockChainValidationException e) {
                        System.out.println("ERROR: No valid hash was found for a block");
                    }
                } else {
                    System.out.println("ERROR: No valid hash was found for a block - " + failure);
                }


            });

        } else {
            //Long end = System.currentTimeMillis();
            actorSystem.terminate();
            printAndValidate(blocks);
            //System.out.println("Time taken " + (end - start) + " ms.");
        }

    }

    public static void main(String[] args) throws InterruptedException {

        mineBlocks();


    }

    private static void printAndValidate(BlockChain blocks) {

        String lastHash = "0";
        for (Block block : blocks) {
            System.out.println("Block " + block.getTransaction().id() + " ");
            System.out.println(block.getTransaction());

            if (block.getPreviousHash().equals(lastHash)) {
                System.out.print("Last hash matches ");
            } else {
                System.out.print("Last hash doesn't match ");
            }

            if (MineCalculator.validateBlock(block)) {
                System.out.println("and hash is valid");
            } else {
                System.out.println("and hash is invalid");
            }

            lastHash = block.getHash();

        }
    }

}
