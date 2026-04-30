package com.nexus.backend.service;

import com.nexus.backend.dto.PipelineEvent;
import com.nexus.backend.model.Application;
import com.nexus.backend.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PipelineService {

    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private EmailService emailService;

    @Autowired
    private ClaudeService claudeService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public Application moveCandidate(
            Long applicationId,
            Application.Stage newStage,
            String recruiterEmail) {

        Application application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        Application.Stage oldStage = application.getStage();
        application.setStage(newStage);
        Application saved = applicationRepository.save(application); // ✅ FIXED: save and assign to 'saved'

        String aiInsight = null;
        if (newStage == Application.Stage.INTERVIEW
                || newStage == Application.Stage.FINAL_ROUND) {
            try {
                String prompt = """
                        A candidate is being moved to %s stage.
                        Their match score is %d out of 100.
                        Their match analysis: %s
                        Give ONE short sentence of advice for the interviewer.
                        Return ONLY the sentence, no JSON.
                        """.formatted(
                        newStage.name(),
                        application.getMatchScore(),
                        application.getMatchAnalysisJson()
                );
                aiInsight = claudeService.ask(prompt);
            } catch (Exception e) {
                aiInsight = "Focus on technical skills and cultural fit.";
            }
        }

        PipelineEvent event = PipelineEvent.builder()
                .applicationId(applicationId)
                .candidateName(application.getCandidate().getFullName())
                .jobTitle(application.getJob().getTitle())
                .oldStage(oldStage)
                .newStage(newStage)
                .movedBy(recruiterEmail)
                .aiInsight(aiInsight)
                .build();

        messagingTemplate.convertAndSend(
                "/topic/pipeline/" + application.getJob().getId(),
                event
        );

        try {
            if (newStage == Application.Stage.SCREENING) {
                emailService.sendScreeningEmail(saved);
            } else if (newStage == Application.Stage.INTERVIEW) {
                emailService.sendInterviewInvitation(saved);
            } else if (newStage == Application.Stage.FINAL_ROUND) {
                emailService.sendFinalRoundEmail(saved);
            } else if (newStage == Application.Stage.OFFER) {
                emailService.sendOfferEmail(saved);
            } else if (newStage == Application.Stage.REJECTED) {
                emailService.sendRejectionEmail(saved);
            }
        } catch (Exception e) {
            System.err.println("Email error: " + e.getMessage());
        }

        return saved; // ✅ FIXED: return the saved application
    }

    public List<Application> getPipelineForJob(Long jobId) {
        return applicationRepository.findByJobIdOrderByMatchScoreDesc(jobId);
    }
}