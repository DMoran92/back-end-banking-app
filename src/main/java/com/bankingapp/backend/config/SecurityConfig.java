package com.bankingapp.backend.config;

import com.bankingapp.backend.security.JwtAuthenticationFilter;
import com.bankingapp.backend.service.CustomerDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomerDetailsService customerDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, CustomerDetailsService customerDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customerDetailsService = customerDetailsService;
    }
    /* Bean responsible for setting up filtering policies */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        logger.info("Configuring security filter chain");
        http
                /* Enable CSRF */
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                )
                /* Configure authorization rules */
                .authorizeHttpRequests(authorize -> authorize
                        /* Allow unauthenticated access to these endpoints */
                        .requestMatchers("/",
                                "/csrf-token",
                                "/resources/**",
                                "/static/**",
                                "/webjars/**",
                                "/api/authenticate",
                                "/api/register",
                                "/favicon.ico",
                                "/css/**",
                                "/img/**",
                                "/api/validate-2fa",
                                "/api/recover-password",
                                "/api/reset-password/**").permitAll()
                        /* limit admin page to only admins */
                        .requestMatchers("/dashboard_admin/**").hasRole("ADMIN")
                        /* Require authentication for all other requests */
                        .anyRequest().authenticated()
                )
                /* Configure session management to be stateless */
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                /* Enforce HTTPS */
                .requiresChannel(channel -> channel.anyRequest().requiresSecure());
        /* Add the JWT authentication filter before the UsernamePasswordAuthenticationFilter */
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        logger.info("JWT Authentication Filter added to security filter chain");
        return http.build();
    }
    /* Bean definition for the AuthenticationManager, required for custom authentication logic */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    /* Bean for password encoder, using Bcrypt */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt automatically generates a random salt for each password and includes it in the resulting hash.
        // this salt is used again when verifying passwords to ensure that the same password always
        // results in the same hash.
        // default hashing rounds: 10
        return new BCryptPasswordEncoder();
    }
}
