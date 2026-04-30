package com.nexus.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.backend.model.Application;
import com.nexus.backend.model.JobDescription;
import com.nexus.backend.model.User;
import com.nexus.backend.repository.ApplicationRepository;
import com.nexus.backend.repository.JobDescriptionRepository;
import com.nexus.backend.repository.UserRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ResumeService {

    @Autowired
    private MinioService minioService;
    @Autowired
    private EmailService emailService;

    @Autowired
    private ClaudeService claudeService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private JobDescriptionRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Application uploadAndAnalyze(
            MultipartFile file,
            Long jobId,
            String candidateEmail) {

        // Step 1 — upload PDF to MinIO
        String fileKey = minioService.uploadFile(file);

        // Step 2 — extract text from PDF
        String resumeText = extractTextFromPdf(file);

        // Step 3 — AI parses resume structure
        String parsePrompt = """
                Parse this resume and return ONLY this exact JSON:
                {
                  "name": "<candidate name>",
                  "email": "<email>",
                  "phone": "<phone>",
                  "totalYearsExperience": <number>,
                  "skills": ["skill1", "skill2"],
                  "education": [
                    {
                      "degree": "<degree>",
                      "field": "<field>",
                      "institution": "<institution>",
                      "year": <year>
                    }
                  ],
                  "workHistory": [
                    {
                      "company": "<company>",
                      "role": "<role>",
                      "years": <number>
                    }
                  ],
                  "keyStrengths": ["strength1", "strength2"],
                  "potentialConcerns": ["concern1"]
                }
                
                Resume Text:
                """ + resumeText;

        String parsedJson = claudeService.ask(parsePrompt);

        // Step 4 — get job description
        JobDescription job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new RuntimeException("Job not found"));

        // Step 5 — AI scores resume against job
        String matchPrompt = """
                Score this candidate against the job description.
                Return ONLY this exact JSON:
                {
                  "matchScore": <number 0-100>,
                  "strengths": ["what makes them a fit"],
                  "gaps": ["what is missing"],
                  "interviewFocus": ["areas to probe in interview"],
                  "verdict": "<Strong Fit | Possible Fit | Not a Fit>",
                  "verdictReason": "<one sentence explanation>"
                }
                
                Job Description:
                """ + job.getDescription() + """
                
                Candidate Resume:
                """ + resumeText;

        String matchJson = claudeService.ask(matchPrompt);

        // Step 6 — extract match score
        int matchScore = 0;
        try {
            JsonNode node = objectMapper.readTree(matchJson);
            matchScore = node.get("matchScore").asInt();
        } catch (Exception e) {
            matchScore = 0;
        }

        // Step 7 — get candidate user
        User candidate = userRepository.findByEmail(candidateEmail)
                .orElseThrow(() ->
                        new RuntimeException("Candidate not found"));

        // Step 8 — save application
        Application application = Application.builder()
                .job(job)
                .candidate(candidate)
                .resumeFileKey(fileKey)
                .parsedResumeJson(parsedJson)
                .matchScore(matchScore)
                .matchAnalysisJson(matchJson)
                .stage(Application.Stage.APPLIED)
                .build();

        Application saved = applicationRepository.save(application);
        try {
            emailService.sendApplicationReceived(saved);
        } catch (Exception e) {
            System.err.println("Email error: " + e.getMessage());
        }
        return saved;
    }

    public List<Application> getApplicationsForJob(Long jobId) {
        JobDescription job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new RuntimeException("Job not found"));
        return applicationRepository.findByJob(job);
    }

    public List<Application> getMyApplications(String candidateEmail) {
        User candidate = userRepository.findByEmail(candidateEmail)
                .orElseThrow(() ->
                        new RuntimeException("Candidate not found"));
        return applicationRepository.findByCandidate(candidate);
    }

    private String extractTextFromPdf(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            PDDocument document = Loader.loadPDF(bytes);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            document.close();
            return text;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to read PDF: " + e.getMessage());
        }
    }
}