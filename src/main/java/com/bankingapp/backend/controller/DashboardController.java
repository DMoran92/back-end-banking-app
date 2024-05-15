package com.bankingapp.backend.controller;

import com.bankingapp.backend.model.Account;
import com.bankingapp.backend.model.Customer;
import com.bankingapp.backend.model.Transaction;
import com.bankingapp.backend.repository.AccountRepository;
import com.bankingapp.backend.service.NewAccountService;
import com.bankingapp.backend.service.NewUserService;
import com.bankingapp.backend.service.TransactionService;
import com.bankingapp.backend.service.TransactionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.*;

@Controller
public class DashboardController {

    List<Account> accountList = new ArrayList<>();

    @Autowired
    private NewUserService newuser;

    @Autowired
    private NewAccountService newacc;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountRepository accountRepository;

    @RequestMapping("/dashboard/")
    public String getCustomerDashboard(@RequestParam(value="CustomerId") int customerId, Model model){

        //User makes new transaction in form
        Transaction transaction = new Transaction();

        //Get account details for the customer
        List<Customer> customers = newuser.getAccountDetails(customerId);
        List<Account> accounts = newacc.getTransactions(customers.get(0).getAccounts().get(0).getAccountId());

        //Get the accounts list of outgoing transactions
        List<Transaction> accountTransactions = accounts.get(0).getTransactions();
        Collections.reverse(accountTransactions); //reversed list so latest transaction is on top

        model.addAttribute("accountTransactions", accountTransactions);
        model.addAttribute("CustomerId", customerId);
        model.addAttribute("customers", customers);
        model.addAttribute("transaction", transaction);
        return "dashboard";
    }

    @GetMapping("/debug-list")
    public String testSpec(Model model) {
        //List<Customer> customers = newuser.getAccountDetails();
        //Customer test = new Customer();
        //double aaa = test.getAccounts().get(0).getBalance();
        //List<Account> accounts = newacc.getBalance();
        //model.addAttribute("accounts", accounts);
        //model.addAttribute("customers", customers);
        return "dashboard";
    }

    @PostMapping("/makeTransaction/")
    public String makeTransaction(@RequestParam double amount, @RequestParam long accountId, @RequestParam int recipientId){

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
        transactionService.newTransaction(transaction);
        return "welcome";
    }
}
