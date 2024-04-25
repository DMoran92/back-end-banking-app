package com.bankingapp.backend.service;

import com.bankingapp.backend.repository.CustomerRepository;
import com.bankingapp.backend.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NewUserServiceImpl  implements NewUserService {

    @Autowired
    private final CustomerRepository customerRepository;

    @Autowired
    public NewUserServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /* add new customer to the database */
    @Override
    public Customer addNewUser(Customer customer) {
        /* TODO: check if user exists based on custer idType and idNumber, decline if he already has an account */
        return customerRepository.save(customer);
    }
    /* return list of customers */
    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
}
