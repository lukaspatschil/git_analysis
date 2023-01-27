package com.tuwien.gitanalyser.service;

import com.tuwien.gitanalyser.entity.Repository;

import java.util.List;

public interface RepositoryService {
    List<Repository> getAllRepositories(Long userId);

    Repository getRepositoryById(Long userId, Long id);
}
