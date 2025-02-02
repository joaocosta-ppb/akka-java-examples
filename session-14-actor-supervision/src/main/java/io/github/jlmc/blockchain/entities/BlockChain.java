package io.github.jlmc.blockchain.entities;

import io.github.jlmc.blockchain.hash.MineCalculator;

import java.util.Iterator;
import java.util.LinkedList;

public final class BlockChain implements Iterable<Block> {
    private final LinkedList<Block> blocks = new LinkedList<>();

    private BlockChain() {
    }

    public static BlockChain empty() {
        return new BlockChain();
    }

    public void add(Block block) {
        String lastHash = blocks.isEmpty() ? "0" : blocks.getLast().getHash();

        if (!lastHash.equals(block.getPreviousHash())) {
            throw new BlockChainValidationException("Blocks have different hashes, lastHash <%s> is not equals to the given <%s>".formatted(lastHash, block.getPreviousHash()));
        }

        if (!MineCalculator.validateBlock(block)) {
            throw new BlockChainValidationException("The Block is not valid %s".formatted(block));
        }

        this.blocks.add(block);
    }

    public int size() {
        return blocks.size();
    }

    @Override
    public Iterator<Block> iterator() {
        return blocks.iterator();
    }

    public String getLastHash() {
        return this.blocks.getLast().getHash();
    }
}
