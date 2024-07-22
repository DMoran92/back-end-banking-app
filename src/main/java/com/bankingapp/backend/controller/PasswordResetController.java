package com.bankingapp.backend.controller;

import com.bankingapp.backend.service.PasswordResetService;
import com.bankingapp.backend.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class PasswordResetController {

    private static final Logger logger = LoggerFactory.getLogger(FavouritePayeeController.class);

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/recover-password")
    public ResponseEntity<String> recoverPassword(@RequestBody Map<String, String> payload) {

        logger.error("starting password recovery ...");
        MailService mailService = new MailService();
        String email = payload.get("email");
        String token = passwordResetService.createPasswordResetToken(email);
        logger.error("Generated token: {}. To be send to email: {}", token,email);
        try {
            /* send the reset email */
            mailService.sendPasswordResetMail(token, email);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to send email");
        }
        return ResponseEntity.ok("Password reset link sent");
    }

    @GetMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam("token") String token) {
        /* validate the token and allow the user to reset the password */
        boolean valid = passwordResetService.validatePasswordResetToken(token);
        if (!valid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("<html><body><script>alert('Invalid or expired token'); window.location.href = '/';</script></body></html>");
        }
        return ResponseEntity.ok("<html><body><script>window.location.href = '/?token=" + token + "';</script></body></html>");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> updatePassword(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        String newPassword = payload.get("newPassword");
        passwordResetService.resetPassword(token, newPassword);
        return ResponseEntity.ok("Password successfully reset");
    }
}
