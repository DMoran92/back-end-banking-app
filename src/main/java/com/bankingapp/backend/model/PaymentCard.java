package com.bankingapp.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "paymentcard")
public class PaymentCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_id")
    private Long cardId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "card_number", nullable = false)
    private String cardNumber;
    @Column(name = "expiry_date", nullable = false)
    private String expiryDate;
    @Column(name = "status", nullable = false)
    private String status;

    // Default constructor
    public PaymentCard() {}

    public PaymentCard(String cardNumber, String expiryDate, String status, Customer customer) {
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.status = status;
        this.customer = customer;
    }

    public Long getCardId() { return cardId; }

    public void setCardId(Long cardId) { this.cardId = cardId; }

    public String getCardNumber() { return cardNumber; }

    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getExpiryDate() { return expiryDate; }

    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status;}
}