package com.bankingapp.backend.service;

import com.bankingapp.backend.model.Account;

import java.util.List;

public interface NewAccountService {

    //List<Account> getAllAccounts();
    //List<Account> getBalance();
    List<Account> getTransactions(long id);

}
