package com.barisdalyanemre.librarymanagement.security;

import com.barisdalyanemre.librarymanagement.enums.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TestJwtUtils {
    
    private static final String TEST_SECRET = "ThisIsAVeryLongSecretKeyGeneratedSpecificallyForTestingPurposesOnlyDontUseInProduction";
    private static final long TEST_JWT_EXPIRATION = 3600000; // 1 hour

    public static String generateTestToken(String email, Role role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role.name());
        
        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + TEST_JWT_EXPIRATION))
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes()))
                .compact();
    }

    public static String generateLibrarianToken() {
        return generateTestToken("librarian@test.com", Role.LIBRARIAN);
    }

    public static String generatePatronToken() {
        return generateTestToken("patron@test.com", Role.PATRON);
    }

    public static String generateRandomUserToken() {
        String randomEmail = "user-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        return generateTestToken(randomEmail, Role.PATRON);
    }
}
