package com.bankingapp.backend.repository;

import com.bankingapp.backend.model.SavingsTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SavingsTransactionRepository extends JpaRepository<SavingsTransaction, Long> {
    List<SavingsTransaction> findByAccountId(Long accountId);
}
