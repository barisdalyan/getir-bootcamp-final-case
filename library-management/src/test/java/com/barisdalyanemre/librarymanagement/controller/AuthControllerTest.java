package com.barisdalyanemre.librarymanagement.controller;

import com.barisdalyanemre.librarymanagement.dto.request.LoginRequest;
import com.barisdalyanemre.librarymanagement.dto.request.RegisterRequest;
import com.barisdalyanemre.librarymanagement.dto.response.AuthResponse;
import com.barisdalyanemre.librarymanagement.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void register_shouldReturnAuthResponse() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setEmail("john.doe@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setContactDetails("1234567890");

        AuthResponse authResponse = AuthResponse.builder()
                .token("jwt-token")
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .role("PATRON")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    void login_shouldReturnAuthResponse() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("john.doe@example.com");
        loginRequest.setPassword("password123");

        AuthResponse authResponse = AuthResponse.builder()
                .token("jwt-token")
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .role("PATRON")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void promoteToLibrarian_shouldCallServiceAndReturnNoContent() throws Exception {
        Long userId = 1L;
        doNothing().when(authService).promoteToLibrarian(userId);

        mockMvc.perform(put("/api/v1/auth/promote/{id}", userId))
                .andExpect(status().isNoContent());

        verify(authService, times(1)).promoteToLibrarian(userId);
    }
}
