package com.bankingapp.backend.controller;

import com.bankingapp.backend.model.Customer;
import com.bankingapp.backend.model.TwoFactorAuth;
import com.bankingapp.backend.repository.CustomerRepository;
import com.bankingapp.backend.repository.TwoFactorAuthRepository;
import org.springframework.security.core.GrantedAuthority;
import com.bankingapp.backend.security.JwtUtil;
import com.bankingapp.backend.service.MailService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.bankingapp.backend.utilities.Utils.generate2FACode;

@RestController
@RequestMapping("/api")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TwoFactorAuthRepository twoFactorAuthRepository;


    /* Endpoint to authenticate the user and generate a JWT token */
    @PostMapping("/authenticate")
    public ResponseEntity<String> authenticate(@RequestBody AuthRequest authRequest,HttpServletRequest request, HttpServletResponse response) {
        logger.info("Authenticating user: {}", authRequest.getUsername());

        /* Perform authentication */
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        /* Include authentication in the security context
           This will be useful when/if we introduce roles
           other parts of the code can get current user authentication with
           SecurityContextHolder.getContext().getAuthentication(),
           our application security is stateless thought, so might not be needed in the end, not sure
         */
        SecurityContextHolder.getContext().setAuthentication(authentication);


        /* Retrieve customer */
        Customer customer = customerRepository.findByUsername(authRequest.getUsername());
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
        logger.info("Customer found: {}", customer.getCustomerId());

        /* Retrieve or create TwoFactorAuth for the customer if it's his first login */
        TwoFactorAuth twoFactorAuth = twoFactorAuthRepository.findByCustomer(customer);
        String currentBrowser = request.getHeader("User-Agent");
        /* create two-factor auth object if its not present in database. This is only in case of first login */
        if (twoFactorAuth == null) {
            twoFactorAuth = new TwoFactorAuth();
            twoFactorAuth.setCustomer(customer);
            twoFactorAuth.setLastKnownBrowser(currentBrowser);
            twoFactorAuthRepository.save(twoFactorAuth);
        }

        /* Check if browser has changed */
        if (!currentBrowser.equals(twoFactorAuth.getLastKnownBrowser())) {
            String code = generate2FACode();
            twoFactorAuth.setTwoFaCode(code);
            twoFactorAuth.setTwoFaCodeExpiry(new Timestamp(System.currentTimeMillis() + 5 * 60 * 1000)); // 5 minutes expiry
            twoFactorAuthRepository.save(twoFactorAuth);

            /* Send 2FA code via email */
            try {
                MailService ms = new MailService();
                ms.sendTwoFactorCode(code, customer.getFirstName(), customer.getLastName(), customer.getEmail());
                return ResponseEntity.status(HttpStatus.ACCEPTED).body("2FA required");
            } catch (Exception e) {
                logger.error("Failed to send 2FA code: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send 2FA code. Email server is down.");
            }
        }
        /* first login / trusted browser */
        /* Generate JWT token */
        String token = jwtUtil.generateToken(authRequest.getUsername());
        logger.info("Generated JWT token: {}", token);

        /* Set JWT token in a Http only cookie */
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true); // Prevents JavaScript access to the cookie (for security)
        cookie.setSecure(true); // Set to true in production for HTTPS
        cookie.setPath("/"); // cookie is accessible throughout entire application
        cookie.setMaxAge(3600); // 60 min
        response.addCookie(cookie);
        logger.info("Set JWT token in HttpOnly cookie");
        /* get the user role stored in authorities */
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        GrantedAuthority authority = userDetails.getAuthorities().iterator().next();

        logger.info("User role: {}", authority.toString());
        return ResponseEntity.ok(authority.toString());
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Removing user auth from security context");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);

            // Clear the JWT cookie
            Cookie cookie = new Cookie("jwt", null);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(0); // Expire the cookie immediately
            response.addCookie(cookie);
        }
        logger.info("User logged out");
        return ResponseEntity.ok("successful logout");
    }

    @PostMapping("/validate-2fa")
    public ResponseEntity<String> validate2FA(@RequestBody Map<String, Object> payload, HttpServletRequest request, HttpServletResponse response) {
        /* retrieve the parameters */
        String username = payload.get("username").toString();
        String TwoFAcode = payload.get("twoFaCode").toString();

        /* find customer */
        Customer customer = customerRepository.findByUsername(username);
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
        /* find matching two-factor auth based on customer */
        TwoFactorAuth twoFactorAuth = twoFactorAuthRepository.findByCustomer(customer);
        /* check if the code supplied by the user was correct, or did the code timeout already */
        if (twoFactorAuth == null ||
            twoFactorAuth.getTwoFaCode() == null ||
            !twoFactorAuth.getTwoFaCode().equals(TwoFAcode) ||
            twoFactorAuth.getTwoFaCodeExpiry().before(new Timestamp(System.currentTimeMillis()))) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired 2FA code");
        }
        /* code was ok we can proceed to generate jwt token */
        /* Generate JWT token */
        String token = jwtUtil.generateToken(customer.getUsername());
        logger.info("Generated JWT token: {}", token);

        /* Set JWT token in a Http only cookie */
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true); // Prevents JavaScript access to the cookie (for security)
        cookie.setSecure(false); // Set to true in production for HTTPS
        cookie.setPath("/"); // cookie is accessible throughout entire application
        cookie.setMaxAge(900); // 15 min
        response.addCookie(cookie);
        logger.info("Set JWT token in HttpOnly cookie");

        /* set the current browser as trusted one */
        String currentBrowser = request.getHeader("User-Agent");
        twoFactorAuth.setLastKnownBrowser(currentBrowser);

        /* Invalidate 2FA code */
        twoFactorAuth.setTwoFaCode(null);
        twoFactorAuth.setTwoFaCodeExpiry(null);
        twoFactorAuthRepository.save(twoFactorAuth);

        return ResponseEntity.ok("Authenticated");
    }
}
/* DTO for authentication request containing username and password  */
class AuthRequest {
    private String username;
    private String password;

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
