package com.bankingapp.backend.controller;

import com.bankingapp.backend.model.Customer;
import com.bankingapp.backend.model.PaymentCard;
import com.bankingapp.backend.repository.CustomerRepository;
import com.bankingapp.backend.service.PaymentCardService;
import com.bankingapp.backend.utilities.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.bankingapp.backend.utilities.Utils.getAuthenticatedUsername;

@RestController
@RequestMapping("/api/cards")
public class PaymentCardController {

    private static final Logger logger = LoggerFactory.getLogger(FavouritePayeeController.class);

    @Autowired
    private PaymentCardService paymentCardService;

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping
    public List<PaymentCard> getCardsByCustomerId() {
        String username = getAuthenticatedUsername();
        Customer customer = customerRepository.findByUsername(username);

        return paymentCardService.getCardsByCustomerId(customer.getCustomerId());
    }

    @PostMapping("/order")
    public ResponseEntity<String> orderNewCard() {

        String username = getAuthenticatedUsername();
        /* retrieve the customer using the username */
        Customer customer = customerRepository.findByUsername(username);
        if (customer == null) {
            logger.error("No customer found with username: {}", username);
            throw new RuntimeException("No customer found with username: " + username);
        }

        try {
            PaymentCard paymentCard = paymentCardService.orderPaymentCard(customer);
            return ResponseEntity.ok("Payment card ordered successfully: " + paymentCard);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error ordering payment card: " + e.getMessage());
        }

    }

    @PutMapping("/freeze")
    public PaymentCard freezeCard(@RequestParam Long id) {
        return paymentCardService.freezeCard(id);
    }

    @PutMapping("/unfreeze")
    public PaymentCard unfreezeCard(@RequestParam Long id) { return paymentCardService.unfreezeCard(id); }

    @PutMapping("/report-stolen")
    public PaymentCard reportStolen(@RequestParam Long id) {
        return paymentCardService.reportStolen(id);
    }
}