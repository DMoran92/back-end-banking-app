package com.bankingapp.backend.service;

import com.bankingapp.backend.model.Account;
import com.bankingapp.backend.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NewAccountServiceImpl  implements NewAccountService {

    @Autowired
    private final AccountRepository accountRepository;

    @Autowired
    public NewAccountServiceImpl( AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    //get the transactions for an account
    @Override
    public List<Account> getTransactions(long id) {
        Specification<Account> isTransaction = AccountSpecifications.accountTransactions(id);
        return accountRepository.findAll(isTransaction);
    }
}
