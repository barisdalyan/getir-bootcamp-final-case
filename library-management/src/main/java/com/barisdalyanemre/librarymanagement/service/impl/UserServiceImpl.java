package com.barisdalyanemre.librarymanagement.service.impl;

import com.barisdalyanemre.librarymanagement.dto.request.UpdateUserRequest;
import com.barisdalyanemre.librarymanagement.dto.response.UserDTO;
import com.barisdalyanemre.librarymanagement.enums.Role;
import com.barisdalyanemre.librarymanagement.entity.User;
import com.barisdalyanemre.librarymanagement.exception.BadRequestException;
import com.barisdalyanemre.librarymanagement.exception.ForbiddenException;
import com.barisdalyanemre.librarymanagement.exception.ResourceNotFoundException;
import com.barisdalyanemre.librarymanagement.mapper.UserMapper;
import com.barisdalyanemre.librarymanagement.repository.UserRepository;
import com.barisdalyanemre.librarymanagement.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDTO getUserById(Long id) {
        User user = findUserById(id);
        validateUserAccess(user);
        return userMapper.toDTO(user);
    }
    
    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UpdateUserRequest updateRequest) {
        User user = findUserById(id);
        validateUserAccess(user);
        
        if (!user.getEmail().equals(updateRequest.getEmail()) && 
            userRepository.existsByEmail(updateRequest.getEmail())) {
            throw new BadRequestException("Email is already taken");
        }
        
        userMapper.updateUserFromRequest(user, updateRequest);
        User updatedUser = userRepository.save(user);
        log.info("User with ID: {} has been updated", id);
        
        return userMapper.toDTO(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = findUserById(id);
        validateUserAccess(user);
        
        // Prevent deletion of a librarian by another librarian
        if (user.getRole() == Role.LIBRARIAN) {
            throw new ForbiddenException("Librarians cannot be deleted through this endpoint");
        }
        
        userRepository.delete(user);
        log.info("User with ID: {} has been deleted", id);
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
    
    private void validateUserAccess(User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        
        // If the user is a librarian, they can access any user
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_LIBRARIAN"))) {
            return;
        }
        
        // If not a librarian, users can only access their own data
        if (!user.getEmail().equals(currentUserEmail)) {
            throw new ForbiddenException("Access denied: You can only access your own user data");
        }
    }
}
