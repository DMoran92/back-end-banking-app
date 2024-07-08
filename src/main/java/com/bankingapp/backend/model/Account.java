package com.bankingapp.backend.model;

import jakarta.persistence.*;

import java.util.List;


@Entity
@Table(name = "account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AccountId")
    private long accountId;
    @Column(name = "CustomerId")
    private int customerId;
    @Column(name = "AccountType")
    private String accountType;
    @Column(name = "Balance")
    private double balance;
    @Column(name = "IBAN")
    private String iban;
    @Column(name = "Currency")
    private String currency = "EUR";

    @OneToMany (fetch = FetchType.EAGER)
    @JoinColumn(name = "AccountId", referencedColumnName = "AccountId")
    private List<Transaction> transactions;

    // Constructors
    public Account() {}

    public Account(int customerId, String accountType, double balance) {
        this.customerId = customerId;
        this.accountType = accountType;
        this.balance = balance;
    }

    // Getters and Setters
    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getIban() { return iban; }

    public void setIban(String iban) { this.iban = iban; }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public String getCurrency() { return currency; }

    public void setCurrency(String currency) { this.currency = currency; }
}
