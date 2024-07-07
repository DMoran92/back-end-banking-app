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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.bankingapp.backend.utilities.Utils.getAuthenticatedUsername;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @PostMapping("/makeTransaction/")
    public ResponseEntity<String> makeTransaction(@RequestBody Map<String, Object> payload){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        Customer customer = customerRepository.findByUsername(username);
        long accountId = Long.parseLong(payload.get("accountId").toString());
        Optional<Account> opSender = customer.getAccounts().stream().filter(acc -> acc.getAccountId() == accountId).findFirst();

        if (!opSender.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Account not found");
        }

        Account senderAccount = opSender.get();
        String senderIBAN = senderAccount.getIban();

        logger.info("Your payload: {}", payload.toString());
        double amount = Double.parseDouble(payload.get("amount").toString());
        String recipientIBAN = payload.get("recipientIBAN").toString();
        String category = payload.get("category").toString();
        String recipientName = payload.getOrDefault("recipientName", "Unknown").toString();

        logger.info("amount: {}, accountId: {}, recipientIBAN: {}, senderIBAN: {}, category: {}", amount, accountId, recipientIBAN, senderIBAN, category);


        // Check if the sender has sufficient balance
        if (senderAccount.getBalance() < amount) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Insufficient balance");
        }

        //Make new transaction, get sender and recipient id, along with transaction time and date
        Transaction transactionOut = new Transaction();
        Transaction transactionIn = new Transaction();
        List<Account> opRecipient = accountRepository.findByIban(recipientIBAN);

        //check if recipient IBAN is in the system
        if (opRecipient.isEmpty()) {
            System.out.println("No recipient found for IBAN: " + recipientIBAN);

            //Create transaction for sender
            Date date = new Date();

            transactionOut.setAccountId(senderAccount.getAccountId());
            transactionOut.setRecipientIBAN(recipientIBAN);
            transactionOut.setAmount(amount);
            transactionOut.setTimestamp(new Timestamp(date.getTime()).toString());
            transactionOut.setCategory(category);
            transactionOut.setRecipientName(recipientName);
            transactionOut.setSenderIBAN(senderIBAN);
            transactionOut.setSenderName(customer.getFirstName() + " " + customer.getLastName());
            logger.info("Your sender: {}, transactionOut: {}, accountId: {}", senderAccount, transactionOut, accountId);
            transactionService.sendMoney(senderAccount, transactionOut, accountId);
            transactionService.makeNewTransaction(transactionOut);
        }
        else{
            System.out.println("Recipient found for IBAN: " + recipientIBAN);

            Account recipient = opRecipient.get(0);
            long recipientId = recipient.getAccountId();
            Date date = new Date();

            //set transaction info for sender
            transactionOut.setAccountId(senderAccount.getAccountId());
            transactionOut.setRecipientName(recipientName);
            transactionOut.setRecipientIBAN(recipient.getIban());
            transactionOut.setAmount(amount);
            transactionOut.setTimestamp(new Timestamp(date.getTime()).toString());
            transactionOut.setSenderIBAN(senderIBAN);
            transactionOut.setCategory(category);
            transactionOut.setSenderName(customer.getFirstName() + " " + customer.getLastName());


            //set transaction info for recipient
            transactionIn.setAccountId(recipient.getAccountId());
            transactionIn.setRecipientName(recipientName);
            transactionIn.setRecipientIBAN(recipient.getIban());
            transactionIn.setRecipientIBAN(recipientIBAN);
            transactionIn.setSenderIBAN(senderIBAN);
            transactionIn.setAmount(amount);
            transactionIn.setTimestamp(new Timestamp(date.getTime()).toString());
            transactionIn.setSenderName(customer.getFirstName() + " " + customer.getLastName());
            transactionIn.setCategory(category);

            //call methods to update the database
            logger.info("Your sender: {}, transactionOut: {}, accountId: {}", senderAccount, transactionOut, accountId);
            logger.info("Your recipient: transactionIn: {}, accountId: {}", transactionIn, accountId);
            transactionService.sendMoney(senderAccount, transactionOut, accountId);
            transactionService.receiveMoney(recipient, transactionOut, recipientId);
            transactionService.makeNewTransaction(transactionOut);
            transactionService.makeNewTransaction(transactionIn);
        }
        return ResponseEntity.ok("Transaction successful");
    }

    @PostMapping("/deposit")
    public ResponseEntity<String> deposit(@RequestBody Map<String, Object> payload) {

        /* get customer based on the username */
        String username = getAuthenticatedUsername();
        Customer customer = customerRepository.findByUsername(username);

        /* extract account ID and amount from the payload */
        long accountId = Long.parseLong(payload.get("accountId").toString());
        double amount = Double.parseDouble(payload.get("amount").toString());
        logger.info("accountId: {}, amount: {}", accountId, amount);
        /* Find the account using account ID */
        Optional<Account> opAccount = customer.getAccounts().stream().filter(acc -> acc.getAccountId() == accountId).findFirst();
        if (opAccount.isPresent()) {
            /* perform deposit */
            transactionService.deposit(opAccount.get(), amount);
            logger.info("Deposit successful");

            /* Create transaction */
            Transaction transaction = new Transaction();
            transaction.setAccountId(accountId);
            transaction.setRecipientName(customer.getFirstName() + " " + customer.getLastName());
            transaction.setRecipientIBAN(opAccount.get().getIban());
            transaction.setSenderName("Cash Deposit");
            transaction.setSenderIBAN(opAccount.get().getIban());
            transaction.setAmount(amount);
            transaction.setTimestamp(new Timestamp(new Date().getTime()).toString());
            transaction.setCategory("Cash Deposit");
            transactionService.makeNewTransaction(transaction);

            return ResponseEntity.ok("Deposit successful");
        } else {
            logger.info("Deposit failed");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found");
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(@RequestBody Map<String, Object> payload) {

        /* get customer based on the username */
        String username = getAuthenticatedUsername();
        Customer customer = customerRepository.findByUsername(username);
        /* extract account ID and amount from the payload */
        long accountId = Long.parseLong(payload.get("accountId").toString());
        double amount = Double.parseDouble(payload.get("amount").toString());
        logger.info("accountId: {}, amount: {}", accountId, amount);
        /* Find the account using account ID */
        Optional<Account> opAccount = customer.getAccounts().stream().filter(acc -> acc.getAccountId() == accountId).findFirst();
        if (opAccount.isPresent()) {
            try {
                /* perform withdrawal */
                transactionService.withdraw(opAccount.get(), amount);
                logger.info("Withdraw successfull");

                /* Create transaction */
                Transaction transaction = new Transaction();
                transaction.setAccountId(accountId);
                transaction.setRecipientName("Cash Withdrawal");
                transaction.setRecipientIBAN(opAccount.get().getIban());
                transaction.setAmount(amount);
                transaction.setTimestamp(new Timestamp(new Date().getTime()).toString());
                transaction.setCategory("Cash Withdrawal");
                transactionService.makeNewTransaction(transaction);

                return ResponseEntity.ok("Withdrawal successful");
            } catch (RuntimeException e) {
                logger.info("Withdraw failed: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
        } else {
            logger.info("Withdraw failed");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found");
        }
    }
}
