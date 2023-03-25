package com.tuwien.gitanalyser.service.implementation;

import com.tuwien.gitanalyser.endpoints.dtos.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitterInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.StatsInternalDTO;
import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.exception.GitException;
import com.tuwien.gitanalyser.exception.NoProviderFoundException;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.security.AuthenticationConstants;
import com.tuwien.gitanalyser.service.GitAPI;
import com.tuwien.gitanalyser.service.GitService;
import com.tuwien.gitanalyser.service.RepositoryService;
import com.tuwien.gitanalyser.service.UserService;
import com.tuwien.gitanalyser.service.apiCalls.GitHubAPI;
import com.tuwien.gitanalyser.service.apiCalls.GitLabAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class GitServiceImpl implements GitService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitServiceImpl.class);
    private final UserService userService;

    private final RepositoryService repositoryService;
    private final GitHubAPI gitHubAPI;
    private final GitLabAPI gitLabAPI;

    public GitServiceImpl(final UserService userService,
                          final RepositoryService repositoryService,
                          final GitHubAPI gitHubAPI,
                          final GitLabAPI gitLabAPI) {
        this.userService = userService;
        this.repositoryService = repositoryService;
        this.gitHubAPI = gitHubAPI;
        this.gitLabAPI = gitLabAPI;
    }

    @Override
    public List<NotSavedRepositoryInternalDTO> getAllRepositories(final Long userId)
        throws NoProviderFoundException, GitException {
        LOGGER.info("RepositoryServiceImpl: getAllRepositories for user " + userId);

        List<NotSavedRepositoryInternalDTO> allRepos;

        GitAPI gitAPI = getAPI(userId);
        allRepos = gitAPI.getAllRepositories(getAccessToken(userId));

        repositoryService.deleteAllNotAccessibleRepositoryEntities(
            userId,
            allRepos.stream()
                    .map(NotSavedRepositoryInternalDTO::getPlatformId)
                    .toList());

        return allRepos;
    }

    @Override
    public List<BranchInternalDTO> getAllBranches(final Long userId, final Long platformId)
        throws GitException, NoProviderFoundException {
        LOGGER.info("getAllBranches for user {} and repository {}", userId, platformId);

        GitAPI gitAPI = getAPI(userId);
        List<BranchInternalDTO> allBranches = gitAPI.getAllBranches(getAccessToken(userId), platformId);

        LOGGER.info("getAllBranches for user {} and repository {} finished", userId, platformId);
        return allBranches;
    }

    @Override
    public NotSavedRepositoryInternalDTO getRepositoryById(final Long userId, final Long platformId)
        throws GitException, NoProviderFoundException {
        LOGGER.info("getRepositoryById with Id {} for user {}", platformId, userId);

        GitAPI gitAPI = getAPI(userId);
        NotSavedRepositoryInternalDTO nSRIDTO = gitAPI.getRepositoryById(getAccessToken(userId), platformId);

        LOGGER.info("getRepositoryById with Id {} for user {} finished", platformId, userId);
        return nSRIDTO;
    }

    @Override
    public List<CommitInternalDTO> getAllCommits(final long userId, final Long platformId, final String branch)
        throws GitException, NoProviderFoundException {
        LOGGER.info("getAllCommits for user {} and repository {} and branch {}", userId, platformId, branch);

        GitAPI gitApi = getAPI(userId);
        List<CommitInternalDTO> allCommits = gitApi.getAllCommits(getAccessToken(userId), platformId, branch);

        LOGGER.info("getAllCommits for user {} and repository {} and branch {} finished with length {}", userId,
                    platformId, branch, allCommits.size());
        return allCommits;
    }

    @Override
    public Set<CommitterInternalDTO> getAllCommitters(final long userId, final Long platformId, final String branch)
        throws GitException, NoProviderFoundException {
        LOGGER.info("getAllCommitters for user {} and repository {} and branch {}", userId, platformId, branch);

        Set<CommitterInternalDTO> result = new HashSet<>();

        GitAPI gitApi = getAPI(userId);
        List<CommitInternalDTO> allCommits = gitApi.getAllCommits(getAccessToken(userId), platformId, branch);

        for (CommitInternalDTO commit : allCommits) {
            result.add(new CommitterInternalDTO(commit.getAuthor()));
        }

        LOGGER.info("getAllCommitters for user {} and repository {} and branch {} finished with length {}", userId,
                    platformId, branch, result.size());

        return result;

    }

    @Override
    public boolean repositoryAccessibleByUser(final long userId, final Long platformId)
        throws NoProviderFoundException {
        LOGGER.info("repositoryAccessibleByUser for user {} and repository {}", userId, platformId);

        boolean result;

        GitAPI gitApi = getAPI(userId);
        try {
            gitApi.getRepositoryById(getAccessToken(userId), platformId);
            result = true;
        } catch (Exception e) {
            LOGGER.error("repositoryAccessibleByUser for user {} and repository {} failed: {}", userId, platformId, e);
            result = false;
        }
        LOGGER.info("repositoryAccessibleByUser for user {} and repository {} finished positive", userId, platformId);
        return result;
    }

    @Override
    public List<StatsInternalDTO> getStats(final long userId, final Long platformId, final String branch)
        throws NoProviderFoundException, GitException {

        HashMap<String, List<CommitInternalDTO>> groupedByAuthor = new HashMap<>();
        List<StatsInternalDTO> stats = new ArrayList<>();

        List<CommitInternalDTO> allCommits = getAllCommits(userId, platformId, branch);

        for (CommitInternalDTO commit : allCommits) {
            /*if (groupedByAuthor.containsKey(commit.getAuthor())) {
                groupedByAuthor.get(commit.getAuthor()).add(commit);
            } else {
                groupedByAuthor.put(commit.getAuthor(), List.of(commit));
            }*/
            groupedByAuthor.computeIfAbsent(commit.getAuthor(), k -> new ArrayList<>()).add(commit);
        }

        for (String author : groupedByAuthor.keySet()) {
            List<CommitInternalDTO> allCommitsForAuthor = groupedByAuthor.get(author);
            AtomicInteger numberOfCommits = new AtomicInteger();
            AtomicInteger numberOfAdditions = new AtomicInteger();
            AtomicInteger numberOfDeletions = new AtomicInteger();
            /*for (CommitInternalDTO commit : allCommitsForAuthor) {
                numberOfCommits.getAndIncrement();
                numberOfAdditions.addAndGet(commit.getAdditions());
                numberOfDeletions.addAndGet(commit.getDeletions());
            }*/
            allCommitsForAuthor.forEach(commit -> {
                numberOfCommits.getAndIncrement();
                numberOfAdditions.addAndGet(commit.getAdditions());
                numberOfDeletions.addAndGet(commit.getDeletions());
            });
            stats.add(new StatsInternalDTO(author, numberOfCommits.get(), numberOfAdditions.get(),
                                           numberOfDeletions.get()));
        }

        return stats;
    }

    private GitAPI getAPI(final Long userId) throws NoProviderFoundException {
        return switch (getUser(userId).getAuthenticationProvider().name().toLowerCase()) {
            case AuthenticationConstants.GITHUB_REGISTRATION_ID -> gitHubAPI;
            case AuthenticationConstants.GITLAB_REGISTRATION_ID -> gitLabAPI;
            default -> throw new NoProviderFoundException(getUser(userId).getAuthenticationProvider().name());
        };
    }

    private String getAccessToken(final Long userId) throws NotFoundException {
        return userService.getUser(userId).getAccessToken();
    }

    private User getUser(final Long userId) throws NotFoundException {
        return userService.getUser(userId);
    }
}
