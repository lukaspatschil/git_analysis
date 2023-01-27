package com.tuwien.gitanalyser.service.APICalls;

import com.tuwien.gitanalyser.entity.Repository;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GitHubAPI implements GitAPI {

    private final GitAPIFactory<GitHub> gitHubAPIFactory;

    public GitHubAPI(final GitAPIFactory<GitHub> gitHubAPIFactory) {
        this.gitHubAPIFactory = gitHubAPIFactory;
    }

    public List<Repository> getAllRepositories(final String accessToken) throws IOException {

        GitHub github = gitHubAPIFactory.createObject(accessToken);
        return github.getMyself().getAllRepositories().values().stream()
                     .map(repo -> new Repository(repo.getId(), repo.getName(), repo.getHttpTransportUrl()))
                     .collect(Collectors.toList());
    }

    @Override
    public Repository getRepositoryById(final String accessToken, final long plattformId) throws IOException {
        GitHub github = gitHubAPIFactory.createObject(accessToken);
        GHRepository repository = github.getRepositoryById(plattformId);
        return new Repository(repository.getId(), repository.getName(), repository.getHttpTransportUrl());
    }
}
