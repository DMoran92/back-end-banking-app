package com.bankingapp.backend.service;

import com.bankingapp.backend.model.Account;
import com.bankingapp.backend.model.Transaction;

import java.util.List;
import java.util.Optional;

public interface TransactionService {
    Optional<Account> sendMoney(Account account, Transaction transaction, long accountId);
    Optional<Account> receiveMoney(Account account, Transaction transaction, long accountId);
    Transaction  makeNewTransaction(Transaction transaction);
}
