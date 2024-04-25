package com.bankingapp.backend.controller;

import com.bankingapp.backend.model.Customer;
import com.bankingapp.backend.service.NewUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;

@Controller
public class RegisterController {
    @Autowired
    private NewUserService newuser;
    /* bandaid*/
    private int countryId = 0;

    @GetMapping("/register")
    public String showRegistration(Model model) {
        Customer customer = new Customer();
        model.addAttribute("customer", customer);

        List<String> listCountyState = Arrays.asList("Dublin, Ireland", "Galway, Ireland", "Cork, Ireland");
        model.addAttribute("listCountyState", listCountyState);

        return "registration";
    }

    @PostMapping("/register")
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
        newuser.addNewUser(newCustomer);

        return "redirect:/debug-list";
    }

    @GetMapping("/debug-list")
    public String getAllCustomers(Model model) {
        List<Customer> customers = newuser.getAllCustomers();
        model.addAttribute("customers", customers);
        return "debug-list";
    }
}
