package com.bankingapp.backend.service;

import com.bankingapp.backend.model.Account;
import com.bankingapp.backend.model.Customer;
import com.bankingapp.backend.model.PaymentCard;
import com.bankingapp.backend.repository.PaymentCardRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.bankingapp.backend.utilities.Utils.generateCardNumber;

@Service
public class PaymentCardService {

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    // Date formatter for MM/YYYY format
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/yyyy");

    @Transactional
    public PaymentCard orderPaymentCard(Account account) {

        // Generate card number
        String cardNumber = generateCardNumber();

        // Generate expiry date (5 years from now)
        LocalDate expiryDate = LocalDate.now().plusYears(5);
        // expiry is in MM/YYYY format
        String formattedExpiryDate = expiryDate.format(DATE_FORMATTER);

        // Create payment card object and save it to the repository
        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setCardNumber(cardNumber);

        paymentCard.setExpiryDate(formattedExpiryDate);
        paymentCard.setAccount(account);
        paymentCard.setStatus("Active");
        return paymentCardRepository.save(paymentCard);
    }
    @Transactional
    public PaymentCard freezeCard(Long cardId) {
        PaymentCard card = paymentCardRepository.findById(cardId).orElseThrow(() -> new RuntimeException("Card not found"));
        if ("Stolen".equals(card.getStatus())) {
            throw new RuntimeException("Cannot freeze a stolen card");
        }
        card.setStatus("Frozen");
        return paymentCardRepository.save(card);
    }
    @Transactional
    public PaymentCard unfreezeCard(Long cardId) {
        PaymentCard card = paymentCardRepository.findById(cardId).orElseThrow(() -> new RuntimeException("Card not found"));
        if ("Stolen".equals(card.getStatus())) {
            throw new RuntimeException("Cannot unfreeze a stolen card");
        }
        card.setStatus("Active");
        return paymentCardRepository.save(card);
    }
    @Transactional
    public PaymentCard reportStolen(Long cardId) {
        PaymentCard card = paymentCardRepository.findById(cardId).orElseThrow(() -> new RuntimeException("Card not found"));
        card.setStatus("Stolen");
        return paymentCardRepository.save(card);
    }
    @Transactional
    public List<PaymentCard> getCardsByAccountId(long accountId) {
        return paymentCardRepository.findByAccount_AccountId(accountId);
    }

}
