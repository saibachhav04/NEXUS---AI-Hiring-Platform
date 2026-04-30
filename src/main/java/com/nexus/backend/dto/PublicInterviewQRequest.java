package com.nexus.backend.dto;

import lombok.Data;

@Data
public class PublicInterviewQRequest {
    private String jobRole;
    private String candidateSkills;
    private String experienceLevel;
}