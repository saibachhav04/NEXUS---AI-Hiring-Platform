package com.nexus.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JobRequest {

    @NotBlank(message = "Job title is required")
    private String title;

    private String department;

    private String location;

    @NotBlank(message = "Job description is required")
    private String description;
}