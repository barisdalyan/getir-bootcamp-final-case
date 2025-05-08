package com.barisdalyanemre.librarymanagement.mapper;

import com.barisdalyanemre.librarymanagement.dto.UpdateUserRequest;
import com.barisdalyanemre.librarymanagement.dto.UserDTO;
import com.barisdalyanemre.librarymanagement.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .contactDetails(user.getContactDetails())
                .build();
    }

    public void updateUserFromRequest(User user, UpdateUserRequest request) {
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setContactDetails(request.getContactDetails());
    }
}
