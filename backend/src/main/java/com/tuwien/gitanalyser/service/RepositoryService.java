package com.tuwien.gitanalyser.service;

import com.tuwien.gitanalyser.endpoints.dtos.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import org.gitlab4j.api.GitLabApiException;

import java.io.IOException;
import java.util.List;

public interface RepositoryService {
    List<NotSavedRepositoryInternalDTO> getAllRepositories(Long userId);

    List<BranchInternalDTO> getAllBranches(Long userId, Long platformId) throws GitLabApiException, IOException;

    NotSavedRepositoryInternalDTO getRepositoryById(Long userId, Long platformId)
        throws GitLabApiException, IOException;

    List<CommitInternalDTO> getAllCommits(long userId, Long platformId, String branch)
        throws GitLabApiException, IOException;
}
