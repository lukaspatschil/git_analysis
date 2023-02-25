package com.tuwien.gitanalyser.service;

import com.tuwien.gitanalyser.endpoints.dtos.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import org.gitlab4j.api.GitLabApiException;

import java.io.IOException;
import java.util.List;

public interface GitAPI {
    List<NotSavedRepositoryInternalDTO> getAllRepositories(String tokenValue) throws IOException, GitLabApiException;

    NotSavedRepositoryInternalDTO getRepositoryById(String accessToken, long id) throws GitLabApiException, IOException;

    List<BranchInternalDTO> getAllBranches(String accessToken, Long id) throws IOException, GitLabApiException;

    List<CommitInternalDTO> getAllCommits(String accessToken, long id, String branchName)
        throws IOException, GitLabApiException;
}
