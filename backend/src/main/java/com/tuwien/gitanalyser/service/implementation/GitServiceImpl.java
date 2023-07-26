package com.tuwien.gitanalyser.service.implementation;

import com.tuwien.gitanalyser.endpoints.dtos.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.StatsInternalDTO;
import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.exception.GitException;
import com.tuwien.gitanalyser.exception.NoProviderFoundException;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.security.AuthenticationConstants;
import com.tuwien.gitanalyser.service.GitExceptionHandlerService;
import com.tuwien.gitanalyser.service.GitService;
import com.tuwien.gitanalyser.service.RepositoryService;
import com.tuwien.gitanalyser.service.UserService;
import com.tuwien.gitanalyser.service.apiCalls.github.GitHubExceptionHandlerServiceImpl;
import com.tuwien.gitanalyser.service.apiCalls.gitlab.GitLabExceptionHandlerServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class GitServiceImpl implements GitService {

    private final UserService userService;

    private final RepositoryService repositoryService;
    private final GitHubExceptionHandlerServiceImpl gitHubAccessTokenRefresherService;
    private final GitLabExceptionHandlerServiceImpl gitLabAccessTokenRefresherService;

    public GitServiceImpl(final UserService userService,
                          @Lazy final RepositoryService repositoryService,
                          final GitHubExceptionHandlerServiceImpl gitHubAccessTokenRefresherService,
                          final GitLabExceptionHandlerServiceImpl gitLabAccessTokenRefresherService) {
        this.userService = userService;
        this.repositoryService = repositoryService;
        this.gitHubAccessTokenRefresherService = gitHubAccessTokenRefresherService;
        this.gitLabAccessTokenRefresherService = gitLabAccessTokenRefresherService;
    }

    @Override
    public List<NotSavedRepositoryInternalDTO> getAllRepositories(final Long userId)
        throws NoProviderFoundException, GitException {

        List<NotSavedRepositoryInternalDTO> allRepos;

        GitExceptionHandlerService gitAPI = getAPI(userId);
        allRepos = gitAPI.getAllRepositories(userId);

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

        List<BranchInternalDTO> allBranches;
        GitExceptionHandlerService gitAPI = getAPI(userId);
        allBranches = gitAPI.getAllBranches(userId, platformId);

        return allBranches;
    }

    @Override
    public NotSavedRepositoryInternalDTO getRepositoryById(final Long userId, final Long platformId)
        throws GitException, NoProviderFoundException {

        GitExceptionHandlerService gitAPI = getAPI(userId);

        return gitAPI.getRepositoryById(userId, platformId);
    }

    @Override
    public List<CommitInternalDTO> getAllCommits(final long userId, final Long platformId, final String branch)
        throws GitException, NoProviderFoundException {

        GitExceptionHandlerService gitApi = getAPI(userId);
        List<CommitInternalDTO> allCommits;
        allCommits = gitApi.getAllCommits(userId, platformId, branch);
        return allCommits;
    }

    @Override
    public boolean repositoryAccessibleByUser(final long userId, final Long platformId)
        throws NoProviderFoundException {

        boolean result;

        GitExceptionHandlerService gitApi = getAPI(userId);
        try {
            gitApi.getRepositoryById(userId, platformId);
            result = true;
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    @Override
    public List<StatsInternalDTO> getStats(final long userId, final Long platformId, final String branch)
        throws NoProviderFoundException, GitException {

        HashMap<String, List<CommitInternalDTO>> groupedByAuthor = new HashMap<>();
        List<StatsInternalDTO> stats = new ArrayList<>();

        List<CommitInternalDTO> allCommits = getAllCommits(userId, platformId, branch);

        for (CommitInternalDTO commit : allCommits) {
            groupedByAuthor.computeIfAbsent(commit.getAuthor(), k -> new ArrayList<>()).add(commit);
        }

        for (String author : groupedByAuthor.keySet()) {
            List<CommitInternalDTO> allCommitsForAuthor = groupedByAuthor.get(author);
            AtomicInteger numberOfCommits = new AtomicInteger();
            AtomicInteger numberOfAdditions = new AtomicInteger();
            AtomicInteger numberOfDeletions = new AtomicInteger();
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

    @Override
    public String getEmail(final long userId) throws NoProviderFoundException, GitException {
        GitExceptionHandlerService gitApi = getAPI(userId);
        return gitApi.getEmail(userId);
    }

    private GitExceptionHandlerService getAPI(final Long userId) throws NoProviderFoundException {
        return switch (getUser(userId).getAuthenticationProvider().name().toLowerCase()) {
            case AuthenticationConstants.GITHUB_REGISTRATION_ID -> gitHubAccessTokenRefresherService;
            case AuthenticationConstants.GITLAB_REGISTRATION_ID -> gitLabAccessTokenRefresherService;
            default -> throw new NoProviderFoundException(getUser(userId).getAuthenticationProvider().name());
        };
    }

    private User getUser(final Long userId) throws NotFoundException {
        return userService.getUser(userId);
    }
}
