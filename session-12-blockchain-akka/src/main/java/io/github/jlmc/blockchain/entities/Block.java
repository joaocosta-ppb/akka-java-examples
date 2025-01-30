package io.github.jlmc.blockchain.entities;

/**
 * The wrapper for the transaction.
 */
public final class Block {
    private final Transaction transaction;
    private final String previousHash;
    private String hash;
    private int nonce;


    public Block(Transaction transaction, String previousHash) {
        this.transaction = transaction;
        this.previousHash = previousHash;
    }

    public Block(Transaction transaction, String previousHash, String hash, int nonce) {
        this(transaction, previousHash);
        this.hash = hash;
        this.nonce = nonce;
    }


    public Block withHashResult(HashResult hashResult) {
        return new Block(transaction, previousHash, hashResult.hash(), hashResult.nonce());
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public long getTransactionTimestamp() {
        return transaction.timestamp();
    }

    public int getNonce() {
        return nonce;
    }

    public String getHash() {
        return hash;
    }
}
