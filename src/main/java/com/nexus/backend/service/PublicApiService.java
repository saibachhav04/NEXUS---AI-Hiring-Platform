package com.nexus.backend.service;

import com.nexus.backend.model.ApiKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PublicApiService {

    @Autowired
    private ClaudeService claudeService;

    @Autowired
    private ApiKeyService apiKeyService;

    public Map<String, Object> biasCheck(
            String apiKeyValue, String jobDescription) {

        apiKeyService.validateAndIncrement(apiKeyValue);

        String prompt = """
                Analyze this job description for hiring bias.
                Return ONLY this exact JSON:
                {
                  "biasScore": <0-100>,
                  "genderedWords": ["word1", "word2"],
                  "legalRisks": ["phrase1"],
                  "suggestions": ["suggestion1", "suggestion2"],
                  "inclusivityTips": ["tip1", "tip2"],
                  "summary": "<one sentence verdict>"
                }
                Job Description: """ + jobDescription;

        return claudeService.askForObject(prompt, Map.class);
    }

    public Map<String, Object> resumeScore(
            String apiKeyValue,
            String resumeText,
            String jobDescription) {

        apiKeyService.validateAndIncrement(apiKeyValue);

        String prompt = """
                Score this candidate resume against the job description.
                Return ONLY this exact JSON:
                {
                  "matchScore": <0-100>,
                  "strengths": ["strength1", "strength2"],
                  "gaps": ["gap1", "gap2"],
                  "verdict": "<Strong Fit|Possible Fit|Not a Fit>",
                  "verdictReason": "<one sentence>",
                  "interviewFocus": ["area1", "area2"]
                }
                Job Description: %s
                Resume: %s
                """.formatted(jobDescription, resumeText);

        return claudeService.askForObject(prompt, Map.class);
    }

    public Map<String, Object> interviewQuestions(
            String apiKeyValue,
            String jobRole,
            String candidateSkills,
            String experienceLevel) {

        apiKeyService.validateAndIncrement(apiKeyValue);

        String prompt = """
                Generate interview questions for this candidate profile.
                Return ONLY this exact JSON:
                {
                  "technical": ["q1", "q2", "q3"],
                  "behavioral": ["q1", "q2"],
                  "gapBased": ["q1"],
                  "situational": ["q1", "q2"],
                  "focusArea": "<one sentence advice>"
                }
                Job Role: %s
                Candidate Skills: %s
                Experience Level: %s
                """.formatted(jobRole, candidateSkills, experienceLevel);

        return claudeService.askForObject(prompt, Map.class);
    }

    public Map<String, Object> salaryCheck(
            String apiKeyValue,
            String jobRole,
            String location,
            String experienceYears,
            String offeredSalary) {

        apiKeyService.validateAndIncrement(apiKeyValue);

        String prompt = """
                Analyze if this salary offer is fair for the role.
                Return ONLY this exact JSON:
                {
                  "fairnessScore": <0-100>,
                  "verdict": "<Fair|Below Market|Above Market|Exploitation Risk>",
                  "marketRangeMin": "<amount in INR>",
                  "marketRangeMax": "<amount in INR>",
                  "recommendation": "<one sentence advice for candidate>",
                  "negotiationTip": "<specific tip to negotiate>"
                }
                Job Role: %s
                Location: %s
                Experience: %s years
                Offered Salary: %s per year
                """.formatted(jobRole, location, experienceYears, offeredSalary);

        return claudeService.askForObject(prompt, Map.class);
    }
}