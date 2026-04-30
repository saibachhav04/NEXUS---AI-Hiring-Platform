package com.nexus.backend.controller;

import com.nexus.backend.model.Application;
import com.nexus.backend.repository.JobDescriptionRepository;
import com.nexus.backend.service.ClaudeService;
import com.nexus.backend.service.ResumeService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private JobDescriptionRepository jobDescriptionRepository;

    @Autowired
    private ClaudeService claudeService;

    @PostMapping("/upload/{jobId}")
    public ResponseEntity<Application> uploadResume(
            @RequestParam("file") MultipartFile file,
            @PathVariable Long jobId,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(
                resumeService.uploadAndAnalyze(file, jobId, email));
    }

    @PostMapping("/preview-score")
    public ResponseEntity<Map<String, Object>> previewScore(
            @RequestParam("file") MultipartFile file,
            @RequestParam("jobId") Long jobId) {
        try {
            byte[] bytes = file.getBytes();
            PDDocument doc = Loader.loadPDF(bytes);
            PDFTextStripper stripper = new PDFTextStripper();
            String resumeText = stripper.getText(doc);
            doc.close();

            com.nexus.backend.model.JobDescription job =
                    jobDescriptionRepository.findById(jobId)
                            .orElseThrow(() ->
                                    new RuntimeException("Job not found"));

            String prompt = """
                    Score this resume against the job description.
                    Return ONLY this exact JSON with no extra text:
                    {
                      "matchScore": <number 0-100>,
                      "verdict": "<Strong Fit|Possible Fit|Not a Fit>",
                      "strengths": ["strength1", "strength2"],
                      "gaps": ["gap1", "gap2"],
                      "certificationSuggestions": ["cert1", "cert2"],
                      "verdictReason": "<one sentence>"
                    }
                    Job Description: %s
                    Resume Text: %s
                    """.formatted(
                    job.getDescription(), resumeText);

            Map<String, Object> result =
                    claudeService.askForObject(prompt, Map.class);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<Application>> getApplicationsForJob(
            @PathVariable Long jobId) {
        return ResponseEntity.ok(
                resumeService.getApplicationsForJob(jobId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<Application>> getMyApplications(
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(
                resumeService.getMyApplications(email));
    }
}