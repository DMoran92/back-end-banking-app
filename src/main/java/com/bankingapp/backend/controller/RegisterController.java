package com.bankingapp.backend.controller;

import com.bankingapp.backend.model.Account;
import com.bankingapp.backend.model.Customer;
import com.bankingapp.backend.repository.CustomerRepository;
import com.bankingapp.backend.service.NewAccountService;
import com.bankingapp.backend.service.NewUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api") // Add base request mapping for better URL management
public class RegisterController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private NewUserService newUserService;

    @Autowired
    private NewAccountService newacc;

    @PostMapping("/register")
    public Map<String, String> addCustomer(@RequestBody Customer customer) {
        // Generate a unique 8-digit username
        String username = generateUniqueUsername();
        customer.setUsername(username);
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));

        // Add new customer
        newUserService.addNewCustomer(customer);

        //create a new account for the user
        int accountCustomerId = newacc.getCustomerIdForAccount();
        Account newAccount = new Account(accountCustomerId, "Current", 0);
        newacc.addNewAccount(newAccount);

        // Return the generated username
        Map<String, String> response = new HashMap<>();
        response.put("username", username);
        return response;
    }

    private String generateUniqueUsername() {
        SecureRandom random = new SecureRandom();
        String username;
        do {
            // Generate a random 8-digit number
            int number = 10000000 + random.nextInt(90000000);
            username = String.valueOf(number);
        } while (customerRepository.findByUsername(username) != null);
        return username;
    }
}
