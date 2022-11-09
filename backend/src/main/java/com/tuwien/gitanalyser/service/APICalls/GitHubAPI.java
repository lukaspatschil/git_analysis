package com.tuwien.gitanalyser.service.APICalls;

import com.tuwien.gitanalyser.endpoints.DTOs.RepositoryDTO;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GitHubAPI implements GitAPI {

    public List<RepositoryDTO> getAllRepositories(final String accessToken) throws IOException {
        List<RepositoryDTO> allRepos = new ArrayList<>();

        GitHub github = getGitHub(accessToken);
        github.getMyself().getAllRepositories().values()
              .forEach(repo -> allRepos.add(new RepositoryDTO(repo.getId(),
                                                              repo.getName(),
                                                              repo.getHttpTransportUrl())
                       )
              );

        return allRepos;
    }

    @Override
    public RepositoryDTO getRepositoryById(final String tokenValue, final long id) throws IOException {
        GitHub github = getGitHub(tokenValue);
        GHRepository repository = github.getRepositoryById(id);
        return new RepositoryDTO(repository.getId(), repository.getName(), repository.getHttpTransportUrl());
    }

    private static GitHub getGitHub(final String accessToken) throws IOException {
        return GitHub.connectUsingOAuth(accessToken);
    }
}
