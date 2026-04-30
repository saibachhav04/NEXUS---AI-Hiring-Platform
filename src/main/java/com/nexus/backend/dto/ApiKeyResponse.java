package com.nexus.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiKeyResponse {
    private Long id;
    private String keyValue;
    private String keyName;
    private Long callsUsed;
    private Long callsLimit;
    private boolean active;
    private LocalDateTime createdAt;
}