package com.nexus.backend.dto;

import lombok.Data;

@Data
public class PublicResumeScoreRequest {
    private String resumeText;
    private String jobDescription;
}