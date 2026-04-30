package com.nexus.backend.repository;

import com.nexus.backend.model.Application;
import com.nexus.backend.model.JobDescription;
import com.nexus.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByJob(JobDescription job);

    List<Application> findByCandidate(User candidate);

    List<Application> findByJobAndStage(JobDescription job, Application.Stage stage);

    long countByJobAndStage(JobDescription job, Application.Stage stage);

    @Query("SELECT a FROM Application a WHERE a.job.id = :jobId ORDER BY a.matchScore DESC NULLS LAST")
    List<Application> findByJobIdOrderByMatchScoreDesc(@Param("jobId") Long jobId);

    @Query("SELECT a FROM Application a WHERE a.candidate.id = :candidateId ORDER BY a.appliedAt DESC")
    List<Application> findByCandidateIdOrderByAppliedAtDesc(@Param("candidateId") String candidateId);
}