package com.tuwien.gitanalyser.service.APICalls;

import com.tuwien.gitanalyser.endpoints.DTOs.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.DTOs.internal.NotSavedRepositoryInternalDTO;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GitHubAPI implements GitAPI {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubAPI.class);

    private final GitAPIFactory<GitHub> gitHubAPIFactory;

    public GitHubAPI(final GitAPIFactory<GitHub> gitHubAPIFactory) {
        this.gitHubAPIFactory = gitHubAPIFactory;
    }

    public List<NotSavedRepositoryInternalDTO> getAllRepositories(final String accessToken) throws IOException {
        LOGGER.info("GitHubAPI: getAllRepositories");
        GitHub github = gitHubAPIFactory.createObject(accessToken);
        List<NotSavedRepositoryInternalDTO> repositories = github.getMyself().getAllRepositories().values().stream()
                                                                 .map(repo -> new NotSavedRepositoryInternalDTO(
                                                                     repo.getId(),
                                                                     repo.getName(),
                                                                     repo.getHttpTransportUrl()))
                                                                 .collect(Collectors.toList());
        LOGGER.info("GitHubAPI: getAllRepositories: " + repositories.size());
        return repositories;
    }

    @Override
    public NotSavedRepositoryInternalDTO getRepositoryById(final String accessToken,
                                                           final long platformId) throws IOException {
        LOGGER.info("GitHubAPI: getRepositoryById: " + platformId);
        GitHub github = gitHubAPIFactory.createObject(accessToken);
        GHRepository repository = github.getRepositoryById(platformId);
        LOGGER.info("GitHubAPI: getRepositoryById: " + repository.getName());
        return NotSavedRepositoryInternalDTO.builder()
                                            .name(repository.getName())
                                            .url(repository.getHttpTransportUrl())
                                            .platformId(repository.getId())
                                            .build();
    }

    @Override
    public List<BranchInternalDTO> getAllBranches(final String accessToken, final Long platformId) throws IOException {
        LOGGER.info("GitHubAPI: getAllBranches: " + platformId);

        GitHub github = gitHubAPIFactory.createObject(accessToken);
        List<BranchInternalDTO> branches = github.getRepositoryById(platformId)
              .getBranches()
              .values()
              .stream()
              .map(branch -> new BranchInternalDTO(branch.getName()))
              .collect(Collectors.toList());

        LOGGER.info("GitHubAPI: getAllBranches for repo {} finished", platformId);
        return branches;
    }
}
