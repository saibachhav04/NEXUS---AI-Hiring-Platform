package com.nexus.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "job_descriptions")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class JobDescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String department;

    private String location;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;           // the full job post text

    private Integer biasScore;            // 0-100, filled by AI

    @Column(columnDefinition = "TEXT")
    private String biasAnalysisJson;      // AI response saved as JSON string

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum Status {
        DRAFT,           // just created, not checked yet
        NEEDS_REVIEW,    // bias score is high
        PUBLISHED,       // live, candidates can see it
        CLOSED           // no longer accepting applications
    }
}