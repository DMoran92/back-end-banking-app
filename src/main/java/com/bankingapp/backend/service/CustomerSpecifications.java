package com.bankingapp.backend.service;

import com.bankingapp.backend.model.Account;
import com.bankingapp.backend.model.Customer;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class CustomerSpecifications {

    public static Specification<Customer> hasFirstNameLike(String name){
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.<String>get("firstName"), "%" + name + "%");
    }

    public static Specification<Customer> accountDetails(int id){
        return (root, query, criteriaBuilder) -> {
            Join<Customer, Account> custAccount = root.join("accounts");
            return criteriaBuilder.equal(custAccount.get("customerId"), id);
        };
    }
}
