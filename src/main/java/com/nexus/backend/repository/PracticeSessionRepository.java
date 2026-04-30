package com.nexus.backend.repository;

import com.nexus.backend.model.PracticeSession;
import com.nexus.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PracticeSessionRepository
        extends JpaRepository<PracticeSession, Long> {
    List<PracticeSession> findByCandidateOrderByCreatedAtDesc(User candidate);
}