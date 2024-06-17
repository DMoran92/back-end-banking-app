package com.bankingapp.backend.repository;

import com.bankingapp.backend.model.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long> {
    List<PaymentCard> findByCustomer_CustomerId(int customerId);
}
