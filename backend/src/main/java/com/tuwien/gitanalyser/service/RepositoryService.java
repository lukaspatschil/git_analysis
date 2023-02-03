package com.tuwien.gitanalyser.service;

import com.tuwien.gitanalyser.endpoints.DTOs.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.DTOs.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.entity.SavedRepository;

import java.util.List;

public interface RepositoryService {
    List<NotSavedRepositoryInternalDTO> getAllRepositories(Long userId);

    SavedRepository getRepositoryById(Long userId, Long id);

    List<BranchInternalDTO> getAllBranches(Long userId, Long id);
}
