package io.github.jlmc.blockchain.entities;

public record HashResult(String hash, int nonce, boolean isCompleted) {

    public static HashResult of( String hash, int nonce) {
        return new HashResult(hash, nonce, false);
    }

    public HashResult completed() {
        return new HashResult(hash, nonce, true);
    }
}
