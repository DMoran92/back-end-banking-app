package com.bankingapp.backend.security;

import com.bankingapp.backend.service.CustomerDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final CustomerDetailsService customerDetailsService;
    private static final List<String> EXCLUDED_PATHS = List.of("/api/authenticate", "/api/register");


    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomerDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.customerDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        logger.info("JwtAuthenticationFilter: Processing request to {}", request.getRequestURI());


        String requestPath = request.getServletPath();

        // Skip JWT validation for excluded paths
        if (EXCLUDED_PATHS.contains(requestPath)) {
            logger.info("Skipping JWT validation for {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = null;

        // Extract JWT from cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                }
            }
        }

        if (jwt == null) {
            logger.warn("JWT token is missing in cookies");
        } else {
            logger.info("JWT token found in cookies: {}", jwt);
        }

        String username = null;

        if (jwt != null) {
            username = jwtUtil.extractUsername(jwt);
            logger.info("Extracted username from JWT: {}", username);
        }
        /* If the username is not null and the user is not already authenticated */
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            /* load user details by username */
            UserDetails userDetails = this.customerDetailsService.loadUserByUsername(username);
            /* validate the token */
            if (jwtUtil.validateToken(jwt, userDetails)) {
                /* create auth token */
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                /* set auth in the security context */
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.info("Successfully authenticated user: {}", username);
            } else {
                logger.error("Invalid JWT token for user: {}", username);
            }
        } else {
            logger.warn("JWT token is missing or invalid");
        }
        /* continue running spring security filter chain */
        filterChain.doFilter(request, response);
    }
}
