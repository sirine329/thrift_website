package com.thriftby.controller;

import com.thriftby.entity.Role;
import com.thriftby.entity.User;
import com.thriftby.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.thriftby.service.EmailService;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class WebViewController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @GetMapping("/login")
    public String login(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String disabled,
            Model model
    ) {
        if (error != null) {
            model.addAttribute("error", "Email ou mot de passe incorrect.");
        }

        if (disabled != null) {
            model.addAttribute("disabled", "Votre compte est desactive.");
        }

        if (logout != null) {
            model.addAttribute("success", "Deconnexion reussie.");
        }

        return "auth/login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }

        return "auth/register";
    }

    @PostMapping("/register")
    public String saveRegister(
            @Valid @ModelAttribute("user") User user,
            BindingResult result,
            RedirectAttributes ra
    ) {
        if (result.hasErrors()) {
            return "auth/register";
        }

        user.setEmail(user.getEmail().trim().toLowerCase());

        if (userRepository.existsByEmail(user.getEmail())) {
            ra.addFlashAttribute("error", "Email deja utilise.");
            ra.addFlashAttribute("user", user);
            return "redirect:/register";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        user.setActif(true);
        user.setVerified(false);
        user.setVerificationToken(UUID.randomUUID().toString());

        userRepository.save(user);
        boolean emailSent = emailService.sendVerificationEmail(user);

        if (emailSent) {
            ra.addFlashAttribute("success", "Compte cree avec succes. Un email de verification a ete envoye a " + user.getEmail() + ".");
        } else {
            ra.addFlashAttribute("error", "Compte cree, mais aucun mail n'a ete envoye. Ajoutez MAIL_PASSWORD avec un mot de passe d'application Gmail.");
        }
        return "redirect:/login";
    }

    @GetMapping("/verify")
    public String verifyEmail(@RequestParam(required = false) String token,
                              RedirectAttributes ra) {
        if (token == null || token.isBlank()) {
            ra.addFlashAttribute("error", "Jeton de verification manquant.");
            return "redirect:/login";
        }

        User user = userRepository.findByVerificationToken(token).orElse(null);
        if (user == null) {
            ra.addFlashAttribute("error", "Jeton de verification invalide ou expire.");
            return "redirect:/login";
        }

        user.setVerified(true);
        user.setActif(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        ra.addFlashAttribute("success", "Adresse e-mail verifiee. Vous pouvez vous connecter.");
        return "redirect:/login";
    }

    @GetMapping("/verify-dev")
    public String verifyEmailDev(@RequestParam(required = false) String email,
                                 RedirectAttributes ra) {
        if (email == null || email.isBlank()) {
            ra.addFlashAttribute("error", "Email manquant. Usage: /verify-dev?email=user@example.com");
            return "redirect:/login";
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            ra.addFlashAttribute("error", "Utilisateur non trouve pour cet email.");
            return "redirect:/login";
        }

        user.setVerified(true);
        user.setActif(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        ra.addFlashAttribute("success", "Email " + email + " verifie avec succes. Vous pouvez vous connecter.");
        return "redirect:/login";
    }
}
