package com.bankingapp.backend.repository;

import com.bankingapp.backend.model.FavouritePayee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavouritePayeeRepository extends JpaRepository<FavouritePayee, Long> {
    List<FavouritePayee> findByCustomer_CustomerId(int customerId);
}
