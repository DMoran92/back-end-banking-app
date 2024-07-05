package com.bankingapp.backend.controller;

import com.bankingapp.backend.model.SavingsTransaction;
import com.bankingapp.backend.service.SavingsTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/savings")
public class SavingsController {

    private static final Logger logger = LoggerFactory.getLogger(SavingsController.class);

    @Autowired
    private SavingsTransactionService savingsTransactionService;

    /* start a savings transaction */
    @PostMapping("/start")
    public ResponseEntity<String> createSavingsTransaction(@RequestBody SavingsTransaction transaction) {

        savingsTransactionService.createSavingsTransaction(transaction);
        return ResponseEntity.ok("created savings successful");

    }
    /* stop a savings transaction */
    @PostMapping("/stop/{transactionId}")
    public ResponseEntity<String> stopSavingsTransaction(@PathVariable Long transactionId) {
         savingsTransactionService.stopSavingsTransaction(transactionId);
        return ResponseEntity.ok("stopped savings successful");
    }

    /* retrieve savings transactions by account ID */
    @GetMapping("/{accountId}")
    public ResponseEntity<List<SavingsTransaction>> getSavingsTransactionsByAccount(@PathVariable Long accountId) {
        logger.info("entered get savings. account id: {}", accountId);
        List<SavingsTransaction> transactions = savingsTransactionService.getSavingsTransactionsByAccount(accountId);
        logger.info(" Retrieved the savings transactions : {}", transactions.size());
        return ResponseEntity.ok(transactions);
    }
}

