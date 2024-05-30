package model;

import repository.BankRepository;

import java.time.LocalDateTime;

public class Transaction {
    private LocalDateTime timestamp;
    private String description;
    private double amount;
    private BankAccount source;
    private BankAccount destination;

    public Transaction(String description, double amount, BankAccount source, BankAccount destination) {
        this.timestamp = LocalDateTime.now();
        this.description = description;
        this.amount = amount;
        this.source = source;
        this.destination = destination;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public BankAccount getSource() {
        return source;
    }

    public BankAccount getDestination() {
        return destination;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "Transaction:" +
                "\nDate and Time: " + timestamp +
                "\nDescription: " + description +
                "\nAmount: " + amount +
                "\nFrom: " + source +
                "\nTo: " + destination;
    }
}
