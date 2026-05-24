package com.thriftby.auth;

import jakarta.validation.constraints.*;
import lombok.*;

public class AuthDtos {

    // ---- Request DTOs ----
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegisterRequest {
        @NotBlank(message = "Le nom est obligatoire")
        public String nom;

        @NotBlank(message = "Le prénom est obligatoire")
        public String prenom;

        @Email(message = "Email invalide")
        @NotBlank(message = "L'email est obligatoire")
        public String email;

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 6, message = "Le mot de passe doit faire au moins 6 caractères")
        public String password;

        public String ville;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoginRequest {
        @Email(message = "Email invalide")
        @NotBlank(message = "L'email est obligatoire")
        public String email;

        @NotBlank(message = "Le mot de passe est obligatoire")
        public String password;
    }

    // ---- Response DTOs ----
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class AuthResponse {
        private String token;
        private UserDto user;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        @Builder
        public static class UserDto {
            private Long id;
            private String nom;
            private String prenom;
            private String email;
            private String avatarUrl;
            private String ville;
            private String role;
        }
    }
}