package com.bankingapp.backend.model;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "two_factor_auth")
public class TwoFactorAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    @Column(name = "last_known_browser")
    private String lastKnownBrowser;
    @Column(name = "two_fa_code")
    private String twoFaCode;
    @Column(name = "two_fa_code_expiry")
    private Timestamp twoFaCodeExpiry;

    public Long getId() { return id;}

    public void setId(Long id) { this.id = id; }

    public Customer getCustomer() { return customer; }

    public void setCustomer(Customer customer) { this.customer = customer; }

    public String getLastKnownBrowser() { return lastKnownBrowser; }

    public void setLastKnownBrowser(String lastKnownBrowser) { this.lastKnownBrowser = lastKnownBrowser; }

    public String getTwoFaCode() { return twoFaCode; }

    public void setTwoFaCode(String twoFaCode) { this.twoFaCode = twoFaCode; }

    public Timestamp getTwoFaCodeExpiry() { return twoFaCodeExpiry; }

    public void setTwoFaCodeExpiry(Timestamp twoFaCodeExpiry) { this.twoFaCodeExpiry = twoFaCodeExpiry; }

}