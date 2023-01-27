package com.tuwien.gitanalyser.service.APICalls;

import com.tuwien.gitanalyser.entity.Repository;
import org.gitlab4j.api.GitLabApiException;

import java.io.IOException;
import java.util.List;

public interface GitAPI {
    List<Repository> getAllRepositories(String tokenValue) throws Exception;

    Repository getRepositoryById(String accessToken, long id) throws GitLabApiException, IOException;
}
