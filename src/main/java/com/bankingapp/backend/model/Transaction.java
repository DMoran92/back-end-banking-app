package com.bankingapp.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "transaction")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TransactionId")
    private int transactionId;
    @Column(name = "AccountId")
    private long accountId;
    @Column(name = "RecipientId")
    private int recipientId;
    @Column(name = "Amount")
    private double amount;
    @Column(name = "Timestamp")
    private String timestamp;

    public Transaction() {
    }

    public Transaction(long accountId, int recipientId, double amount, String timestamp) {
        this.accountId = accountId;
        this.recipientId = recipientId;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public int getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(int recipientId) {
        this.recipientId = recipientId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
