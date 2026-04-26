package com.example.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenUtil implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);
    private static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60 * 1000;
    private static final Key SIGNING_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    public String generateJwtToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SIGNING_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    public String generateResetToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setExpiration(new Date(System.currentTimeMillis() + 15 * 60 * 1000))
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS512)
                .compact();
    }
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(SIGNING_KEY)
                    .build()
                    .parseClaimsJws(authToken);
            return true;

        } catch (SecurityException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}
