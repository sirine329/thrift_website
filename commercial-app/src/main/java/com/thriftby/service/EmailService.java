package com.thriftby.service;

import com.thriftby.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:8081}")
    private String appBaseUrl;

    @Value("${resend.from:ThriftBy <noreply@thriftby.tn>}")
    private String fromAddress;

    public void sendVerificationEmail(User user) {
        if (user == null || user.getEmail() == null || user.getVerificationToken() == null) {
            return;
        }

        String verificationUrl = appBaseUrl + "/verify?token=" + user.getVerificationToken();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setFrom(fromAddress);
        message.setSubject("Vérifiez votre adresse email ThriftBy");
        message.setText("Bonjour " + user.getPrenom() + ",\n\n"
                + "Merci d'avoir créé un compte sur ThriftBy.\n"
                + "Pour activer votre compte, cliquez sur le lien suivant :\n"
                + verificationUrl + "\n\n"
                + "Si vous n'êtes pas à l'origine de cette demande, ignorez ce message.\n\n"
                + "Cordialement,\n"
                + "L'équipe ThriftBy");

        try {
            mailSender.send(message);
            log.info("✅ Email de vérification envoyé à {}", user.getEmail());
        } catch (MailException ex) {
            log.warn("⚠️ Impossible d'envoyer l'email de vérification à {}. Lien de vérification : {}", user.getEmail(), verificationUrl, ex);
        }
    }
}
