package com.bankingapp.backend.service;

import com.bankingapp.backend.model.Customer;
import com.bankingapp.backend.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomerDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerDetailsService.class);

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Attempting to load user by username: {}", username);
        Customer customer = customerRepository.findByUsername(username);
        if (customer == null) {
            logger.error("User not found with username: {}", username);
            throw new UsernameNotFoundException("User not found");
        }
        logger.info("User found: {}. Creating UserDetails object.", customer.getUsername());
        return new User(customer.getUsername(), customer.getPassword(), Collections.emptyList());
    }
}
