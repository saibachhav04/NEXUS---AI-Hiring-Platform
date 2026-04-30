package com.nexus.backend.controller;

import com.nexus.backend.dto.InterviewQuestionsResponse;
import com.nexus.backend.dto.ScorecardRequest;
import com.nexus.backend.model.InterviewScorecard;
import com.nexus.backend.service.ScorecardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scorecards")
public class ScorecardController {

    @Autowired
    private ScorecardService scorecardService;

    // generate AI interview questions
    @GetMapping("/questions/{applicationId}")
    public ResponseEntity<InterviewQuestionsResponse> getQuestions(
            @PathVariable Long applicationId) {
        return ResponseEntity.ok(
                scorecardService.generateQuestions(applicationId));
    }

    // submit scorecard after interview
    @PostMapping
    public ResponseEntity<InterviewScorecard> submitScorecard(
            @RequestBody ScorecardRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(
                scorecardService.submitScorecard(request, email));
    }

    // get AI final verdict
    @GetMapping("/verdict/{applicationId}")
    public ResponseEntity<String> getVerdict(
            @PathVariable Long applicationId) {
        return ResponseEntity.ok(
                scorecardService.generateVerdict(applicationId));
    }

    // get all scorecards for application
    @GetMapping("/{applicationId}")
    public ResponseEntity<List<InterviewScorecard>> getScorecards(
            @PathVariable Long applicationId) {
        return ResponseEntity.ok(
                scorecardService.getScorecards(applicationId));
    }
}