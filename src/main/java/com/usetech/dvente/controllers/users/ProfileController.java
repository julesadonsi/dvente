package com.usetech.dvente.controllers.users;

import com.usetech.dvente.entities.users.User;
import com.usetech.dvente.responses.ApiResponse;
import com.usetech.dvente.responses.users.UserResponse;
import com.usetech.dvente.services.users.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class ProfileController {

    private final UserService userService;


    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity <ApiResponse<UserResponse>> getUserProfile(Authentication authentication)
    {
        String email = authentication.getName();
        User user = userService.getUserByEmail(email);

        ApiResponse<UserResponse> response = new ApiResponse<>(
                "User profile retrieved successfully",
                UserResponse.fromUser(user)
        );
        return ResponseEntity.ok(response);
    }
}
