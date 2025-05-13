package com.barisdalyanemre.librarymanagement.mapper;

import com.barisdalyanemre.librarymanagement.dto.request.UpdateUserRequest;
import com.barisdalyanemre.librarymanagement.dto.response.UserDTO;
import com.barisdalyanemre.librarymanagement.entity.User;
import com.barisdalyanemre.librarymanagement.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private UserMapper userMapper;
    private User user;
    private UpdateUserRequest updateUserRequest;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
        
        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setRole(Role.PATRON);
        user.setContactDetails("1234567890");
        user.setEnabled(true);
        
        updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setFirstName("Jane");
        updateUserRequest.setLastName("Smith");
        updateUserRequest.setEmail("jane.smith@example.com");
        updateUserRequest.setContactDetails("0987654321");
        updateUserRequest.setEnabled(false);
    }

    @Test
    void toDTO_shouldMapAllFields() {
        UserDTO result = userMapper.toDTO(user);
        
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getFirstName(), result.getFirstName());
        assertEquals(user.getLastName(), result.getLastName());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getRole(), result.getRole());
        assertEquals(user.getContactDetails(), result.getContactDetails());
        assertEquals(user.isEnabled(), result.isEnabled());
    }

    @Test
    void updateUserFromRequest_shouldUpdateAllFields() {
        User userToUpdate = new User();
        userToUpdate.setFirstName("Original First");
        userToUpdate.setLastName("Original Last");
        userToUpdate.setEmail("original@example.com");
        userToUpdate.setContactDetails("Original Contact");
        userToUpdate.setEnabled(true);
        
        userMapper.updateUserFromRequest(userToUpdate, updateUserRequest);
        
        assertEquals(updateUserRequest.getFirstName(), userToUpdate.getFirstName());
        assertEquals(updateUserRequest.getLastName(), userToUpdate.getLastName());
        assertEquals(updateUserRequest.getEmail(), userToUpdate.getEmail());
        assertEquals(updateUserRequest.getContactDetails(), userToUpdate.getContactDetails());
        assertEquals(updateUserRequest.getEnabled(), userToUpdate.isEnabled());
    }

    @Test
    void updateUserFromRequest_shouldHandleNullEnabled() {
        User userToUpdate = new User();
        userToUpdate.setEnabled(true);
        
        UpdateUserRequest requestWithNullEnabled = new UpdateUserRequest();
        requestWithNullEnabled.setFirstName("Jane");
        requestWithNullEnabled.setLastName("Smith");
        requestWithNullEnabled.setEmail("jane.smith@example.com");
        requestWithNullEnabled.setContactDetails("0987654321");
        requestWithNullEnabled.setEnabled(null);
        
        userMapper.updateUserFromRequest(userToUpdate, requestWithNullEnabled);
        
        assertTrue(userToUpdate.isEnabled());
    }
}
