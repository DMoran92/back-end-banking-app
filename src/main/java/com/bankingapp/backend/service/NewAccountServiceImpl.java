package com.bankingapp.backend.service;

import com.bankingapp.backend.model.Account;
import com.bankingapp.backend.model.Customer;
import com.bankingapp.backend.repository.AccountRepository;
import com.bankingapp.backend.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

@Service
public class NewAccountServiceImpl  implements NewAccountService {

    @Autowired
    private final AccountRepository accountRepository;

    @Autowired
    private final CustomerRepository customerRepository;

    @Autowired
    public NewAccountServiceImpl( AccountRepository accountRepository, CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
    }

    //get the transactions for an account
    @Override
    public List<Account> getTransactions(long id) {
        Specification<Account> isTransaction = AccountSpecifications.accountTransactions(id);
        return accountRepository.findAll(isTransaction);
    }

    @Override
    public int getCustomerIdForAccount() {
        int index = -1;
        List<Customer> lastCustomer = customerRepository.findAll();

        for (int i = 0; i <lastCustomer.size(); i++){
            index++;
            System.out.println(index);
        }

        return lastCustomer.get(index).getCustomerId();
    }

    @Override
    public Account addNewAccount(Account account) {
        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public Optional<Account> makeIban(Account account) {
        int checkNum = (int) (account.getAccountId() % 97);
        return accountRepository.findById(account.getAccountId()).map(target -> {
            System.out.println("Setting new IBAN for " + account.getAccountId());
            System.out.println("IE"+(new DecimalFormat("00").format(checkNum)+"BOGY"+913322+(new DecimalFormat("00000000").format(account.getAccountId()))));
            target.setIban("IE"+(new DecimalFormat("00").format(checkNum)+"BOGY"+913322+(new DecimalFormat("00000000").format(account.getAccountId()))));
            return target;
        });
    }


}
