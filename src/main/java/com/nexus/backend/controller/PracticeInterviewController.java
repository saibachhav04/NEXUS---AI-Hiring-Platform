package com.nexus.backend.controller;

import com.nexus.backend.dto.PracticeReportRequest;
import com.nexus.backend.model.PracticeSession;
import com.nexus.backend.service.PracticeInterviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/practice")
public class PracticeInterviewController {

    @Autowired
    private PracticeInterviewService practiceInterviewService;

    @GetMapping("/questions/{jobRole}")
    public ResponseEntity<Map<String, Object>> getQuestions(
            @PathVariable String jobRole) {
        return ResponseEntity.ok(
                practiceInterviewService.generateQuestions(jobRole));
    }

    @PostMapping("/analyze-answer")
    public ResponseEntity<Map<String, Object>> analyzeAnswer(
            @RequestBody PracticeReportRequest request) {
        return ResponseEntity.ok(
                practiceInterviewService.analyzeAnswer(request));
    }

    @PostMapping("/final-report")
    public ResponseEntity<Map<String, Object>> finalReport(
            @RequestBody Map<String, Object> body,
            Authentication auth) {
        String jobRole = (String) body.get("jobRole");
        List<Map<String, Object>> results =
                (List<Map<String, Object>>) body.get("results");
        return ResponseEntity.ok(
                practiceInterviewService.generateFinalReport(
                        auth.getName(), jobRole, results));
    }

    @GetMapping("/history")
    public ResponseEntity<List<PracticeSession>> getHistory(
            Authentication auth) {
        return ResponseEntity.ok(
                practiceInterviewService.getMyHistory(auth.getName()));
    }
}