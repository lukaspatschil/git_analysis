package com.tuwien.gitanalyser.service.implementation;

import com.tuwien.gitanalyser.endpoints.DTOs.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.DTOs.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.entity.SavedRepository;
import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.repository.RepositoryRepository;
import com.tuwien.gitanalyser.security.AuthenticationConstants;
import com.tuwien.gitanalyser.service.APICalls.GitAPI;
import com.tuwien.gitanalyser.service.APICalls.GitHubAPI;
import com.tuwien.gitanalyser.service.APICalls.GitLabAPI;
import com.tuwien.gitanalyser.service.JGit;
import com.tuwien.gitanalyser.service.RepositoryService;
import com.tuwien.gitanalyser.service.UserService;
import org.gitlab4j.api.GitLabApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class RepositoryServiceImpl implements RepositoryService {

    public static final Logger LOGGER = LoggerFactory.getLogger(RepositoryServiceImpl.class);

    private final RepositoryRepository repositoryRepository;
    private final UserService userService;
    private final GitHubAPI gitHubAPI;
    private final GitLabAPI gitLabAPI;
    private final JGit jGit;

    public RepositoryServiceImpl(final UserService userService,
                                 final GitHubAPI gitHubAPI,
                                 final GitLabAPI gitLabAPI,
                                 final RepositoryRepository repositoryRepository,
                                 final JGit jGit) {
        this.userService = userService;
        this.repositoryRepository = repositoryRepository;
        this.gitHubAPI = gitHubAPI;
        this.gitLabAPI = gitLabAPI;
        this.jGit = jGit;
    }

    @Override
    public List<NotSavedRepositoryInternalDTO> getAllRepositories(final Long userId) {
        LOGGER.info("RepositoryServiceImpl: getAllRepositories for user " + userId);

        List<NotSavedRepositoryInternalDTO> allRepos;

        try {
            GitAPI gitAPI = getAPI(userId);
            allRepos = gitAPI.getAllRepositories(getAccessToken(userId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return allRepos;
    }

    @Override
    public SavedRepository getRepositoryById(final Long userId, final Long id) {
        LOGGER.info("RepositoryServiceImpl: getRepositoryById with Id {} for user {}", id, userId);

        User user = userService.getUser(userId);
        SavedRepository savedRepo = new SavedRepository();

        try {
            GitAPI gitAPI = getAPI(userId);
            NotSavedRepositoryInternalDTO nSRIDTO = gitAPI.getRepositoryById(getAccessToken(userId), id);

            SavedRepository savedRepository =
                repositoryRepository.findByUserIdAndPlatformId(user.getId(), nSRIDTO.getPlatformId());

            // save on first access
            if (savedRepository == null) {
                savedRepo.setPlatformId(nSRIDTO.getPlatformId());
                savedRepo.setName(nSRIDTO.getName());
                savedRepo.setUrl(nSRIDTO.getUrl());
                savedRepo.setUser(user);
                savedRepo = repositoryRepository.save(savedRepo);
                jGit.cloneRepository(savedRepo.getUrl(), savedRepo.getId(), getAccessToken(userId));
            } else {
                savedRepo = savedRepository;
            }
        } catch (GitLabApiException | IOException e) {
            throw new RuntimeException(e);
        }
        return savedRepo;
    }

    public List<BranchInternalDTO> getAllBranches(final Long userId, final Long id) {
        LOGGER.info("RepositoryServiceImpl: getAllBranches for user {} and repository {}", userId, id);

        List<BranchInternalDTO> allBranches;

        try {
            GitAPI gitAPI = getAPI(userId);
            allBranches = gitAPI.getAllBranches(getAccessToken(userId), id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return allBranches;
    }

    private GitAPI getAPI(final Long userId) throws NotFoundException {
        return switch (getUser(userId).getAuthenticationProvider().name().toLowerCase()) {
            case AuthenticationConstants.GITHUB_REGISTRATION_ID -> gitHubAPI;
            case AuthenticationConstants.GITLAB_REGISTRATION_ID -> gitLabAPI;
            default -> throw new RuntimeException("No API for this client");
        };
    }

    private String getAccessToken(final Long userId) throws NotFoundException {
        return userService.getUser(userId).getAccessToken();
    }

    private User getUser(final Long userId) throws NotFoundException {
        return userService.getUser(userId);
    }
}
