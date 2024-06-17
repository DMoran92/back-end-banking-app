package com.bankingapp.backend.controller;

import com.bankingapp.backend.model.Customer;
import com.bankingapp.backend.model.FavouritePayee;
import com.bankingapp.backend.repository.CustomerRepository;
import com.bankingapp.backend.service.FavouritePayeeService;
import com.bankingapp.backend.utilities.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/favourite-payees")
public class FavouritePayeeController {

    private static final Logger logger = LoggerFactory.getLogger(FavouritePayeeController.class);

    @Autowired
    private FavouritePayeeService favouritePayeeService;

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping
    public List<Map<String, Object>> getFavouritePayees() {
        /* get the authenticated user's username */
        String username = Utils.getAuthenticatedUsername();
        Customer customer = customerRepository.findByUsername(username);
        List<FavouritePayee> favouritePayees = favouritePayeeService.getFavouritePayees(customer.getCustomerId());
        // ok im not sure if this is ok to do it this way. The problem is the Favourite payee entity contains
        // both the FavouritePayees Entity AND Customer entity (joined tables). It was mistake to join them maybe?
        // The entire object is quite large now and if we just pass it there is a lot of data that was already received
        // at the frontend while initial loading. I want to only stream the id name and iban without creating another DTO
        // so I did it this way (don't know how bad this is D:).
        //return ResponseEntity.ok(payees);
        return favouritePayees.stream().map(payee -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", payee.getId());
            map.put("name", payee.getName());
            map.put("iban", payee.getIban());
            return map;
        }).collect(Collectors.toList());
    }

    @PostMapping("/add")
    public ResponseEntity<FavouritePayee> addFavouritePayee(@RequestBody FavouritePayee favouritePayee, @RequestParam int customerId) {
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new RuntimeException("Customer not found"));
        favouritePayee.setCustomer(customer);
        FavouritePayee savedPayee = favouritePayeeService.addFavouritePayee(favouritePayee);
        return ResponseEntity.ok(savedPayee);
    }

    @DeleteMapping("/remove")
    public ResponseEntity<String> removeFavouritePayee(@RequestParam Long payeeId) {
        // this is a remove call, checking if ht customerID was assigned to the payeeId, otherwise deny the request
        /* get the authenticated user's username */
        String username = Utils.getAuthenticatedUsername();
        /* retrieve the customer using the username */
        Customer customer = customerRepository.findByUsername(username);
        if (customer == null) {
            logger.error("No customer found with username: {}", username);
            throw new RuntimeException("No customer found with username: " + username);
        }

        logger.info("Attempting to remove payee with ID: {}", payeeId);
        // Check if the authenticated user is authorized to remove the payee
        if (!favouritePayeeService.isAuthorizedToModify(customer.getCustomerId(), payeeId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized to remove this payee.");
        }
        /* user authenticated and correct user is trying to remove the payee, try the operation on db */
        try {
            favouritePayeeService.removeFavouritePayee(payeeId);
            return ResponseEntity.ok("Payee removed successfully");
        } catch (Exception e) {
            logger.error("ERROR REMOVING PAYEE{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    "Unable to remove payee, try again later");
        }
    }
}
