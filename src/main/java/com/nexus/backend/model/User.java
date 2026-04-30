package com.nexus.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@Entity                      // tells JPA this is a database table
@Table(name = "users")       // table name in PostgreSQL
@Data                        // Lombok: generates getters, setters, toString
@NoArgsConstructor           // Lombok: empty constructor
@AllArgsConstructor          // Lombok: constructor with all fields
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})// Lombok: User.builder().email("x").build()
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;          // stored as bcrypt hash, never plain text

    @Column(nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum Role {
        ADMIN,
        RECRUITER,
        HIRING_MANAGER,
        CANDIDATE
    }
}