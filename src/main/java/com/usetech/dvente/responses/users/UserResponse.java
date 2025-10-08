package com.usetech.dvente.responses.users;

import com.usetech.dvente.entities.users.User;
import com.usetech.dvente.entities.users.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Data
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String name;
    private String email;
    private UserRole role;
    private String phone;
    private String createdAt;
    private String updatedAt;

    public static UserResponse fromUser(User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getPhone(),
                user.getCreatedAt() != null ? user.getCreatedAt().format(formatter) : null,
                user.getUpdatedAt() != null ? user.getUpdatedAt().format(formatter) : null
        );
    }
}