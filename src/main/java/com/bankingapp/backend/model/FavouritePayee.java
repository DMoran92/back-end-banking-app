package com.bankingapp.backend.model;

import jakarta.persistence.*;


@Entity
@Table(name = "favouritepayees")
public class FavouritePayee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String iban;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", referencedColumnName = "CustomerId", nullable = false)
    private Customer customer;

    // Default constructor
    public FavouritePayee() {}

    // Constructor with parameters
    public FavouritePayee(String name, String iban, Customer customer) {
        this.name = name;
        this.iban = iban;
        this.customer = customer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}