package com.bankingapp.backend.service;

import com.bankingapp.backend.model.Account;
import com.bankingapp.backend.model.Customer;
import com.bankingapp.backend.model.Transaction;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class AccountSpecifications {

    //joins transaction and account tables on accountId
    public static Specification<Account> accountTransactions(long id){
        return (root, query, criteriaBuilder) -> {
            Join<Account, Transaction> accountTransactionJoin = root.join("transactions");
            return criteriaBuilder.equal(accountTransactionJoin.get("accountId"), id);
        };
    }

}
