package com.nexus.backend.dto;

import lombok.Data;

@Data
public class ScorecardRequest {
    private Long applicationId;
    private String competency;
    private Integer rating;
    private String notes;
}