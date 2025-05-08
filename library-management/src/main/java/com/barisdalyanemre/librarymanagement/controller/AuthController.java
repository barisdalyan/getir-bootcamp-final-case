package com.barisdalyanemre.librarymanagement.controller;

import com.barisdalyanemre.librarymanagement.dto.AuthResponse;
import com.barisdalyanemre.librarymanagement.dto.LoginRequest;
import com.barisdalyanemre.librarymanagement.dto.RegisterRequest;
import com.barisdalyanemre.librarymanagement.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "APIs for authentication and authorization")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Register a new user with PATRON role")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Received registration request for email: {}", request.getEmail());
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate user and return JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Received login request for email: {}", request.getEmail());
        return ResponseEntity.ok(authService.login(request));
    }
    
    @PostMapping("/promote/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(
        summary = "Promote user to librarian", 
        description = "Promote a user from PATRON to LIBRARIAN role. Only accessible by librarians.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Void> promoteToLibrarian(@PathVariable Long id) {
        log.info("Promoting user with ID: {} to librarian role", id);
        authService.promoteToLibrarian(id);
        return ResponseEntity.noContent().build();
    }
}
