package io.github.jlmc.blockchain.entities;

public class BlockChainValidationException extends RuntimeException {
    BlockChainValidationException(String message) {
        super(message);
    }
}
