package io.github.jlmc.blockchain;

import io.github.jlmc.blockchain.control.BlockStubData;
import io.github.jlmc.blockchain.hash.MineCalculator;
import io.github.jlmc.blockchain.entities.Block;
import io.github.jlmc.blockchain.entities.BlockChain;
import io.github.jlmc.blockchain.entities.HashResult;

public class Session14Main {

    public static void main(String[] args) {

        int difficultyLevel = 6;

        Long start = System.currentTimeMillis();

        BlockChain blocks = BlockChain.empty();

        String lastHash = "0";
        for (int i = 0; i < 10; i++) {
            Block nextBlock = BlockStubData.getNextBlock(i, lastHash);

            HashResult hashResult = MineCalculator.mineBlock(nextBlock, difficultyLevel, 0, 100000000);
            if (hashResult == null) {
                throw new RuntimeException("Didn't find a valid hash for block " + i);
            }

            Block block = nextBlock.withHashResult(hashResult);

            blocks.add(block);

            System.out.println("Block " + i + " hash : " + block.getHash());
            System.out.println("Block " + i + " nonce: " + block.getNonce());
            lastHash = block.getHash();
        }

        Long end = System.currentTimeMillis();
        printAndValidate(blocks);

        System.out.println("Time taken " + (end - start) + " ms.");
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
