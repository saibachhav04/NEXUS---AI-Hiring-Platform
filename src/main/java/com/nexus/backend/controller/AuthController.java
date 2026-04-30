package com.nexus.backend.controller;

import com.nexus.backend.dto.AuthResponse;
import com.nexus.backend.dto.LoginRequest;
import com.nexus.backend.dto.RegisterRequest;
import com.nexus.backend.service.AuthService;
import com.nexus.backend.service.EmailService;
import com.nexus.backend.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @RequestBody Map<String, String> body) {
        try {
            passwordResetService.sendResetLink(body.get("email"));
            return ResponseEntity.ok(Map.of(
                    "message",
                    "Reset link sent to your email"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of(
                    "error", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestBody Map<String, String> body) {
        try {
            passwordResetService.resetPassword(
                    body.get("token"),
                    body.get("newPassword"));
            return ResponseEntity.ok(Map.of(
                    "message", "Password reset successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of(
                    "error", e.getMessage()));
        }
    }
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "NEXUS Backend"
        ));
    }

    @GetMapping("/test-email")
    public ResponseEntity<String> testEmail(
            @RequestParam String to) {
        try {
            emailService.sendEmail(
                    to,
                    "NEXUS Test Email",
                    "<h1 style='color:#9333EA'>NEXUS Email Working!</h1>"
                            + "<p>Your email service is configured correctly.</p>"
            );
            return ResponseEntity.ok(
                    "Email sent successfully to " + to);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    "Email failed: " + e.getMessage());
        }
    }
}