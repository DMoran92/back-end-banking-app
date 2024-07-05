package com.bankingapp.backend.controller;

import com.bankingapp.backend.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    /* Endpoint to authenticate the user and generate a JWT token */
    @PostMapping("/authenticate")
    public ResponseEntity<String> authenticate(@RequestBody AuthRequest authRequest, HttpServletResponse response) {
        logger.info("Authenticating user: {}", authRequest.getUsername());

        /* Perform authentication */
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        /* Include authentication in the security context
           This will be useful when/if we introduce roles
           other parts of the code can get current user authentication with
           SecurityContextHolder.getContext().getAuthentication(),
           our application security is stateless thought, so might not be needed in the end, not sure
         */
        SecurityContextHolder.getContext().setAuthentication(authentication);

        /* Generate JWT token */
        String token = jwtUtil.generateToken(authRequest.getUsername());
        logger.info("Generated JWT token: {}", token);

        /* Set JWT token in a Http only cookie */
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true); // Prevents JavaScript access to the cookie (for security)
        cookie.setSecure(false); // Set to true in production for HTTPS
        cookie.setPath("/"); // cookie is accessible throughout entire application
        cookie.setMaxAge(900); // 15 min
        response.addCookie(cookie);
        logger.info("Set JWT token in HttpOnly cookie");

        return ResponseEntity.ok("Authenticated");
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
