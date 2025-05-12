package com.barisdalyanemre.librarymanagement.mapper;

import com.barisdalyanemre.librarymanagement.dto.request.UpdateUserRequest;
import com.barisdalyanemre.librarymanagement.dto.response.UserDTO;
import com.barisdalyanemre.librarymanagement.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .contactDetails(user.getContactDetails())
                .enabled(user.isEnabled())
                .build();
    }

    public void updateUserFromRequest(User user, UpdateUserRequest request) {
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setContactDetails(request.getContactDetails());
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }
    }
}
