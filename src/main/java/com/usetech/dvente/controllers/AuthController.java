package com.usetech.dvente.controllers;
import com.usetech.dvente.entities.users.User;
import com.usetech.dvente.events.users.UserRegisteredEvent;
import com.usetech.dvente.exceptions.CustomException;
import com.usetech.dvente.repositories.users.UserRepository;
import com.usetech.dvente.requests.LoginRequest;
import com.usetech.dvente.requests.RefreshRequest;
import com.usetech.dvente.requests.RegisterRequest;
import com.usetech.dvente.responses.AuthResponse;
import com.usetech.dvente.responses.RefreshTokenResponse;
import com.usetech.dvente.responses.users.UserResponse;
import com.usetech.dvente.services.auth.JwtService;
import com.usetech.dvente.services.users.UserService;
import org.springframework.context.ApplicationEventPublisher;
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

    private final UserRepository userRepository;


    public AuthController(
            UserService userService,
            JwtService jwtService,
            UserRepository userRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
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

        String token = jwtService.generateAccessToken(user.getEmail());
        String refresh = jwtService.generateRefreshToken(user.getEmail());

        return new AuthResponse(token, refresh, UserResponse.fromUser(user));
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        User user = userService.login(request.getEmail(), request.getPassword());
        String token = jwtService.generateAccessToken(user.getEmail());
        String refresh = jwtService.generateRefreshToken(user.getEmail());
        return new AuthResponse(token, refresh, UserResponse.fromUser(user));
    }

    @PostMapping("/refresh")
    public RefreshTokenResponse refresh(@RequestBody RefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new CustomException("Refresh token invalide ou expiré");
        }

        String email = jwtService.extractEmail(refreshToken);
        String newAccessToken = jwtService.generateAccessToken(email);
        String newRefreshToken = jwtService.generateRefreshToken(email);

        return new RefreshTokenResponse(newAccessToken, newRefreshToken);
    }
}
