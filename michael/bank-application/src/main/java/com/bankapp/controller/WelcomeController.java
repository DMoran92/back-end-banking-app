package com.bankapp.controller;

import com.bankapp.Customer;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;

@Controller
public class WelcomeController {

    //TomcatServletWebServerFactory
    // inject via application.properties
    //@Value("${welcome.message}")
    private String message = "default name";

    // private List<String> tasks = Arrays.asList("a", "b", "c", "d", "e", "f", "g");

    @GetMapping("/")
    public String main(Model model) {
        model.addAttribute("message", message);
        //model.addAttribute("tasks", tasks);

        return "welcome"; //view
    }

    // /hello?name=kotlin
    @GetMapping("/hello")
    public String mainWithParam(

            @RequestParam(name = "name", required = false, defaultValue = "default name") String name, Model model) {

        model.addAttribute("message", name);

        return "welcome"; //view
    }

    @GetMapping("/register")
    public String showForm(Model model) {
        Customer customer = new Customer();
        model.addAttribute("customer", customer);

        List<String> listCountyState = Arrays.asList("Dublin, Ireland", "Galway, Ireland", "Cork, Ireland");
        model.addAttribute("listCountyState", listCountyState);

        return "registration";
    }

}