package com.ga.lanceify.identity.security;

import com.ga.lanceify.identity.model.User;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility Service for generating, reading and validating tokens.
 *
 * Token format is shared with every other service:
 * sub = user id, role = ADMIN / CLIENT / FREELANCER (no ROLE_ prefix), HS256.
 */
@Service
public class JwtUtils {
    private static final Logger logger = Logger.getLogger(JwtUtils.class.getName());

    private final SecretKey key;

    private final long jwtExpirationMs;

    public JwtUtils(@Value("${jwt.secret}") String jwtSecret,
                    @Value("${jwt.expiration-ms:86400000}") long jwtExpirationMs) {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirationMs = jwtExpirationMs;
    }

    /**
     * creates JWT Token for an authenticated user
     * @param user the logged-in user
     * @return Jwt Token
     */
    public String generateJwtToken(User user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + jwtExpirationMs))
                .signWith(key)
                .compact();
    }

    /**
     * get the user id that is stored in the JWT Token
     * @param token Jwt Token
     * @return user id that is stored in the token
     */
    public String getUserIdFromJwtToken(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
    }

    /**
     * check whether the JWT token is valid (signature and expiry)
     * @param authToken JWT token that is sent by user
     * @return true if token is valid, otherwise return false
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            logger.log(Level.WARNING, "Invalid JWT: {0}", e.getMessage());
        }
        return false;
    }
}
