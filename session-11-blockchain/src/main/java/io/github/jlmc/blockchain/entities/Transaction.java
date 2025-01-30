package io.github.jlmc.blockchain.entities;

public record Transaction(int id, long timestamp, int accountNumber, double amount) {
}
