package com.nexus.backend.controller;

import com.nexus.backend.dto.PipelineUpdateRequest;
import com.nexus.backend.model.Application;
import com.nexus.backend.service.PipelineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pipeline")
public class PipelineController {

    @Autowired
    private PipelineService pipelineService;

    // REST endpoint — move candidate via HTTP
    @PutMapping("/move")
    public ResponseEntity<Application> moveCandidate(
            @RequestBody PipelineUpdateRequest request,
            Authentication authentication) {

        String email = authentication.getName();
        return ResponseEntity.ok(
                pipelineService.moveCandidate(
                        request.getApplicationId(),
                        request.getNewStage(),
                        email
                )
        );
    }

    // get all applications for a job grouped by stage
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<Application>> getPipeline(
            @PathVariable Long jobId) {
        return ResponseEntity.ok(
                pipelineService.getPipelineForJob(jobId));
    }

    // WebSocket endpoint — move candidate via WebSocket
    @MessageMapping("/pipeline/move")
    public void moveCandidateWS(
            @Payload PipelineUpdateRequest request,
            Authentication authentication) {

        String email = authentication != null
                ? authentication.getName()
                : "recruiter";

        pipelineService.moveCandidate(
                request.getApplicationId(),
                request.getNewStage(),
                email
        );
    }
}


