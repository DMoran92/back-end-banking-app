package com.bankingapp.backend.service;

import com.bankingapp.backend.model.Account;
import com.bankingapp.backend.model.Customer;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class CustomerSpecifications {

    //joins account and customer table on customerId
    public static Specification<Customer> accountDetails(int id){
        return (root, query, criteriaBuilder) -> {
            Join<Customer, Account> custAccount = root.join("accounts");
            return criteriaBuilder.equal(custAccount.get("customerId"), id);
        };
    }
}
