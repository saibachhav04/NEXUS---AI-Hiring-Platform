package com.nexus.backend.service;

import com.nexus.backend.dto.PracticeReportRequest;
import com.nexus.backend.model.PracticeSession;
import com.nexus.backend.model.User;
import com.nexus.backend.repository.PracticeSessionRepository;
import com.nexus.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PracticeInterviewService {

    @Autowired
    private ClaudeService claudeService;

    @Autowired
    private PracticeSessionRepository practiceSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public Map<String, Object> generateQuestions(String jobRole) {
        String prompt = """
                Generate a mock interview question set for practice.
                Return ONLY this exact JSON:
                {
                  "questions": [
                    {
                      "id": 1,
                      "question": "<question text>",
                      "type": "<Technical|Behavioral|Situational|HR>",
                      "tip": "<what to focus on in answer>",
                      "timeLimit": 60
                    }
                  ],
                  "totalQuestions": 5,
                  "estimatedMinutes": 8
                }
                Generate exactly 5 questions for: %s role
                Mix: 2 technical, 1 behavioral, 1 situational, 1 HR
                """.formatted(jobRole);

        return claudeService.askForObject(prompt, Map.class);
    }

    public Map<String, Object> analyzeAnswer(
            PracticeReportRequest request) {

        String prompt = """
                Evaluate this interview answer and provide feedback.
                Return ONLY this exact JSON:
                {
                  "answerScore": <0-100>,
                  "contentQuality": <0-100>,
                  "clarity": <0-100>,
                  "relevance": <0-100>,
                  "strengths": ["what was good"],
                  "improvements": ["what to improve"],
                  "idealAnswerStructure": "<STAR/brief/technical>",
                  "sampleAnswer": "<2-3 sentence example of better answer>"
                }
                Question: %s
                Candidate Answer (transcript): %s
                Job Role: %s
                """.formatted(
                request.getQuestion(),
                request.getTranscript(),
                request.getJobRole()
        );

        Map<String, Object> aiAnalysis =
                claudeService.askForObject(prompt, Map.class);

        aiAnalysis.put("eyeContactScore",
                request.getEyeContactScore());
        aiAnalysis.put("postureScore",
                request.getPostureScore());
        aiAnalysis.put("fillerWordCount",
                request.getFillerWordCount());
        aiAnalysis.put("speakingPaceWpm",
                request.getSpeakingPaceWpm());

        int gestureScore = calculateGestureScore(
                request.getEyeContactScore(),
                request.getPostureScore(),
                request.getFillerWordCount(),
                request.getSpeakingPaceWpm()
        );
        aiAnalysis.put("gestureScore", gestureScore);

        int overall = (
                (int) aiAnalysis.getOrDefault("answerScore", 70)
                        + gestureScore) / 2;
        aiAnalysis.put("overallScore", overall);

        return aiAnalysis;
    }

    public Map<String, Object> generateFinalReport(
            String userEmail,
            String jobRole,
            List<Map<String, Object>> questionResults) {

        String prompt = """
                Synthesize these mock interview results into a final report.
                Return ONLY this exact JSON:
                {
                  "overallReadinessScore": <0-100>,
                  "readinessLevel": "<Not Ready|Needs Practice|Good|Excellent>",
                  "topStrengths": ["strength1", "strength2"],
                  "criticalImprovements": ["improvement1", "improvement2"],
                  "gestureAdvice": ["tip1", "tip2", "tip3"],
                  "communicationAdvice": ["tip1", "tip2"],
                  "practiceRecommendations": ["recommendation1"],
                  "encouragement": "<one motivational sentence>"
                }
                Job Role: %s
                Results: %s
                """.formatted(jobRole, questionResults.toString());

        Map<String, Object> report =
                claudeService.askForObject(prompt, Map.class);

        try {
            User candidate = userRepository
                    .findByEmail(userEmail).orElse(null);
            if (candidate != null) {
                int score = (int) report
                        .getOrDefault("overallReadinessScore", 0);
                PracticeSession session = PracticeSession.builder()
                        .candidate(candidate)
                        .jobRole(jobRole)
                        .overallScore(score)
                        .reportJson(objectMapper
                                .writeValueAsString(report))
                        .build();
                practiceSessionRepository.save(session);
            }
        } catch (Exception ignored) {}

        return report;
    }

    public List<PracticeSession> getMyHistory(String userEmail) {
        User candidate = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
        return practiceSessionRepository
                .findByCandidateOrderByCreatedAtDesc(candidate);
    }

    private int calculateGestureScore(
            Integer eyeContact,
            Integer posture,
            Integer fillerWords,
            Integer wpm) {

        int eye = eyeContact != null ? eyeContact : 50;
        int post = posture != null ? posture : 50;
        int filler = fillerWords != null
                ? Math.max(0, 100 - (fillerWords * 5)) : 50;
        int pace = 50;
        if (wpm != null) {
            if (wpm >= 120 && wpm <= 160) pace = 100;
            else if (wpm >= 100 && wpm <= 180) pace = 75;
            else pace = 40;
        }
        return (eye + post + filler + pace) / 4;
    }
}