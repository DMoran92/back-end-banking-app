package com.bankingapp.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class WelcomeController {

    @GetMapping("/")
    public String main() {
        return "welcome"; //view
    }

    @GetMapping("/dashboard")
    public String showLanding(Model model) {
        return "dashboard_bootstrap";
    }
}


