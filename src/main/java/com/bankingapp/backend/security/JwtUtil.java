package com.bankingapp.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private static final long EXPIRATION_TIME = 900000; // 15 mins

    /* jwt secret is stored in JWT_SECRET ENV variable that can be set on production */
    @Value("${jwt.secret}")
    private String secretKey;
    private Key SECRET_KEY;

    @PostConstruct
    public void init() {
        SECRET_KEY = Keys.hmacShaKeyFor(secretKey.getBytes());
        logger.error("secret key: {}", SECRET_KEY.toString());
    }

    // this would generate new secret key each time, its annoying, because you need to keep restarting your browser to
    // clear cookie. I keep it here, we might do it that on production too who knows
    // private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    /* generate a JWT token for the given username */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username) // Set the username as the subject
                .setIssuedAt(new Date())  // Set the current date as the issue date
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // Set the expiration time
                .signWith(SECRET_KEY) // Sign the JWT with the secret key
                .compact(); // Build the JWT and serialize it to a compact form
    }
    /* validate token, extract username and check if its expired */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        if (!isValid) {
            logger.error("Token validation failed. Username from token: {}, Expected username: {}, Token expired: {}",
                    username, userDetails.getUsername(), isTokenExpired(token));
        }
        return isValid;
    }
    /* extract the username from the JWT token */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    /* extract expiration from the JWT token */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    /* extract specific claim from all claims, for instance used to extract subject or expiration above
    * "claims" are the fields stored in the jwt token*/
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    /* extract claims in the jwt token */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token).getBody();
    }
    /* check if the JWT token is expired by comparing with current date */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}