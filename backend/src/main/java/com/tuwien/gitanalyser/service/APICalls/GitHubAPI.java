package com.tuwien.gitanalyser.service.APICalls;

import com.tuwien.gitanalyser.endpoints.DTOs.RepositoryDTO;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GitHubAPI implements GitAPI {

    private final GitHubAPIFactory gitHubAPIFactory;

    public GitHubAPI(final GitHubAPIFactory gitHubAPIFactory) {
        this.gitHubAPIFactory = gitHubAPIFactory;
    }

    public List<RepositoryDTO> getAllRepositories(final String accessToken) throws IOException {

        GitHub github = gitHubAPIFactory.createObject(accessToken);
        return github.getMyself().getAllRepositories().values().stream()
                     .map(repo -> new RepositoryDTO(repo.getId(), repo.getName(), repo.getHttpTransportUrl()))
                     .collect(Collectors.toList());
    }

    @Override
    public RepositoryDTO getRepositoryById(final String accessToken, final long plattformId) throws IOException {
        GitHub github = gitHubAPIFactory.createObject(accessToken);
        GHRepository repository = github.getRepositoryById(plattformId);
        return new RepositoryDTO(repository.getId(), repository.getName(), repository.getHttpTransportUrl());
    }
}
