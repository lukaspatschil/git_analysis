package com.tuwien.gitanalyser.service;

import com.tuwien.gitanalyser.endpoints.dtos.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.StatsInternalDTO;
import com.tuwien.gitanalyser.exception.GitException;
import com.tuwien.gitanalyser.exception.NoProviderFoundException;

import java.util.List;

public interface GitService {

    List<NotSavedRepositoryInternalDTO> getAllRepositories(Long userId) throws NoProviderFoundException, GitException;

    List<BranchInternalDTO> getAllBranches(Long userId, Long platformId)
        throws GitException, NoProviderFoundException;

    NotSavedRepositoryInternalDTO getRepositoryById(Long userId, Long platformId)
        throws GitException, NoProviderFoundException;

    List<CommitInternalDTO> getAllCommits(long userId, Long platformId, String branch)
        throws GitException, NoProviderFoundException;

    boolean repositoryAccessibleByUser(long userId, Long platformId) throws NoProviderFoundException;

    List<StatsInternalDTO> getStats(long userId, Long platformId, String branch)
        throws NoProviderFoundException, GitException;

    String getEmail(long userId) throws NoProviderFoundException, GitException;
}
