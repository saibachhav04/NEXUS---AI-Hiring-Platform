package com.nexus.backend.service;

import com.nexus.backend.dto.InterviewQuestionsResponse;
import com.nexus.backend.dto.ScorecardRequest;
import com.nexus.backend.model.Application;
import com.nexus.backend.model.InterviewScorecard;
import com.nexus.backend.model.User;
import com.nexus.backend.repository.ApplicationRepository;
import com.nexus.backend.repository.InterviewScorecardRepository;
import com.nexus.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScorecardService {

    @Autowired
    private InterviewScorecardRepository scorecardRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClaudeService claudeService;

    // Generate interview questions for a candidate
    public InterviewQuestionsResponse generateQuestions(
            Long applicationId) {

        Application application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() ->
                        new RuntimeException("Application not found"));

        String prompt = """
                Generate interview questions for this candidate.
                Return ONLY this exact JSON:
                {
                  "technical": [
                    "<technical question 1>",
                    "<technical question 2>",
                    "<technical question 3>"
                  ],
                  "behavioral": [
                    "<behavioral question 1>",
                    "<behavioral question 2>"
                  ],
                  "gapBased": [
                    "<question targeting a gap in their resume>"
                  ],
                  "focusArea": "<one sentence on what to focus on>"
                }
                
                Job Title: %s
                Job Description: %s
                Candidate Resume: %s
                Match Analysis: %s
                """.formatted(
                application.getJob().getTitle(),
                application.getJob().getDescription(),
                application.getParsedResumeJson(),
                application.getMatchAnalysisJson()
        );

        return claudeService.askForObject(
                prompt, InterviewQuestionsResponse.class);
    }

    // Submit scorecard after interview
    public InterviewScorecard submitScorecard(
            ScorecardRequest request,
            String interviewerEmail) {

        Application application = applicationRepository
                .findById(request.getApplicationId())
                .orElseThrow(() ->
                        new RuntimeException("Application not found"));

        User interviewer = userRepository
                .findByEmail(interviewerEmail)
                .orElseThrow(() ->
                        new RuntimeException("Interviewer not found"));

        InterviewScorecard scorecard = InterviewScorecard.builder()
                .application(application)
                .interviewer(interviewer)
                .competency(request.getCompetency())
                .rating(request.getRating())
                .notes(request.getNotes())
                .build();

        return scorecardRepository.save(scorecard);
    }

    // Generate final AI verdict from all scorecards
    public String generateVerdict(Long applicationId) {

        List<InterviewScorecard> scorecards = scorecardRepository
                .findByApplicationId(applicationId);

        if (scorecards.isEmpty()) {
            throw new RuntimeException(
                    "No scorecards found for this application");
        }

        StringBuilder scoresContext = new StringBuilder();
        for (InterviewScorecard s : scorecards) {
            scoresContext.append(String.format(
                    "%s rated %s: %d/5 — \"%s\"\n",
                    s.getInterviewer().getFullName(),
                    s.getCompetency(),
                    s.getRating(),
                    s.getNotes()
            ));
        }

        String prompt = """
                Synthesize these interview evaluations into a verdict.
                Return ONLY this exact JSON:
                {
                  "verdict": "<Strong Hire | Hire | No Hire | Strong No Hire>",
                  "confidence": <number 0-100>,
                  "topStrength": "<one sentence>",
                  "topConcern": "<one sentence>",
                  "consensusAreas": ["area1", "area2"],
                  "recommendation": "<two sentence final recommendation>"
                }
                
                Evaluations:
                %s
                """.formatted(scoresContext.toString());

        String verdictJson = claudeService.ask(prompt);

        // save verdict to all scorecards for this application
        for (InterviewScorecard s : scorecards) {
            s.setAiVerdictJson(verdictJson);
            scorecardRepository.save(s);
        }

        return verdictJson;
    }

    // Get all scorecards for an application
    public List<InterviewScorecard> getScorecards(
            Long applicationId) {
        return scorecardRepository
                .findByApplicationId(applicationId);
    }
}