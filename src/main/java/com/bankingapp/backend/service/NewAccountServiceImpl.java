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

    //Specification<Account> hasBal = AccountSpecifications.custAccount(1);

    @Autowired
    public NewAccountServiceImpl( AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /*@Override
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    public List<Account> getBalance() {
        return accountRepository.findAll();
    }*/

    //get the transactions for an account
    @Override
    public List<Account> getTransactions(long id) {
        Specification<Account> isTransaction = AccountSpecifications.accountTransactions(id);
        return accountRepository.findAll(isTransaction);
    }
}
