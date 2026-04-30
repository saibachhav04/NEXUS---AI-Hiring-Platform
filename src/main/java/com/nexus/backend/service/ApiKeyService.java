package com.nexus.backend.service;

import com.nexus.backend.dto.ApiKeyResponse;
import com.nexus.backend.model.ApiKey;
import com.nexus.backend.model.User;
import com.nexus.backend.repository.ApiKeyRepository;
import com.nexus.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ApiKeyService {

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private UserRepository userRepository;

    public ApiKeyResponse generateKey(String userEmail, String keyName) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String rawKey = "nxs_live_" + UUID.randomUUID()
                .toString().replace("-", "");

        ApiKey apiKey = ApiKey.builder()
                .keyValue(rawKey)
                .keyName(keyName)
                .owner(user)
                .callsUsed(0L)
                .callsLimit(100L)
                .active(true)
                .build();

        ApiKey saved = apiKeyRepository.save(apiKey);
        return toResponse(saved);
    }

    public List<ApiKeyResponse> getMyKeys(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return apiKeyRepository.findByOwner(user)
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ApiKey validateAndIncrement(String keyValue) {
        if (keyValue.equals("PREVIEW_KEY")) {
            return new ApiKey(); // return empty key for preview
        }
        ApiKey key = apiKeyRepository
                .findByKeyValueAndActiveTrue(keyValue)
                .orElseThrow(() ->
                        new RuntimeException("Invalid or inactive API key"));

        if (key.getCallsUsed() >= key.getCallsLimit()) {
            throw new RuntimeException(
                    "API key rate limit exceeded. Upgrade your plan.");
        }

        key.setCallsUsed(key.getCallsUsed() + 1);
        return apiKeyRepository.save(key);
    }

    public void revokeKey(Long keyId, String userEmail) {
        ApiKey key = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new RuntimeException("Key not found"));
        if (!key.getOwner().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized");
        }
        key.setActive(false);
        apiKeyRepository.save(key);
    }

    private ApiKeyResponse toResponse(ApiKey key) {
        return ApiKeyResponse.builder()
                .id(key.getId())
                .keyValue(key.getKeyValue())
                .keyName(key.getKeyName())
                .callsUsed(key.getCallsUsed())
                .callsLimit(key.getCallsLimit())
                .active(key.isActive())
                .createdAt(key.getCreatedAt())
                .build();
    }
}