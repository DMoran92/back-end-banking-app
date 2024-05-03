package com.bankingapp.backend.model;

import jakarta.persistence.*;


@Entity
@Table(name = "account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AccountId")
    private int accountId;
    @Column(name = "CustomerId")
    private int customerId;
    @Column(name = "AccountType")
    private String accountType;
    @Column(name = "Balance")
    private double balance;

    // Constructors
    public Account() {}

    public Account(int customerId, String accountType, double balance) {
        this.customerId = customerId;
        this.accountType = accountType;
        this.balance = balance;
    }

    // Getters and Setters
    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
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
}
