package io.github.jlmc.blockchain.hash;

import io.github.jlmc.blockchain.entities.Block;
import io.github.jlmc.blockchain.entities.HashResult;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;

public class MineCalculator {

    public static String calculateHash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] rawHash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte hash : rawHash) {
                String hex = Integer.toHexString(0xFF & hash);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static HashResult mineBlock(Block block, int difficultyLevel, int startNonce, int endNonce) {
        String hash = String.join("", Collections.nCopies(difficultyLevel, "X"));
        String target = String.join("", Collections.nCopies(difficultyLevel, "0"));

        int nonce = startNonce;
        while (!hash.substring(0, difficultyLevel).equals(target) && nonce < endNonce) {
            nonce++;
            String dataToEncode = getDataToEncode(block, nonce);
            hash = calculateHash(dataToEncode);
        }
        if (hash.substring(0, difficultyLevel).equals(target)) {
            return HashResult.of(hash, nonce).completed();
        } else {
            return null;
        }
    }

    /*private static String getDataToEncode(Block block, int nonce) {
        return block.getPreviousHash() + block.getTransactionTimestamp() + nonce + block.getTransaction();
    }
     */

    private static String getDataToEncode(Block block, int nonce) {
        return "%s%s%s%s".formatted(
                block.getPreviousHash(),
                "" + block.getTransactionTimestamp(),
                "" + nonce,
                "" + block.getTransaction()
        );
    }

    private static String getDataToEncode(Block block) {
        return getDataToEncode(block, block.getNonce());
    }

    public static boolean validateBlock(Block block) {
        String dataToEncode = getDataToEncode(block);
        String checkHash = calculateHash(dataToEncode);
        return (checkHash.equals(block.getHash()));
    }

}
