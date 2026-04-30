package com.nexus.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@Entity
@Table(name = "applications")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "job_id", nullable = false)
    private JobDescription job;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    private String resumeFileKey;         // the filename stored in MinIO

    @Column(columnDefinition = "TEXT")
    private String parsedResumeJson;      // AI extracted: skills, experience etc.

    private Integer matchScore;           // 0-100 how well they fit the job

    @Column(columnDefinition = "TEXT")
    private String matchAnalysisJson;     // AI explanation of the score

    @Enumerated(EnumType.STRING)
    private Stage stage;

    @CreationTimestamp
    private LocalDateTime appliedAt;

    public enum Stage {
        APPLIED,
        SCREENING,
        INTERVIEW,
        FINAL_ROUND,
        OFFER,
        REJECTED
    }
}