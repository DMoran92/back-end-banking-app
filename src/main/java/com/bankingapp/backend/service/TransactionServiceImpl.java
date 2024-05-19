package com.bankingapp.backend.service;

import com.bankingapp.backend.model.Account;
import com.bankingapp.backend.model.Transaction;
import com.bankingapp.backend.repository.AccountRepository;
import com.bankingapp.backend.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public TransactionServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional
    public Optional<Account> sendMoney(Account account, Transaction transaction, long accountId) {
        return accountRepository.findById(accountId).map(target -> {
            target.setBalance(account.getBalance() - transaction.getAmount());
            return target;
        });
    }

    @Override
    @Transactional
    public Optional<Account> receiveMoney(Account account, Transaction transaction, long accountId) {
        return accountRepository.findById(accountId).map(target -> {
            target.setBalance(account.getBalance() + transaction.getAmount());
            return target;
        });
    }

    @Override
    public Transaction newTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }
}
