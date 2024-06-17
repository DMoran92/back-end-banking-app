package com.bankingapp.backend.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

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
}
