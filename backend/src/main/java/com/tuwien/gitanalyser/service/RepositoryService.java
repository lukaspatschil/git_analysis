package com.tuwien.gitanalyser.service;

import com.tuwien.gitanalyser.endpoints.dtos.assignment.CreateAssignmentDTO;
import com.tuwien.gitanalyser.entity.Assignment;

import java.util.List;

public interface RepositoryService {

    void assignCommitter(long userId, Long platformId, CreateAssignmentDTO dto);

    List<Assignment> getAssignments(long userId, Long platformId);

    void deleteAssignment(Long userId, Long platformId, Long subAssignmentId);
}
