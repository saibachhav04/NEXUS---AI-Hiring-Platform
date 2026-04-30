package com.nexus.backend.controller;

import com.nexus.backend.dto.ApiKeyResponse;
import com.nexus.backend.service.ApiKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/developer")
public class ApiKeyController {

    @Autowired
    private ApiKeyService apiKeyService;

    @PostMapping("/keys")
    public ResponseEntity<ApiKeyResponse> generateKey(
            @RequestBody Map<String, String> body,
            Authentication auth) {
        return ResponseEntity.ok(
                apiKeyService.generateKey(
                        auth.getName(),
                        body.getOrDefault("keyName", "My API Key")));
    }

    @GetMapping("/keys")
    public ResponseEntity<List<ApiKeyResponse>> getMyKeys(
            Authentication auth) {
        return ResponseEntity.ok(
                apiKeyService.getMyKeys(auth.getName()));
    }

    @DeleteMapping("/keys/{keyId}")
    public ResponseEntity<Map<String, String>> revokeKey(
            @PathVariable Long keyId,
            Authentication auth) {
        apiKeyService.revokeKey(keyId, auth.getName());
        return ResponseEntity.ok(
                Map.of("message", "Key revoked successfully"));
    }
}