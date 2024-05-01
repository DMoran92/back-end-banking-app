package com.bankingapp.backend.service;

import com.bankingapp.backend.model.Customer;

import java.util.List;

public interface NewUserService {

    Customer addNewCustomer(Customer customer);

    public List<Customer> getAllCustomers();

}
