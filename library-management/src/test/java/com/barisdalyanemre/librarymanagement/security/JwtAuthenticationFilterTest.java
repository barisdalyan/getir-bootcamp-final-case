package com.barisdalyanemre.librarymanagement.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final String validToken = "valid.jwt.token";
    private final String validEmail = "user@example.com";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_withValidJwtToken_shouldAuthenticate() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtils.extractUsername(validToken)).thenReturn(validEmail);
        when(userDetailsService.loadUserByUsername(validEmail)).thenReturn(userDetails);
        when(jwtUtils.validateToken(validToken, userDetails)).thenReturn(true);
        when(userDetails.getAuthorities()).thenReturn(java.util.Collections.emptyList());

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(userDetails, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withInvalidJwtToken_shouldNotAuthenticate() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtils.extractUsername(validToken)).thenReturn(validEmail);
        when(userDetailsService.loadUserByUsername(validEmail)).thenReturn(userDetails);
        when(jwtUtils.validateToken(validToken, userDetails)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withNoAuthorizationHeader_shouldNotAuthenticate() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withNonBearerToken_shouldNotAuthenticate() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic " + validToken);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withExceptionDuringProcessing_shouldContinueChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtils.extractUsername(validToken)).thenThrow(new RuntimeException("Test exception"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withExistingAuthentication_shouldNotOverwrite() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "existing-user", null, java.util.Collections.emptyList()
            )
        );
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtils.extractUsername(validToken)).thenReturn(validEmail);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertEquals("existing-user", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userDetailsService);
    }
}
