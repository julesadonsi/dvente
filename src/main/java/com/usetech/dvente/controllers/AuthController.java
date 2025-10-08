package com.usetech.dvente.controllers;
import com.usetech.dvente.entities.users.User;
import com.usetech.dvente.events.users.UserRegisteredEvent;
import com.usetech.dvente.exceptions.CustomException;
import com.usetech.dvente.repositories.users.UserRepository;
import com.usetech.dvente.requests.LoginRequest;
import com.usetech.dvente.requests.RefreshRequest;
import com.usetech.dvente.requests.RegisterRequest;
import com.usetech.dvente.responses.ApiResponse;
import com.usetech.dvente.responses.AuthResponse;
import com.usetech.dvente.responses.RefreshTokenResponse;
import com.usetech.dvente.responses.users.UserResponse;
import com.usetech.dvente.services.CustomUserDetailsService;
import com.usetech.dvente.services.auth.JwtService;
import com.usetech.dvente.services.users.UserService;
import org.apache.coyote.Response;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final ApplicationEventPublisher eventPublisher;
    private final CustomUserDetailsService customUserDetailsService;

    private final UserRepository userRepository;


    public AuthController(
            UserService userService,
            JwtService jwtService,
            UserRepository userRepository,
            ApplicationEventPublisher eventPublisher,
            CustomUserDetailsService customUserDetailsService
    ) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.customUserDetailsService = customUserDetailsService;
    }


    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new CustomException("Email déjà utilisé");
        }
        User user = userService.register(
                request.getName(),
                request.getEmail(),
                request.getPassword()
        );
        eventPublisher.publishEvent(
                new UserRegisteredEvent(user, this)
        );

        String token = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);

        return new AuthResponse(token, refresh, UserResponse.fromUser(user));
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        User user = userService.login(request.getEmail(), request.getPassword());
        String token = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);
        return new AuthResponse(token, refresh, UserResponse.fromUser(user));
    }

    @PostMapping("/refresh")
    public RefreshTokenResponse refresh(@RequestBody RefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        String email = jwtService.extractUsername(refreshToken);

        UserDetails user = customUserDetailsService.loadUserByUsername(email);

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new RuntimeException("Refresh token invalide ou expiré");
        }

        String newAccessToken = jwtService.generateAccessToken((User) user);
        String newRefreshToken = jwtService.generateRefreshToken((User) user);

        return new RefreshTokenResponse(newAccessToken, newRefreshToken);
    }


}
