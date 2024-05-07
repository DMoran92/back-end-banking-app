package com.bankingapp.backend.controller;

import com.bankingapp.backend.model.Account;
import com.bankingapp.backend.model.Customer;
import com.bankingapp.backend.service.NewAccountService;
import com.bankingapp.backend.service.NewUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class DashboardController {
    @Autowired
    private NewUserService newuser;

    @Autowired
    private NewAccountService newacc;

    @RequestMapping("/dashboard/")
    public String getCustomerDashboard(@RequestParam(value="CustomerId") int customerId, Model model){
        List<Customer> customers = newuser.getAccountDetails(customerId);
        model.addAttribute("CustomerId", customerId);
        model.addAttribute("customers", customers);
        //model.addAttribute("accounts", accounts);
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
}
