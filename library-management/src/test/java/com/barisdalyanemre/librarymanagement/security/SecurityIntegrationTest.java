package com.barisdalyanemre.librarymanagement.security;

import com.barisdalyanemre.librarymanagement.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityIntegrationTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;
    private UserDetails patronDetails;
    private UserDetails librarianDetails;
    private String validToken;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        
        SecurityContextHolder.clearContext();
        
        patronDetails = new org.springframework.security.core.userdetails.User(
            "patron@example.com", 
            "password", 
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + Role.PATRON.name()))
        );
        
        librarianDetails = new org.springframework.security.core.userdetails.User(
            "librarian@example.com", 
            "password", 
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + Role.LIBRARIAN.name()))
        );
        
        validToken = "valid.jwt.token";
    }

    @Test
    void validToken_ShouldAuthenticateUser() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer " + validToken);
        when(jwtUtils.extractUsername(validToken)).thenReturn("patron@example.com");
        when(jwtUtils.validateToken(validToken, patronDetails)).thenReturn(true);
        when(userDetailsService.loadUserByUsername("patron@example.com")).thenReturn(patronDetails);
        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("patron@example.com", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void invalidToken_ShouldNotAuthenticate() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer invalidtoken");
        when(jwtUtils.extractUsername("invalidtoken")).thenThrow(new RuntimeException("Invalid token"));
        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        assertEquals(null, SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void noToken_ShouldNotAuthenticate() throws ServletException, IOException {
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        assertEquals(null, SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void malformedAuthorizationHeader_ShouldNotAuthenticate() throws ServletException, IOException {
        request.addHeader("Authorization", "malformed-header");
        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        assertEquals(null, SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void patronRoleTest() {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                patronDetails.getUsername(),
                null,
                patronDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        boolean hasPatronRole = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PATRON"));
        boolean hasLibrarianRole = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_LIBRARIAN"));
        
        assertEquals(true, hasPatronRole);
        assertEquals(false, hasLibrarianRole);
    }

    @Test
    void librarianRoleTest() {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                librarianDetails.getUsername(),
                null,
                librarianDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        boolean hasPatronRole = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PATRON"));
        boolean hasLibrarianRole = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_LIBRARIAN"));
        
        assertEquals(false, hasPatronRole);
        assertEquals(true, hasLibrarianRole);
    }
}
