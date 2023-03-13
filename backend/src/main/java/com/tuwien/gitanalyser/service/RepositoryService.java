package com.tuwien.gitanalyser.service;

import com.tuwien.gitanalyser.endpoints.dtos.assignment.CreateAssignmentDTO;

public interface RepositoryService {

    void assignCommitter(long userId, Long platformId, CreateAssignmentDTO dto);



}
