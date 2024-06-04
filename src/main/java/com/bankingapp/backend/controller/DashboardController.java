package com.bankingapp.backend.controller;

import com.bankingapp.backend.model.Account;
import com.bankingapp.backend.model.Customer;
import com.bankingapp.backend.model.Transaction;
import com.bankingapp.backend.repository.AccountRepository;
import com.bankingapp.backend.repository.CustomerRepository;
import com.bankingapp.backend.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.*;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);


    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountRepository accountRepository;


    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getCustomerDashboard()  {
        logger.info("Entering getCustomerDashboard");

        /* get the authenticated user's username */
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            username = userDetails.getUsername();
            logger.info("Authenticated user: {}", username);
        } else {
            logger.warn("No authenticated user found");
            throw new RuntimeException("No authenticated user found");
        }
        /* retrieve the customer using the username */
        Customer customer = customerRepository.findByUsername(username);
        if (customer == null) {
            logger.error("No customer found with username: {}", username);
            throw new RuntimeException("No customer found with username: " + username);
        }
        /* Retrieve the accounts and transactions for the customer */
        List<Account> accounts = customer.getAccounts();
        List<Transaction> accountTransactions = new ArrayList<>();
        if (!accounts.isEmpty()) {
            accountTransactions = accounts.get(0).getTransactions();
            Collections.reverse(accountTransactions); // Reverse to show latest transaction first
        }

        logger.info("Customer: {}, Accounts: {}, Transactions: {}", customer, accounts, accountTransactions);

        /* prep the response with customer object and transactions */
        Map<String, Object> response = new HashMap<>();
        response.put("customer", customer);
        response.put("accountTransactions", accountTransactions);
        return ResponseEntity.ok(response);
    }
    /* Exception handler that will catch all errors in DashboardController and return 500 with an error message
     * Probably to be handled properly on the frontend*/
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", e.getMessage());
        return ResponseEntity.status(500).body(errorResponse); // Returning 500 Internal Server Error

    }
    /*
    NOTE: this prop will need to be reworked when we start working with transactions on the new dashboard */
    @PostMapping("/makeTransaction/")
    public String makeTransaction(@RequestParam double amount, @RequestParam long accountId, @RequestParam int recipientId){

        /* auth missing here */

        //Make new transaction, get sender and recipient id, along with transaction time and date
        Transaction transaction = new Transaction();
        Optional<Account> opSender = accountRepository.findById(accountId);
        Optional<Account> opRecipient = accountRepository.findById((long) recipientId);
        Account sender = opSender.get();
        Account recipient = opRecipient.get();
        Date date = new Date();

        //set transaction info
        transaction.setAccountId(accountId);
        transaction.setRecipientId(recipientId);
        transaction.setAmount(amount);
        transaction.setTimestamp(new Timestamp(date.getTime()).toString());

        //call methods to update the database
        transactionService.sendMoney(sender, transaction, accountId);
        transactionService.receiveMoney(recipient, transaction, recipientId);
        transactionService.makeNewTransaction(transaction);
        return "redirect:/";
    }
}
