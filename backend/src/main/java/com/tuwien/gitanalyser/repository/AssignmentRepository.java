package com.tuwien.gitanalyser.repository;

import com.tuwien.gitanalyser.entity.Assignment;
import com.tuwien.gitanalyser.entity.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@SuppressWarnings("checkstyle:methodname")
@org.springframework.stereotype.Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    @SuppressWarnings("checkstyle:methodname")
    Optional<Assignment> findByRepositoryAndSubAssignments_Id(Repository repository, Long id);

    Optional<Assignment> findByRepositoryAndKey(Repository repository, String key);
}
