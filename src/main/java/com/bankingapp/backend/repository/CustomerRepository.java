package com.bankingapp.backend.repository;

import com.bankingapp.backend.model.Customer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer>, JpaSpecificationExecutor<Customer> {
    Customer findByUsername(String username);
    Customer findByEmail(String email);
    Optional<Customer> findByIdNumber(String idNumber);
}
