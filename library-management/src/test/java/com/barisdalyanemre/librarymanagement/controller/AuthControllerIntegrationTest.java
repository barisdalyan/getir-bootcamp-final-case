package com.barisdalyanemre.librarymanagement.controller;

import com.barisdalyanemre.librarymanagement.dto.request.LoginRequest;
import com.barisdalyanemre.librarymanagement.dto.request.RegisterRequest;
import com.barisdalyanemre.librarymanagement.entity.User;
import com.barisdalyanemre.librarymanagement.enums.Role;
import com.barisdalyanemre.librarymanagement.repository.BorrowRecordRepository;
import com.barisdalyanemre.librarymanagement.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private User librarian;

    @BeforeEach
    void setUp() {
        borrowRecordRepository.deleteAll();
        userRepository.deleteAll();

        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setFirstName("John");
        validRegisterRequest.setLastName("Doe");
        validRegisterRequest.setEmail("john.doe@example.com");
        validRegisterRequest.setPassword("Password123!");
        validRegisterRequest.setContactDetails("1234567890");

        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("john.doe@example.com");
        validLoginRequest.setPassword("Password123!");

        librarian = new User();
        librarian.setFirstName("Admin");
        librarian.setLastName("User");
        librarian.setEmail("admin@example.com");
        librarian.setPassword(passwordEncoder.encode("Admin123!"));
        librarian.setRole(Role.LIBRARIAN);
        librarian.setEnabled(true);
        librarian = userRepository.save(librarian);
    }

    @Test
    void register_withValidRequest_shouldReturnAuthResponse() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value(validRegisterRequest.getEmail()))
                .andExpect(jsonPath("$.firstName").value(validRegisterRequest.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(validRegisterRequest.getLastName()))
                .andExpect(jsonPath("$.role").value("PATRON"));

        User savedUser = userRepository.findByEmail(validRegisterRequest.getEmail()).orElse(null);
        assertNotNull(savedUser);
        assertEquals(validRegisterRequest.getFirstName(), savedUser.getFirstName());
        assertEquals(validRegisterRequest.getLastName(), savedUser.getLastName());
        assertEquals(Role.PATRON, savedUser.getRole());
        assertTrue(savedUser.isEnabled());
    }

    @Test
    void login_withValidCredentials_shouldReturnAuthResponse() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value(validLoginRequest.getEmail()))
                .andExpect(jsonPath("$.firstName").value(validRegisterRequest.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(validRegisterRequest.getLastName()))
                .andExpect(jsonPath("$.role").value("PATRON"));
    }

    @Test
    void login_withInvalidCredentials_shouldReturnUnauthorized() throws Exception {
        LoginRequest invalidLoginRequest = new LoginRequest();
        invalidLoginRequest.setEmail("nonexistent@example.com");
        invalidLoginRequest.setPassword("WrongPassword123!");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLoginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void promoteToLibrarian_asLibrarian_shouldSucceed() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isOk());

        User userToPromote = userRepository.findByEmail(validRegisterRequest.getEmail()).orElseThrow();

        mockMvc.perform(put("/api/v1/auth/promote/{id}", userToPromote.getId()))
                .andExpect(status().isNoContent());

        User promotedUser = userRepository.findById(userToPromote.getId()).orElseThrow();
        assertEquals(Role.LIBRARIAN, promotedUser.getRole());
    }

    @Test
    @WithMockUser(roles = "PATRON")
    void promoteToLibrarian_asPatron_shouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isOk());

        User userToPromote = userRepository.findByEmail(validRegisterRequest.getEmail()).orElseThrow();

        mockMvc.perform(put("/api/v1/auth/promote/{id}", userToPromote.getId()))
                .andExpect(status().isForbidden());

        User nonPromotedUser = userRepository.findById(userToPromote.getId()).orElseThrow();
        assertEquals(Role.PATRON, nonPromotedUser.getRole());
    }
}
