package com.bankingapp.backend.service;

import com.bankingapp.backend.controller.FavouritePayeeController;
import com.bankingapp.backend.model.Customer;
import com.bankingapp.backend.model.FavouritePayee;
import com.bankingapp.backend.repository.CustomerRepository;
import com.bankingapp.backend.repository.FavouritePayeeRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavouritePayeeService {

    private static final Logger logger = LoggerFactory.getLogger(FavouritePayeeService.class);

    @Autowired
    private FavouritePayeeRepository favouritePayeeRepository;

    @Autowired
    private CustomerRepository customerRepository;


    public boolean isAuthorizedToModify(int authenticatedCustomerId, Long payeeId) {
        // Retrieve the payee's associated customer ID from the database
        FavouritePayee payee = favouritePayeeRepository.findById(payeeId).orElse(null);
        if (payee == null) {
            return false;
        }

        int customerId = payee.getCustomer().getCustomerId();
        return authenticatedCustomerId == customerId;
    }

    public List<FavouritePayee> getFavouritePayees(int customerId) {
        return favouritePayeeRepository.findByCustomer_CustomerId(customerId);
    }
    @Transactional
    public FavouritePayee addFavouritePayee(FavouritePayee favouritePayee) {
        return favouritePayeeRepository.save(favouritePayee);
    }
    @Transactional
    public void removeFavouritePayee(Long payeeId) {
        favouritePayeeRepository.deleteById(payeeId);
    }
}
