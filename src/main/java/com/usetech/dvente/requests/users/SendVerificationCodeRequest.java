package com.usetech.dvente.requests.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendVerificationCodeRequest {

    @NotBlank(message = "L'adresse email est requise")
    @Email(message = "Format d'email invalide")
    private String email;
}