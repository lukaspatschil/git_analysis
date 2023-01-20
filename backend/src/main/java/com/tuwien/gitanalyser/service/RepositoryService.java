package com.tuwien.gitanalyser.service;

import com.tuwien.gitanalyser.endpoints.DTOs.RepositoryDTO;

import java.util.List;

public interface RepositoryService {
    List<RepositoryDTO> getAllRepositories(Long userId);

    RepositoryDTO getRepositoryById(Long userId, Long id);
}
