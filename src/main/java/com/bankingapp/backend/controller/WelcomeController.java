package com.bankingapp.backend.controller;

import com.bankingapp.backend.model.Account;
import com.bankingapp.backend.model.Customer;
import com.bankingapp.backend.model.Transaction;
import com.bankingapp.backend.service.NewAccountService;
import com.bankingapp.backend.service.NewUserService;
import com.bankingapp.backend.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Controller
public class WelcomeController {

    @Autowired
    private NewAccountService newacc;

    @Autowired
    private NewUserService newuser;

    /* bandaid*/
    private int countryId = 0;

    @GetMapping("/")
    public String showRegistration(Model model) {
        Customer customer = new Customer();
        model.addAttribute("customer", customer);

        List<String> listCountyState = Arrays.asList("Dublin, Ireland", "Galway, Ireland", "Cork, Ireland");
        model.addAttribute("listCountyState", listCountyState);

        return "welcome";
    }

    @PostMapping("/")
    public String addCustomer(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam int phoneNumber,
            @RequestParam String dob,
            @RequestParam String addrLine1,
            @RequestParam String addrLine2,
            @RequestParam String townCity,
            @RequestParam String countyState,
            @RequestParam String password,
            @RequestParam String idType,
            @RequestParam String idNumber) {


        Customer newCustomer = new Customer(firstName,lastName, email, phoneNumber, countryId, addrLine1, addrLine2, townCity, countyState, password, idType, idNumber, dob);

        /* this should return failure  if it was unsucessful to create new user */
        newuser.addNewCustomer(newCustomer);

        //create a new account for the user
        int accountCustomerId = newacc.getCustomerIdForAccount();
        Account newAccount = new Account(accountCustomerId, "Current", 0);
        newacc.addNewAccount(newAccount);
        return "redirect:/";
    }

    @GetMapping("/debug-list")
    public String getAllCustomers(Model model) {
        List<Customer> customers = newuser.getAllCustomers();
        model.addAttribute("customers", customers);
        return "debug-list";
    }

    @GetMapping("/dashboard")
    public String showLanding(Model model) {
        return "dashboard_bootstrap";
    }
}


