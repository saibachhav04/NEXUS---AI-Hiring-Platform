package com.nexus.backend.service;

import com.nexus.backend.dto.JobRequest;
import com.nexus.backend.model.JobDescription;
import com.nexus.backend.model.User;
import com.nexus.backend.repository.JobDescriptionRepository;
import com.nexus.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class JobService {

    @Autowired
    private JobDescriptionRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClaudeService claudeService;

    public JobDescription createJob(JobRequest request, String recruiterEmail) {

        User recruiter = userRepository.findByEmail(recruiterEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String biasPrompt = """
                Analyze this job description for hiring bias.
                Return ONLY this exact JSON structure with no extra text:
                {
                  "biasScore": <number 0-100>,
                  "genderedWords": [<list of biased words found>],
                  "suggestions": [<list of improvement suggestions>],
                  "legalRisks": [<list of legally risky phrases>],
                  "summary": "<one sentence verdict>"
                }
                
                Job Description:
                """ + request.getDescription();

        String biasJson = claudeService.ask(biasPrompt);

        int biasScore = 0;
        try {
            Map result = claudeService.askForObject(biasPrompt, Map.class);
            Object score = result.get("biasScore");
            if (score != null) {
                biasScore = Integer.parseInt(score.toString());
            }
        } catch (Exception e) {
            biasScore = 0;
        }

        JobDescription.Status status = biasScore > 60
                ? JobDescription.Status.NEEDS_REVIEW
                : JobDescription.Status.DRAFT;

        JobDescription job = JobDescription.builder()
                .title(request.getTitle())
                .department(request.getDepartment())
                .location(request.getLocation())
                .description(request.getDescription())
                .biasScore(biasScore)
                .biasAnalysisJson(biasJson)
                .status(status)
                .createdBy(recruiter)
                .build();

        return jobRepository.save(job);
    }

    public List<JobDescription> getAllPublishedJobs() {
        return jobRepository.findByStatus(JobDescription.Status.PUBLISHED);
    }

    public List<JobDescription> getMyJobs(String recruiterEmail) {
        User recruiter = userRepository.findByEmail(recruiterEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return jobRepository.findByCreatedBy(recruiter);
    }

    public JobDescription publishJob(Long jobId, String recruiterEmail) {
        JobDescription job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus(JobDescription.Status.PUBLISHED);
        return jobRepository.save(job);
    }
}