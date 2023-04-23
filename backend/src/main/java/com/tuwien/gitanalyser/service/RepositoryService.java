package com.tuwien.gitanalyser.service;

import com.tuwien.gitanalyser.endpoints.dtos.assignment.CreateAssignmentDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitAggregatedInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.StatsInternalDTO;
import com.tuwien.gitanalyser.entity.Assignment;
import com.tuwien.gitanalyser.exception.GitException;
import com.tuwien.gitanalyser.exception.IllegalArgumentException;
import com.tuwien.gitanalyser.exception.NoProviderFoundException;

import java.util.List;

public interface RepositoryService {

    void addAssignment(long userId, Long platformId, CreateAssignmentDTO dto) throws IllegalArgumentException;

    List<Assignment> getAssignments(long userId, Long platformId);

    void deleteAssignment(Long userId, Long platformId, Long subAssignmentId);

    void deleteAllNotAccessibleRepositoryEntities(Long userId, List<Long> gitRepositoryIds);

    List<StatsInternalDTO> getStats(long userId, Long platformId, String branch, boolean mappedByAssignments)
        throws GitException, NoProviderFoundException;

    List<CommitAggregatedInternalDTO> getCommits(long userId, Long platformId, String branch,
                                                 Boolean mappedByAssignments)
        throws GitException, NoProviderFoundException;
}
