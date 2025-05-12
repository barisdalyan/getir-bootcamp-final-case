package com.barisdalyanemre.librarymanagement.service;

import com.barisdalyanemre.librarymanagement.dto.request.UpdateUserRequest;
import com.barisdalyanemre.librarymanagement.dto.response.UserDTO;

import java.util.List;

public interface UserService {
    
    /**
     * Get user by ID
     * 
     * @param id the user ID
     * @return the user DTO
     */
    UserDTO getUserById(Long id);
    
    /**
     * Get all users
     * 
     * @return list of all users
     */
    List<UserDTO> getAllUsers();
    
    /**
     * Update user information
     * 
     * @param id the user ID
     * @param updateRequest the update request
     * @return the updated user DTO
     */
    UserDTO updateUser(Long id, UpdateUserRequest updateRequest);
    
    /**
     * Delete a user
     * 
     * @param id the user ID
     */
    void deleteUser(Long id);
}
