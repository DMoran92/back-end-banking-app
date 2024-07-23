package com.bankingapp.backend.service;

import com.bankingapp.backend.model.PasswordResetToken;
import com.bankingapp.backend.model.Customer;
import com.bankingapp.backend.repository.PasswordResetTokenRepository;
import com.bankingapp.backend.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Transactional
    public String createPasswordResetToken(String email) {
        Customer customer = customerRepository.findByEmail(email);
        if (customer == null) {
            throw new RuntimeException("Customer not found");
        }

        /* remove existing token if it exists */
        PasswordResetToken existingToken = passwordResetTokenRepository.findByCustomer(customer);
        if (existingToken != null) {
            passwordResetTokenRepository.delete(existingToken);
        }

        /* create a new token */
        String token = UUID.randomUUID().toString();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5); // token expires in 5 mins

        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setToken(token);
        passwordResetToken.setCustomer(customer);
        passwordResetToken.setExpiryDate(new Timestamp(calendar.getTimeInMillis()));

        passwordResetTokenRepository.save(passwordResetToken);
        return token;
    }
    /* check if token is valid */
    public boolean validatePasswordResetToken(String token) {
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken passwordResetToken = tokenOpt.get();
        return passwordResetToken.getExpiryDate().after(new Timestamp(System.currentTimeMillis()));
    }
    /* reset password */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        /* make sure token is actually valid first */
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(token);
        if (tokenOpt.isEmpty() || !validatePasswordResetToken(token)) {
            throw new RuntimeException("Invalid or expired token");
        }

        PasswordResetToken passwordResetToken = tokenOpt.get();
        Customer customer = passwordResetToken.getCustomer();

        customer.setPassword(passwordEncoder.encode(newPassword));
        customerRepository.save(customer);

        /* invalidate the token */
        passwordResetTokenRepository.delete(passwordResetToken);
    }
}
