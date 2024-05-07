package com.bankingapp.backend.service;

import com.bankingapp.backend.model.Account;
import org.springframework.data.jpa.domain.Specification;

public class AccountSpecifications {


    public static Specification<Account> custAccount(int id){
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("customerId"), id);
    }

}
