package com.nexus.backend.dto;

import com.nexus.backend.model.Application;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PipelineEvent {
    private Long applicationId;
    private String candidateName;
    private String jobTitle;
    private Application.Stage oldStage;
    private Application.Stage newStage;
    private String movedBy;
    private String aiInsight;
}