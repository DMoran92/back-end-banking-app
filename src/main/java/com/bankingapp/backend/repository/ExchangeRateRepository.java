package com.bankingapp.backend.repository;

import com.bankingapp.backend.model.ExchangeRate;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.sql.Timestamp;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    ExchangeRate findByTargetCurrency(String targetCurrency);

    @Modifying
    @Transactional
    @Query(value = "UPDATE exchange_rates_current SET previous_rate = rate, rate = :rate, timestamp = :timestamp WHERE target_currency = :targetCurrency", nativeQuery = true)
    void updateExchangeRate(String targetCurrency, double rate, Timestamp timestamp);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO exchange_rates_current (base_currency, target_currency, previous_rate, rate, timestamp) VALUES (:baseCurrency, :targetCurrency, 0, :rate, :timestamp) ON DUPLICATE KEY UPDATE previous_rate = VALUES(rate), rate = VALUES(rate), timestamp = VALUES(timestamp)", nativeQuery = true)
    void insertExchangeRate(String baseCurrency, String targetCurrency, double rate, Timestamp timestamp);
}

