package com.barisdalyanemre.librarymanagement.dto;

import com.barisdalyanemre.librarymanagement.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private String contactDetails;
    private boolean enabled;
}
