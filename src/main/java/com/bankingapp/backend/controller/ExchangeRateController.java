package com.bankingapp.backend.controller;

import com.bankingapp.backend.model.ExchangeRate;
import com.bankingapp.backend.service.ExchangeRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/exchange-rates")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @Autowired
    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping("/latest")
    public List<ExchangeRate> getLatestExchangeRates() {
        return exchangeRateService.getLatestExchangeRates();
    }
}
