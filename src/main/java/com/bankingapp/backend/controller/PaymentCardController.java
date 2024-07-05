package com.bankingapp.backend.controller;

import com.bankingapp.backend.model.Account;
import com.bankingapp.backend.model.Customer;
import com.bankingapp.backend.model.PaymentCard;
import com.bankingapp.backend.model.Transaction;
import com.bankingapp.backend.repository.AccountRepository;
import com.bankingapp.backend.repository.CustomerRepository;
import com.bankingapp.backend.service.PaymentCardService;
import com.bankingapp.backend.service.TransactionService;
import com.bankingapp.backend.utilities.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.Date;
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

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionService transactionService;

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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No username found");
        }

        List<Account> accounts = customer.getAccounts();

        if (accounts.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No accounts found for customer");
        }

        // Deduct service charge from the first account
        Account account = accounts.get(0);
        double serviceChargeAmount = 9.99;

        if (account.getBalance() < serviceChargeAmount) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Insufficient balance for service charge");
        }

        // Create transaction for service charge
        Transaction serviceChargeTransaction = new Transaction();
        serviceChargeTransaction.setAccountId(account.getAccountId());
        serviceChargeTransaction.setRecipientName("Bank of Galway");
        serviceChargeTransaction.setRecipientIBAN("IE01BOGY91332299999999");
        serviceChargeTransaction.setAmount(serviceChargeAmount);
        serviceChargeTransaction.setTimestamp(new Timestamp(new Date().getTime()).toString());
        serviceChargeTransaction.setCategory("Service Charge");

        // Update account balance
        account.setBalance(account.getBalance() - serviceChargeAmount);
        accountRepository.save(account);

        // Save the transaction
        transactionService.makeNewTransaction(serviceChargeTransaction);

        try {
            PaymentCard paymentCard = paymentCardService.orderPaymentCard(customer);
            logger.info("Created new payment card for the user");
            return ResponseEntity.ok("Payment card ordered successfully: " + paymentCard.getCardNumber());
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