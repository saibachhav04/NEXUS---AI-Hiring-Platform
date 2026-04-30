// InterviewScorecardRepository.java
package com.nexus.backend.repository;

import com.nexus.backend.model.InterviewScorecard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InterviewScorecardRepository extends JpaRepository<InterviewScorecard, Long> {
    List<InterviewScorecard> findByApplicationId(Long applicationId);
}
