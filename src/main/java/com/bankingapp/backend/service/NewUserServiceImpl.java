package com.bankingapp.backend.service;

import com.bankingapp.backend.repository.CustomerRepository;
import com.bankingapp.backend.model.Customer;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
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
/*
    /* add new customer to the database */
    @Override
    @Transactional
    public Customer addNewCustomer(Customer customer) {
        return customerRepository.save(customer);
    }
    /* return list of customers */
    @Override
    @Transactional
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    //get account details for a customer
    @Override
    @Transactional
    public List<Customer> getAccountDetails(int id) {
        Specification<Customer> isAcc = CustomerSpecifications.accountDetails(id);
        return customerRepository.findAll(isAcc);
    }
}
