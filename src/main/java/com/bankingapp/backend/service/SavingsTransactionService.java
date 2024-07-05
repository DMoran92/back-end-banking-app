package com.bankingapp.backend.service;

import com.bankingapp.backend.model.Account;
import com.bankingapp.backend.model.SavingsTransaction;
import com.bankingapp.backend.repository.AccountRepository;
import com.bankingapp.backend.repository.SavingsTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SavingsTransactionService {

    private static final Logger logger = LoggerFactory.getLogger(SavingsTransactionService.class);

    @Autowired
    private SavingsTransactionRepository savingsTransactionRepository;

    @Autowired
    private AccountRepository accountRepository;
    /* create new savings product */
    public void createSavingsTransaction(SavingsTransaction transaction) {
        Account account = accountRepository.findById(transaction.getAccountId())
             .orElseThrow(() -> new RuntimeException("Account not found"));
        /* deduct the savings amount from the account balance */
        account.setBalance(account.getBalance() - transaction.getAmount());
        accountRepository.save(account);
        /* set the start date and status of the transaction */
        transaction.setStartDate(new Timestamp(new Date().getTime()));
        transaction.setStatus("ACTIVE");
        /* save the transaction in the repository */
        savingsTransactionRepository.save(transaction);
    }
    /* retrieve savings transactions by account ID */
    public List<SavingsTransaction> getSavingsTransactionsByAccount(Long accountId) {
        return savingsTransactionRepository.findByAccountId(accountId);
    }
    /* stop an active savings product */
    public void stopSavingsTransaction(Long transactionId) {
        /* retrieve the transaction using transactionId */
        SavingsTransaction transaction = savingsTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("SavingsTransaction not found"));

        logger.info("attempting to stop transactionId: {}", transactionId);
        /*  check if the transaction is active */
        if (!transaction.getStatus().equals("ACTIVE")) {
            throw new RuntimeException("Transaction is not active");
        }
        /* retrieve the account from the repository, throw an exception if not found */
        Account account = accountRepository.findById(transaction.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));
        /* calculate the time passed since the transaction started */
        Timestamp now = new Timestamp(System.currentTimeMillis());
        long millisecondsPassed = now.getTime() - transaction.getStartDate().getTime();
        long minutesPassed = millisecondsPassed / 60000; // 60 seconds * 1000 milliseconds
        /* get the principal amount */
        double principal = transaction.getAmount();

        /* calculate compound interest using the formula */
        double compoundedAmount = principal * Math.pow((1 + transaction.getInterestRate()), minutesPassed);

        /* update the account balance with the compounded amount */
        logger.info("final compoundedAmount: {}", compoundedAmount);
        account.setBalance(account.getBalance() + compoundedAmount);
        accountRepository.save(account);
        /* Update the transaction's end date and status */
        transaction.setEndDate(now);
        transaction.setStatus("COMPLETED");
        savingsTransactionRepository.save(transaction);
    }
}
