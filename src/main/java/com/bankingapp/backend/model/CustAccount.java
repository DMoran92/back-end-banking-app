package com.bankingapp.backend.model;

import jakarta.persistence.*;

//attmpt at creating a joined entity
//https://www.baeldung.com/jpa-mapping-single-entity-to-multiple-tables

@Entity
@Table(name = "customer")
@SecondaryTable(name = "account", pkJoinColumns = @PrimaryKeyJoinColumn(name = "AccountId"))
public class CustAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CustomerId")
    int customerId;
    @Column(name = "AccountType", table = "account")
    private String accountType;
    @Column(name = "Balance", table = "account")
    private double balance;

    public CustAccount(int customerId, String accountType, double balance) {
        this.customerId = customerId;
        this.accountType = accountType;
        this.balance = balance;
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
