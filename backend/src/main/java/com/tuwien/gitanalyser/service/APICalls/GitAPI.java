package com.tuwien.gitanalyser.service.APICalls;

import com.tuwien.gitanalyser.endpoints.DTOs.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.DTOs.internal.NotSavedRepositoryInternalDTO;
import org.gitlab4j.api.GitLabApiException;

import java.io.IOException;
import java.util.List;

public interface GitAPI {
    List<NotSavedRepositoryInternalDTO> getAllRepositories(String tokenValue) throws Exception;

    NotSavedRepositoryInternalDTO getRepositoryById(String accessToken, long id) throws GitLabApiException, IOException;

    List<BranchInternalDTO> getAllBranches(String accessToken, Long id) throws IOException, GitLabApiException;
}
