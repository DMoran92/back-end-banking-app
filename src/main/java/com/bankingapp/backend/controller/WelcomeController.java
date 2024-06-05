package com.bankingapp.backend.controller;

import com.bankingapp.backend.model.Customer;
import com.bankingapp.backend.service.NewUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Controller
public class WelcomeController {

    @Autowired
    private NewUserService newuser;

    @GetMapping("/")
    public String showRegistration(Model model) {
        Customer customer = new Customer();
        model.addAttribute("customer", customer);

        List<String> listCountyState = Arrays.asList("Dublin, Ireland", "Galway, Ireland", "Cork, Ireland");
        model.addAttribute("listCountyState", listCountyState);

        return "welcome";
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


