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
    @Column(name = "RecipientName")
    private String recipientName;
    @Column(name = "RecipientIBAN")
    private String recipientIBAN;
    @Column(name = "Amount")
    private double amount;
    @Column(name = "Timestamp")
    private String timestamp;
    @Column(name = "SenderIBAN")
    private String senderIBAN;
    @Column(name = "Category")
    private String category;
    @Column(name = "SenderName")
    private String senderName;

    public Transaction() {
    }

    public Transaction(long accountId, String recipientIBAN, double amount, String timestamp, String category) {
        this.accountId = accountId;
        this.recipientIBAN = recipientIBAN;
        this.amount = amount;
        this.timestamp = timestamp;
        this.category = category;
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

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientIBAN() {
        return recipientIBAN;
    }

    public void setRecipientIBAN(String recipientIBAN) {
        this.recipientIBAN = recipientIBAN;
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

    public String getSenderIBAN() {
        return senderIBAN;
    }

    public void setSenderIBAN(String senderIBAN) {
        this.senderIBAN = senderIBAN;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
}
