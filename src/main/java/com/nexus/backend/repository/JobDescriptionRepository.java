// JobDescriptionRepository.java
package com.nexus.backend.repository;

import com.nexus.backend.model.JobDescription;
import com.nexus.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobDescriptionRepository extends JpaRepository<JobDescription, Long> {
    List<JobDescription> findByStatus(JobDescription.Status status);
    List<JobDescription> findByCreatedBy(User user);
}