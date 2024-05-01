package com.bankingapp.backend.controller;

import com.bankingapp.backend.model.Customer;
import com.bankingapp.backend.model.Account;
import com.bankingapp.backend.service.NewAccountService;
import com.bankingapp.backend.service.NewUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class DashboardController {
    @Autowired
    private NewUserService newuser;
    private NewAccountService newacc;

    @RequestMapping("/dashboard/")
    public String getCustomerDashboard(@RequestParam(value="CustomerId") int CustomerId, Model model){
        List<Customer> customers = newuser.getAllCustomers();
        List<Account> accounts = newacc.getAllAccounts();
        model.addAttribute("CustomerId", CustomerId);
        model.addAttribute("customers", customers);
        model.addAttribute("accounts", accounts);
        return "dashboard";
    }
}
