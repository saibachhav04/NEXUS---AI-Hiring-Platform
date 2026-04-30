package com.nexus.backend.controller;

import com.nexus.backend.dto.*;
import com.nexus.backend.service.PublicApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1")
public class PublicApiController {

    @Autowired
    private PublicApiService publicApiService;

    private String extractKey(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException(
                    "Missing API key. Add header: Authorization: Bearer nxs_live_xxx");
        }
        return authHeader.substring(7);
    }

    @PostMapping("/bias-check")
    public ResponseEntity<Map<String, Object>> biasCheck(
            @RequestHeader("Authorization") String auth,
            @RequestBody PublicBiasRequest request) {
        String key = extractKey(auth);
        return ResponseEntity.ok(
                publicApiService.biasCheck(
                        key, request.getJobDescription()));
    }

    @PostMapping("/resume-score")
    public ResponseEntity<Map<String, Object>> resumeScore(
            @RequestHeader("Authorization") String auth,
            @RequestBody PublicResumeScoreRequest request) {
        String key = extractKey(auth);
        return ResponseEntity.ok(
                publicApiService.resumeScore(
                        key,
                        request.getResumeText(),
                        request.getJobDescription()));
    }

    @PostMapping("/interview-questions")
    public ResponseEntity<Map<String, Object>> interviewQuestions(
            @RequestHeader("Authorization") String auth,
            @RequestBody PublicInterviewQRequest request) {
        String key = extractKey(auth);
        return ResponseEntity.ok(
                publicApiService.interviewQuestions(
                        key,
                        request.getJobRole(),
                        request.getCandidateSkills(),
                        request.getExperienceLevel()));
    }

    @PostMapping("/salary-check")
    public ResponseEntity<Map<String, Object>> salaryCheck(
            @RequestHeader("Authorization") String auth,
            @RequestBody Map<String, String> body) {
        String key = extractKey(auth);
        return ResponseEntity.ok(
                publicApiService.salaryCheck(
                        key,
                        body.get("jobRole"),
                        body.get("location"),
                        body.get("experienceYears"),
                        body.get("offeredSalary")));
    }
}