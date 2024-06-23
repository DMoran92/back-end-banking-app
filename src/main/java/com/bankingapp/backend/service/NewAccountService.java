package com.bankingapp.backend.service;

import com.bankingapp.backend.model.Account;

import java.util.List;
import java.util.Optional;

public interface NewAccountService {

    List<Account> getTransactions(long id);
    int getCustomerIdForAccount();
    Account addNewAccount(Account account);
    Optional<Account> makeIban(Account account);

}
