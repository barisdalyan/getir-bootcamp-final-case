package com.barisdalyanemre.librarymanagement.service.impl;

import com.barisdalyanemre.librarymanagement.dto.request.UpdateUserRequest;
import com.barisdalyanemre.librarymanagement.dto.response.UserDTO;
import com.barisdalyanemre.librarymanagement.entity.User;
import com.barisdalyanemre.librarymanagement.enums.Role;
import com.barisdalyanemre.librarymanagement.exception.BadRequestException;
import com.barisdalyanemre.librarymanagement.exception.ForbiddenException;
import com.barisdalyanemre.librarymanagement.exception.ResourceNotFoundException;
import com.barisdalyanemre.librarymanagement.mapper.UserMapper;
import com.barisdalyanemre.librarymanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private User testLibrarian;
    private UserDTO testUserDTO;
    private UpdateUserRequest updateUserRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole(Role.PATRON);
        testUser.setContactDetails("Test contact");
        testUser.setEnabled(true);

        testLibrarian = new User();
        testLibrarian.setId(2L);
        testLibrarian.setFirstName("Test");
        testLibrarian.setLastName("Librarian");
        testLibrarian.setEmail("librarian@example.com");
        testLibrarian.setPassword("password");
        testLibrarian.setRole(Role.LIBRARIAN);
        testLibrarian.setContactDetails("Librarian contact");
        testLibrarian.setEnabled(true);

        testUserDTO = new UserDTO();
        testUserDTO.setId(1L);
        testUserDTO.setFirstName("Test");
        testUserDTO.setLastName("User");
        testUserDTO.setEmail("test@example.com");
        testUserDTO.setRole(Role.PATRON);
        testUserDTO.setContactDetails("Test contact");
        testUserDTO.setEnabled(true);

        updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setFirstName("Updated");
        updateUserRequest.setLastName("User");
        updateUserRequest.setEmail("test@example.com");
        updateUserRequest.setContactDetails("Updated contact");
        updateUserRequest.setEnabled(true);
    }

    @Test
    @DisplayName("Should get user by ID when user exists")
    void getUserByIdWhenUserExists() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userMapper.toDTO(any(User.class))).thenReturn(testUserDTO);
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("test@example.com");
        
        UserDTO result = userService.getUserById(1L);
        
        assertNotNull(result);
        assertEquals(testUserDTO.getId(), result.getId());
        assertEquals(testUserDTO.getEmail(), result.getEmail());
    }

    @Test
    @DisplayName("Should throw exception when user does not exist")
    void getUserByIdWhenUserDoesNotExist() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(1L));
    }
    
    @Test
    @DisplayName("Should get all users")
    void getAllUsers() {
        List<User> users = Arrays.asList(testUser, testLibrarian);
        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toDTO(any(User.class))).thenReturn(testUserDTO);
        
        List<UserDTO> result = userService.getAllUsers();
        
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should update user when valid request")
    void updateUserWhenValidRequest() {
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("User");
        updatedUser.setEmail("test@example.com");
        
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toDTO(any(User.class))).thenReturn(testUserDTO);
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("test@example.com");
        
        UserDTO result = userService.updateUser(1L, updateUserRequest);
        
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
        verify(userMapper).updateUserFromRequest(any(User.class), any(UpdateUserRequest.class));
    }

    @Test
    @DisplayName("Should throw exception when email is already taken")
    void updateUserWhenEmailIsAlreadyTaken() {
        updateUserRequest.setEmail("new@example.com");
        
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("test@example.com");
        
        assertThrows(BadRequestException.class, () -> userService.updateUser(1L, updateUserRequest));
    }

    @Test
    @DisplayName("Should allow librarian to access other user's data")
    void librarianCanAccessOtherUserData() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userMapper.toDTO(any(User.class))).thenReturn(testUserDTO);
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("librarian@example.com");
        
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_LIBRARIAN")))
            .when(authentication).getAuthorities();
        
        UserDTO result = userService.getUserById(1L);
        
        assertNotNull(result);
        assertEquals(testUserDTO.getId(), result.getId());
    }

    @Test
    @DisplayName("Should throw exception when patron tries to access other user's data")
    void patronCannotAccessOtherUserData() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("other@example.com");
        
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_PATRON")))
            .when(authentication).getAuthorities();
        
        assertThrows(ForbiddenException.class, () -> userService.getUserById(1L));
    }

    @Test
    @DisplayName("Should delete user when user exists")
    void deleteUserWhenUserExists() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("test@example.com");
        
        userService.deleteUser(1L);
        
        verify(userRepository).delete(testUser);
    }

    @Test
    @DisplayName("Should throw exception when trying to delete a librarian")
    void deleteUserWhenUserIsLibrarian() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testLibrarian));
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("librarian@example.com");
        
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_LIBRARIAN")))
            .when(authentication).getAuthorities();
        
        assertThrows(ForbiddenException.class, () -> userService.deleteUser(2L));
    }

    @Test
    @DisplayName("Should throw exception when user tries to delete another user")
    void deleteUserWhenNotAuthorized() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("other@example.com");
        
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_PATRON")))
            .when(authentication).getAuthorities();
        
        assertThrows(ForbiddenException.class, () -> userService.deleteUser(1L));
    }
    
    @Test
    @DisplayName("Cleanup SecurityContextHolder after each test")
    void cleanupSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}
