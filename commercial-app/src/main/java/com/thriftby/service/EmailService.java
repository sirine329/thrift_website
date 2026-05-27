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

    @Value("${spring.mail.password:}")
    private String mailPassword;

    public boolean sendVerificationEmail(User user) {
        if (user == null || user.getEmail() == null || user.getVerificationToken() == null) {
            return false;
        }

        String verificationUrl = appBaseUrl + "/verify?token=" + user.getVerificationToken();

        if (mailPassword == null || mailPassword.isBlank()) {
            log.warn("MAIL_PASSWORD est vide. Email non envoye. Lien de verification : {}", verificationUrl);
            return false;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setFrom(fromAddress);
        message.setSubject("Verifiez votre adresse email ThriftBy");
        message.setText("Bonjour " + user.getPrenom() + ",\n\n"
                + "Merci d'avoir cree un compte sur ThriftBy.\n"
                + "Compte a verifier : " + user.getEmail() + "\n\n"
                + "Pour activer votre compte, cliquez sur le lien suivant :\n"
                + verificationUrl + "\n\n"
                + "Si vous n'etes pas a l'origine de cette demande, ignorez ce message.\n\n"
                + "Cordialement,\n"
                + "L'equipe ThriftBy");

        try {
            mailSender.send(message);
            log.info("Email de verification envoye a {}", user.getEmail());
            return true;
        } catch (MailException ex) {
            log.warn("Impossible d'envoyer l'email de verification a {}. Lien de verification : {}",
                    user.getEmail(), verificationUrl, ex);
            return false;
        }
    }
}
