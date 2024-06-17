package com.bankingapp.backend.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Random;

public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public static String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            logger.info("Authenticated user: {}", username);
            return username;
        } else {
            logger.warn("No authenticated user found");
            throw new RuntimeException("No authenticated user found");
        }
    }
    // simple way to generate banking card numbers
    // https://gist.github.com/halienm/b929d2bc62eb69a1726a0c76a3dbbd57
    // skiping Luhn check for now https://www.dcode.fr/luhn-algorithm.
    public static String generateCardNumber() {
        Random random = new Random();
        StringBuilder cardNumber = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            int digit = random.nextInt(10);
            cardNumber.append(digit);
        }
        return cardNumber.toString();
    }
}
