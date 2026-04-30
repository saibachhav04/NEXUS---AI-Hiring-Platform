package com.nexus.backend.dto;

import com.nexus.backend.model.Application;
import lombok.Data;

@Data
public class PipelineUpdateRequest {
    private Long applicationId;
    private Application.Stage newStage;
}