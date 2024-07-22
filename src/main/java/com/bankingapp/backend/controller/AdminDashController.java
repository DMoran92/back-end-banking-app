package com.bankingapp.backend.controller;

import com.bankingapp.backend.model.Account;
import com.bankingapp.backend.model.Customer;
import com.bankingapp.backend.model.Transaction;
import com.bankingapp.backend.repository.AccountRepository;
import com.bankingapp.backend.repository.CustomerRepository;
import com.bankingapp.backend.service.*;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.*;

@Controller
public class AdminDashController {

    @Autowired
    private NewUserServiceImpl newUserServiceImpl;

    @GetMapping("/dashboard_admin")
    public String getAllAccounts(Model model) {
        List<Customer> customers = newUserServiceImpl.getAllCustomers();

        model.addAttribute("customers", customers);
        return "dashboard_admin";
    }

    /* Exception handler that will catch all errors in DashboardController and return 500 with an error message
     * Probably to be handled properly on the frontend*/
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", e.getMessage());
        return ResponseEntity.status(500).body(errorResponse); // Returning 500 Internal Server Error

    }
}
