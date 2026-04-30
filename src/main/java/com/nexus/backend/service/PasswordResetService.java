package com.nexus.backend.service;

import com.nexus.backend.model.PasswordResetToken;
import com.nexus.backend.model.User;
import com.nexus.backend.repository.PasswordResetTokenRepository;
import com.nexus.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Transactional
    public void sendResetLink(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No account found with this email"));

        // delete old tokens for this user
        tokenRepository.deleteByUserId(user.getId());

        // generate new token
        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .used(false)
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build();

        tokenRepository.save(resetToken);

        String resetLink = frontendUrl
                + "/reset-password?token=" + token;

        emailService.sendPasswordResetEmail(
                user.getEmail(),
                user.getFullName(),
                resetLink);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository
                .findByTokenAndUsedFalse(token)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invalid or expired reset link"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException(
                    "Reset link has expired. Please request a new one.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }
}