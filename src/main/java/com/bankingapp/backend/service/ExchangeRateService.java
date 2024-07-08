package com.bankingapp.backend.service;

import com.bankingapp.backend.model.ExchangeRate;
import com.bankingapp.backend.repository.ExchangeRateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Service
public class ExchangeRateService {

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    private final String apiUrl = "https://api.currencybeacon.com/v1/latest";
    private final String apiKey = System.getenv("CURRENCY_API_KEY");
    private final RestTemplate restTemplate = new RestTemplate();
    private final Logger logger = LoggerFactory.getLogger(ExchangeRateService.class);

    @Scheduled(fixedRate = 900000) // 15 minutes
    @Transactional
    public void fetchAndUpdateExchangeRates() {
        try {
            /* we are interested only in a selection of currencies, from a list of 170 */
            String[] targetCurrencies = {"USD", "GBP", "JPY", "CNY", "CHF"};
            String symbols = String.join(",", targetCurrencies);
            /* prepare the url with the api_key and currencies */
            String url = apiUrl + "?api_key=" + apiKey + "&base=EUR&symbols=" + symbols;

            logger.info("Fetching exchange rates from URL: {}", url);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            logger.info("Exchange Rates Response: {}", response);
            /* the currency exchange rates is available in rates */
            if (response != null && response.get("rates") != null) {
                Map<String, Double> rates = (Map<String, Double>) response.get("rates");
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                /* update or insert currency rates */
                for (String currency : targetCurrencies) {
                    if (rates.containsKey(currency)) {
                        double rate = rates.get(currency);
                        ExchangeRate existingRate = exchangeRateRepository.findByTargetCurrency(currency);
                        if (existingRate != null) {
                            exchangeRateRepository.updateExchangeRate(currency, rate, timestamp);
                            logger.info("Updated exchange rate for {}: {}", currency, rate);
                        } else {
                            exchangeRateRepository.insertExchangeRate("EUR", currency, rate, timestamp);
                            logger.info("Inserted exchange rate for {}: {}", currency, rate);
                        }
                    } else {
                        logger.error("Rate for {} not found in the response", currency);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to fetch or save exchange rates", e);
        }
    }
    /* Fetch the latest exchange rates for each target currency */
    public List<ExchangeRate> getLatestExchangeRates() {
        return exchangeRateRepository.findAll();
    }
}
