package com.bankingapp.backend.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "exchange_rates_current")
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "base_currency", nullable = false)
    private String baseCurrency;

    @Column(name = "target_currency", nullable = false)
    private String targetCurrency;

    @Column(name = "previous_rate", nullable = false)
    private double previousRate;

    @Column(name = "rate", nullable = false)
    private double rate;

    @Column(name = "timestamp", nullable = false)
    private Timestamp timestamp;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getBaseCurrency() { return baseCurrency; }

    public void setBaseCurrency(String baseCurrency) { this.baseCurrency = baseCurrency; }

    public String getTargetCurrency() { return targetCurrency; }

    public void setTargetCurrency(String targetCurrency) { this.targetCurrency = targetCurrency; }

    public double getPreviousRate() { return previousRate; }

    public void setPreviousRate(double previousTargetCurrency) { this.previousRate = previousTargetCurrency; }

    public double getRate() { return rate; }

    public void setRate(double rate) { this.rate = rate; }

    public Timestamp getTimestamp() { return timestamp; }

    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
