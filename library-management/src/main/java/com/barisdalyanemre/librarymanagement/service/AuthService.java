package com.barisdalyanemre.librarymanagement.service;

import com.barisdalyanemre.librarymanagement.dto.request.LoginRequest;
import com.barisdalyanemre.librarymanagement.dto.request.RegisterRequest;
import com.barisdalyanemre.librarymanagement.dto.response.AuthResponse;

public interface AuthService {
    
    /**
     * Register a new user
     * 
     * @param request the registration request
     * @return authentication response with token
     */
    AuthResponse register(RegisterRequest request);
    
    /**
     * Authenticate a user
     * 
     * @param request the login request
     * @return authentication response with token
     */
    AuthResponse login(LoginRequest request);
    
    /**
     * Promote a user to librarian role
     * 
     * @param userId ID of the user to promote
     */
    void promoteToLibrarian(Long userId);
}
