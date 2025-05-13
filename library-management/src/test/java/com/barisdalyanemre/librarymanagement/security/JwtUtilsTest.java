package com.barisdalyanemre.librarymanagement.security;

import com.barisdalyanemre.librarymanagement.enums.Role;
import com.barisdalyanemre.librarymanagement.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private UserDetails userDetails;
    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_SECRET = "dGhpc2lzYXZlcnlsb25nc2VjcmV0a2V5Zm9ydGVzdGluZ3B1cnBvc2Vzb25seWRvbnR1c2VpbnByb2R1Y3Rpb24=";
    private final long TEST_EXPIRATION = 3600000;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpiration", TEST_EXPIRATION);
        
        userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(TEST_EMAIL);
    }

    @Test
    void generateToken_withUserDetails_shouldCreateValidToken() {
        String token = jwtUtils.generateToken(userDetails);
        
        assertNotNull(token);
        assertEquals(TEST_EMAIL, jwtUtils.extractUsername(token));
        assertFalse(jwtUtils.extractClaim(token, Claims::getExpiration).before(new Date()));
    }

    @Test
    void generateToken_withEmailOnly_shouldCreateValidToken() {
        String token = jwtUtils.generateToken(TEST_EMAIL);
        
        assertNotNull(token);
        assertEquals(TEST_EMAIL, jwtUtils.extractUsername(token));
        assertFalse(jwtUtils.extractClaim(token, Claims::getExpiration).before(new Date()));
    }

    @Test
    void generateToken_withExtraClaims_shouldIncludeClaimsInToken() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", Role.LIBRARIAN.name());
        extraClaims.put("customClaim", "customValue");
        
        String token = jwtUtils.generateToken(extraClaims, userDetails);
        
        assertNotNull(token);
        assertEquals(TEST_EMAIL, jwtUtils.extractUsername(token));
        assertEquals(Role.LIBRARIAN.name(), jwtUtils.extractClaim(token, claims -> claims.get("role")));
        assertEquals("customValue", jwtUtils.extractClaim(token, claims -> claims.get("customClaim")));
    }

    @Test
    void validateToken_withValidTokenAndMatchingUser_shouldReturnTrue() {
        String token = jwtUtils.generateToken(userDetails);
        
        boolean isValid = jwtUtils.validateToken(token, userDetails);
        
        assertTrue(isValid);
    }

    @Test
    void validateToken_withValidTokenAndNonMatchingUser_shouldReturnFalse() {
        String token = jwtUtils.generateToken(userDetails);
        
        UserDetails differentUser = mock(UserDetails.class);
        when(differentUser.getUsername()).thenReturn("different@example.com");
        
        boolean isValid = jwtUtils.validateToken(token, differentUser);
        
        assertFalse(isValid);
    }

    @Test
    void extractClaim_withCustomClaimExtractor_shouldExtractClaim() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("customClaim", "customValue");
        String token = jwtUtils.generateToken(extraClaims, userDetails);
        
        Function<Claims, String> customClaimExtractor = claims -> claims.get("customClaim", String.class);
        
        String extractedValue = jwtUtils.extractClaim(token, customClaimExtractor);
        
        assertEquals("customValue", extractedValue);
    }

    @Test
    void extractUsername_withValidToken_shouldReturnUsername() {
        String token = jwtUtils.generateToken(userDetails);
        
        String extractedUsername = jwtUtils.extractUsername(token);
        
        assertEquals(TEST_EMAIL, extractedUsername);
    }

    @Test
    void extractAllClaims_withInvalidToken_shouldThrowUnauthorizedException() {
        String invalidToken = "invalid.token.string";
        
        assertThrows(UnauthorizedException.class, () -> {
            ReflectionTestUtils.invokeMethod(jwtUtils, "extractAllClaims", invalidToken);
        });
    }

    @Test
    void isTokenExpired_withExpiredToken_shouldReturnTrue() throws Exception {
        JwtUtils spyJwtUtils = spy(jwtUtils);
        
        Date pastDate = new Date(System.currentTimeMillis() - 1000);
        doReturn(pastDate).when(spyJwtUtils).extractClaim(anyString(), any());
        
        boolean isExpired = ReflectionTestUtils.invokeMethod(spyJwtUtils, "isTokenExpired", "dummy-token");
        
        assertTrue(isExpired);
    }

    @Test
    void isTokenExpired_withValidToken_shouldReturnFalse() {
        String token = jwtUtils.generateToken(userDetails);
        
        boolean isExpired = ReflectionTestUtils.invokeMethod(jwtUtils, "isTokenExpired", token);
        
        assertFalse(isExpired);
    }
}
