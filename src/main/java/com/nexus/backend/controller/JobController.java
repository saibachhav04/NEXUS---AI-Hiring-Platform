package com.nexus.backend.controller;

import com.nexus.backend.dto.JobRequest;
import com.nexus.backend.model.JobDescription;
import com.nexus.backend.service.JobService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    @PostMapping
    public ResponseEntity<JobDescription> createJob(
            @Valid @RequestBody JobRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(jobService.createJob(request, email));
    }

    @GetMapping
    public ResponseEntity<List<JobDescription>> getPublishedJobs() {
        return ResponseEntity.ok(jobService.getAllPublishedJobs());
    }

    @GetMapping("/my")
    public ResponseEntity<List<JobDescription>> getMyJobs(
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(jobService.getMyJobs(email));
    }

    @PutMapping("/{jobId}/publish")
    public ResponseEntity<JobDescription> publishJob(
            @PathVariable Long jobId,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(jobService.publishJob(jobId, email));
    }
}