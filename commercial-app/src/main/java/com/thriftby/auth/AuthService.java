package com.thriftby.auth;

import com.thriftby.config.BusinessException;
import com.thriftby.entity.User;
import com.thriftby.entity.Role;
import com.thriftby.repository.UserRepository;
import com.thriftby.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authManager;
    private final EmailService emailService;

    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest req) {
        String email = req.email.trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("Email déjà utilisé: " + req.email);
        }

        User user = User.builder()
                .nom(req.nom)
                .prenom(req.prenom)
                .email(email)
                .password(passwordEncoder.encode(req.password))
                .ville(req.ville)
                .role(Role.USER)
                .actif(true)
                .verified(true)
                .verificationToken(null)
                .build();

        userRepository.save(user);
        emailService.sendVerificationEmail(user);
        return buildResponse(null, user);
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest req) {
        String email = req.email.trim().toLowerCase();
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, req.password)
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable"));

        String token = jwtUtil.generateToken(user);
        return buildResponse(token, user);
    }

    private AuthDtos.AuthResponse buildResponse(String token, User user) {
        return AuthDtos.AuthResponse.builder()
                .token(token)
                .user(AuthDtos.AuthResponse.UserDto.builder()
                        .id(user.getId())
                        .nom(user.getNom())
                        .prenom(user.getPrenom())
                        .email(user.getEmail())
                        .avatarUrl(user.getAvatarUrl())
                        .ville(user.getVille())
                        .role(user.getRole().name())
                        .build())
                .build();
    }
}
