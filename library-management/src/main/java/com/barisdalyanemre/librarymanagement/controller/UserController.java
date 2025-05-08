package com.barisdalyanemre.librarymanagement.controller;

import com.barisdalyanemre.librarymanagement.dto.UpdateUserRequest;
import com.barisdalyanemre.librarymanagement.dto.UserDTO;
import com.barisdalyanemre.librarymanagement.service.UserService;
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
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "APIs for managing users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve a user's details by their ID. Librarians can access any user, patrons can only access their own details.")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        log.info("Retrieving user with ID: {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Get all users", description = "Retrieve a list of all users. Only accessible by librarians.")
    public ResponseEntity<Iterable<UserDTO>> getAllUsers() {
        log.info("Retrieving all users");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update user details. Librarians can update any user, patrons can only update their own details.")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest updateRequest) {
        log.info("Updating user with ID: {}", id);
        return ResponseEntity.ok(userService.updateUser(id, updateRequest));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Delete a user by ID. Only librarians can delete users.")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
