package com.tuwien.gitanalyser.service;

import com.tuwien.gitanalyser.endpoints.dtos.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.exception.GitException;

import java.util.List;

/**
 * refreshes the access token if necessary.
 */
public interface GitExceptionHandlerService {

    /**
     * calls the repository accessTokenService and refreshes the access token if needed.
     *
     * @param userId of the current user
     * @return List of Repositories
     * @throws GitException if something went wrong
     */
    List<NotSavedRepositoryInternalDTO> getAllRepositories(Long userId) throws GitException;

    /**
     * calls the repository accessTokenService and refreshes the access token if needed.
     *
     * @param userId     of the current user
     * @param platformId of the repository
     * @return List of branch
     * @throws GitException if something went wrong
     */
    List<BranchInternalDTO> getAllBranches(Long userId, Long platformId)
        throws GitException;

    /**
     * calls the repository accessTokenService and refreshes the access token if needed.
     *
     * @param userId     of the current user
     * @param platformId of the repository
     * @return single repository
     * @throws GitException if something went wrong
     */
    NotSavedRepositoryInternalDTO getRepositoryById(Long userId, Long platformId)
        throws GitException;

    /**
     * calls the repository accessTokenService and refreshes the access token if needed.
     *
     * @param userId     of the current user
     * @param platformId of the repository
     * @param branch from which to get the commits
     * @return List of commits
     * @throws GitException if something went wrong
     */
    List<CommitInternalDTO> getAllCommits(long userId, Long platformId, String branch)
        throws GitException;

    /**
     * calls the repository accessTokenService and refreshes the access token if needed.
     *
     * @param userId     of the current user
     * @param platformId of the repository
     * @return Boolean
     */
    boolean repositoryAccessibleByUser(long userId, Long platformId);
}
