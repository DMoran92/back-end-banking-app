package com.bankingapp.backend.controller;

import com.bankingapp.backend.model.Account;
import com.bankingapp.backend.model.Customer;
import com.bankingapp.backend.model.Transaction;
import com.bankingapp.backend.repository.AccountRepository;
import com.bankingapp.backend.repository.CustomerRepository;
import com.bankingapp.backend.service.NewAccountServiceImpl;
import com.bankingapp.backend.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);


    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountRepository accountRepository;


    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private NewAccountServiceImpl newAccountServiceImpl;

    @PostMapping("/makeTransaction/")
    public String makeTransaction(@RequestBody Map<String, Object> payload){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        Customer customer = customerRepository.findByUsername(username);
        List<Account> accounts = customer.getAccounts();
        long accountId = accounts.get(0).getAccountId();
        String senderIBAN = accounts.get(0).getIban();


        logger.info("Your payload: {}", payload.toString());
        double amount = Double.parseDouble(payload.get("amount").toString());
        String recipientIBAN = payload.get("recipientIBAN").toString();
        logger.info("amount: {}, accountId: {}, recipientIBAN: {}, senderIBAN: {}", amount, accountId, recipientIBAN, senderIBAN);

        //Make new transaction, get sender and recipient id, along with transaction time and date
        Transaction transactionOut = new Transaction();
        Transaction transactionIn = new Transaction();
        Optional<Account> opSender = accountRepository.findById(accountId);
        List<Account> opRecipient = accountRepository.findByIban(recipientIBAN);

        //check if recipient IBAN is in the system
        if (opRecipient.isEmpty()) {
            System.out.println("No recipient found for IBAN: " + recipientIBAN);

            //Create transaction for sender
            Account sender = opSender.get();
            Date date = new Date();

            transactionOut.setAccountId(sender.getAccountId());
            transactionOut.setRecipientIBAN(recipientIBAN);
            transactionOut.setAmount(amount);
            transactionOut.setTimestamp(new Timestamp(date.getTime()).toString());

            transactionService.sendMoney(sender, transactionOut, accountId);
            transactionService.makeNewTransaction(transactionOut);
        }
        else{
            System.out.println("Recipient found for IBAN: " + recipientIBAN);

            Account sender = opSender.get();
            Account recipient = opRecipient.get(0);
            long recipientId = recipient.getAccountId();
            Date date = new Date();

            //set transaction info for sender
            transactionOut.setAccountId(sender.getAccountId());
            transactionOut.setRecipientIBAN(recipientIBAN);
            transactionOut.setAmount(amount);
            transactionOut.setTimestamp(new Timestamp(date.getTime()).toString());

            //set transaction info for recipient
            transactionIn.setAccountId(recipient.getAccountId());
            transactionIn.setSenderIBAN(senderIBAN);
            transactionIn.setAmount(amount);
            transactionIn.setTimestamp(new Timestamp(date.getTime()).toString());

            //call methods to update the database
            transactionService.sendMoney(sender, transactionOut, accountId);
            transactionService.receiveMoney(recipient, transactionOut, recipientId);
            transactionService.makeNewTransaction(transactionOut);
            transactionService.makeNewTransaction(transactionIn);
        }

        return "redirect:/";
    }
}
