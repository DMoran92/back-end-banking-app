package com.bankingapp.backend.controller;

import com.bankingapp.backend.model.Account;
import com.bankingapp.backend.model.Customer;
import com.bankingapp.backend.model.Transaction;
import com.bankingapp.backend.repository.AccountRepository;
import com.bankingapp.backend.repository.CustomerRepository;
import com.bankingapp.backend.service.MailService;
import com.bankingapp.backend.service.NewAccountServiceImpl;
import com.bankingapp.backend.service.TransactionService;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
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
    @Autowired
    private NewAccountServiceImpl newAccountServiceImpl;

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getCustomerDashboard() throws MailjetSocketTimeoutException, MailjetException {
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
        response.put("accounts",accounts);
        response.put("accountTransactions", accountTransactions);

        List<Account> customerIbanAccounts = customer.getAccounts();

        for (Account account : customerIbanAccounts) {
            if (account.getIban() == null) {
                newAccountServiceImpl.makeIban(account);
            }
            else{
                System.out.println("THIS IS YOUR IBAN: "+account.getIban());
            }
        }

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
    
    @PostMapping("/makeAccount/")
    public String makeAccount(@RequestBody Map<String, Object> payload){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        Customer customer = customerRepository.findByUsername(username);
        int customerId = customer.getCustomerId();

        logger.info("Your payload: {}", payload.toString());
        String accountType = payload.get("accountType").toString();
        logger.info("accountType: {}", accountType);

        Account newAccount = new Account(customerId, accountType, 0);
        newAccountServiceImpl.addNewAccount(newAccount);

        return "redirect:/dashboard";
    }

    @PostMapping("/contactUs/")
    public String contactUs(@RequestBody Map<String, Object> payload) throws MailjetSocketTimeoutException, MailjetException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        Customer customer = customerRepository.findByUsername(username);
        int customerId = customer.getCustomerId();
        String firstName = customer.getFirstName();
        String lastName = customer.getLastName();
        String email = customer.getEmail();

        MailService ms = new MailService();

        logger.info("Your payload: {}", payload.toString());
        String mailText = payload.get("mailText").toString();
        logger.info("mailText: {}", mailText);

        ms.sendMail(mailText, username, firstName, lastName, customerId);
        return "redirect:/dashboard";
    }
}
