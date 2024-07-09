package com.bankingapp.backend.repository;

import com.bankingapp.backend.model.Customer;
import com.bankingapp.backend.model.TwoFactorAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TwoFactorAuthRepository extends JpaRepository<TwoFactorAuth, Long> {
    TwoFactorAuth findByCustomer(Customer customer);
}
