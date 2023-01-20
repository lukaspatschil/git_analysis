package com.tuwien.gitanalyser.service.APICalls;

import com.tuwien.gitanalyser.endpoints.DTOs.RepositoryDTO;
import org.gitlab4j.api.GitLabApiException;

import java.io.IOException;
import java.util.List;

public interface GitAPI {
    List<RepositoryDTO> getAllRepositories(String tokenValue) throws Exception;

    RepositoryDTO getRepositoryById(String accessToken, long id) throws GitLabApiException, IOException;
}
