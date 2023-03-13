package com.tuwien.gitanalyser.repository;

import com.tuwien.gitanalyser.entity.SubAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubAssignmentRepository extends JpaRepository<SubAssignment, Long> {
}
