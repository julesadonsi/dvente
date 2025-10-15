package com.usetech.dvente.controllers;

import com.usetech.dvente.configs.AsyncConfig;
import com.usetech.dvente.entities.users.EmailVerification;
import com.usetech.dvente.entities.users.User;
import com.usetech.dvente.events.users.UserRegisteredEvent;
import com.usetech.dvente.exceptions.CustomException;
import com.usetech.dvente.repositories.UserRepository;
import com.usetech.dvente.requests.LoginRequest;
import com.usetech.dvente.requests.RefreshRequest;
import com.usetech.dvente.requests.RegisterRequest;
import com.usetech.dvente.requests.users.SendVerificationCodeRequest;
import com.usetech.dvente.responses.AuthResponse;
import com.usetech.dvente.responses.RefreshTokenResponse;
import com.usetech.dvente.responses.users.UserResponse;
import com.usetech.dvente.responses.users.VerificationCodeResponse;
import com.usetech.dvente.services.auth.CookieService;
import com.usetech.dvente.services.auth.JwtService;
import com.usetech.dvente.services.notifs.EmailService;
import com.usetech.dvente.services.users.EmailVerificationService;
import com.usetech.dvente.services.users.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final CookieService cookieService;
    private final ApplicationEventPublisher eventPublisher;
    private final AsyncConfig.CustomUserDetailsService customUserDetailsService;
    private final UserRepository userRepository;
    private final EmailVerificationService verificationService;
    private final EmailService emailService;


    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request, HttpServletResponse response) {
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

        cookieService.addAccessTokenCookie(response, token);
        cookieService.addRefreshTokenCookie(response, refresh);

        return new AuthResponse(token, refresh, UserResponse.fromUser(user));
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request, HttpServletResponse response) {
        User user = userService.login(request.getEmail(), request.getPassword());
        String token = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);

        cookieService.addAccessTokenCookie(response, token);
        cookieService.addRefreshTokenCookie(response, refresh);

        return new AuthResponse(token, refresh, UserResponse.fromUser(user));
    }

    @PostMapping("/refresh")
    public RefreshTokenResponse refresh(@RequestBody(required = false) RefreshRequest request,
                                        HttpServletRequest httpRequest,
                                        HttpServletResponse response) {
        String refreshToken = null;

        if (httpRequest.getCookies() != null) {
            refreshToken = Arrays.stream(httpRequest.getCookies())
                    .filter(cookie -> "refresh_token".equals(cookie.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }

        if (refreshToken == null && request != null) {
            refreshToken = request.getRefreshToken();
        }

        if (refreshToken == null) {
            throw new RuntimeException("Refresh token manquant");
        }

        String email = jwtService.extractUsername(refreshToken);
        UserDetails user = customUserDetailsService.loadUserByUsername(email);

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new RuntimeException("Refresh token invalide ou expiré");
        }

        String newAccessToken = jwtService.generateAccessToken((User) user);
        String newRefreshToken = jwtService.generateRefreshToken((User) user);

        cookieService.addAccessTokenCookie(response, newAccessToken);
        cookieService.addRefreshTokenCookie(response, newRefreshToken);

        return new RefreshTokenResponse(newAccessToken, newRefreshToken);
    }

    @PostMapping("/logout")
    public void logout(HttpServletResponse response) {
        cookieService.clearAuthCookies(response);
    }

    @PostMapping("/send-verification-code")
    public ResponseEntity<?> sendVerificationCode(
            @Valid @RequestBody SendVerificationCodeRequest request) {

        String email = request.getEmail();

        // Vérifier si l'utilisateur existe déjà
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Cet email est déjà utilisé");
            error.put("field", "email");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        try {
            // Créer la vérification
            EmailVerification verification = verificationService.createVerification(email);

            // Envoyer l'email
            boolean emailSent = emailService.sendVerificationCode(email, verification.getCode());

            if (emailSent) {
                VerificationCodeResponse response = VerificationCodeResponse.builder()
                        .message("Code de vérification envoyé avec succès")
                        .email(email)
                        .expiresInMinutes(15)
                        .build();

                return ResponseEntity.ok(response);
            } else {
                verificationService.invalidateOldVerifications(email);
                Map<String, String> error = new HashMap<>();
                error.put("message", "Erreur lors de l'envoi de l'email. Veuillez réessayer.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
            }

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Une erreur inattendue s'est produite");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }



    /**
     * Vérifie un code de vérification.
     */
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        if (email == null || code == null) {
            throw new CustomException("Email et code sont requis");
        }
        boolean isValid = verificationService.isCodeValid(email, code);
        if (isValid) {
            verificationService.markAsUsed(email, code);
            return ResponseEntity.ok(Map.of(
                    "message", "Code vérifié avec succès",
                    "verified", true
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Code invalide ou expiré",
                    "verified", false
            ));
        }
    }



}