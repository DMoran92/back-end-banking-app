package com.bankingapp.backend.service;

import com.bankingapp.backend.model.Account;
import com.bankingapp.backend.model.Customer;
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
    @Transactional
    public Optional<Account> deposit(Account account, double amount) {
        /* find the account by using accountId*/
        return accountRepository.findById(account.getAccountId()).map(target -> {
            /* update the account balance */
            target.setBalance(account.getBalance() + amount);
            return target;
        });
    }

    @Override
    @Transactional
    public Optional<Account> withdraw(Account account, double amount) {
        /* ind the account by using accountId */
        return accountRepository.findById(account.getAccountId()).map(target -> {
            /* Check if there is sufficient balance before withdrawal */
            if (target.getBalance() >= amount) {
                /* update the balance */
                target.setBalance(account.getBalance() - amount);
                return target;
            } else {
                throw new RuntimeException ("Insufficient funds");
            }
        });
    }

    @Override
    @Transactional
    public Transaction makeNewTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }
}
