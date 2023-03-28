package com.tuwien.gitanalyser.service.implementation;

import com.tuwien.gitanalyser.endpoints.dtos.assignment.CreateAssignmentDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.StatsInternalDTO;
import com.tuwien.gitanalyser.entity.Assignment;
import com.tuwien.gitanalyser.entity.Repository;
import com.tuwien.gitanalyser.entity.RepositoryFactory;
import com.tuwien.gitanalyser.entity.SubAssignment;
import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.exception.GitException;
import com.tuwien.gitanalyser.exception.NoProviderFoundException;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.repository.RepositoryRepository;
import com.tuwien.gitanalyser.service.AssignmentService;
import com.tuwien.gitanalyser.service.GitService;
import com.tuwien.gitanalyser.service.RepositoryService;
import com.tuwien.gitanalyser.service.SubAssignmentService;
import com.tuwien.gitanalyser.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RepositoryServiceImpl implements RepositoryService {

    public static final Logger LOGGER = LoggerFactory.getLogger(RepositoryServiceImpl.class);
    private final UserService userService;
    private final RepositoryRepository repositoryRepository;
    private final AssignmentService assignmentService;
    private final SubAssignmentService subAssignmentService;
    private final GitService gitService;
    private final RepositoryFactory repositoryFactory;

    public RepositoryServiceImpl(final UserService userService,
                                 final RepositoryRepository repositoryRepository,
                                 final AssignmentService assignmentService,
                                 final SubAssignmentService subAssignmentService,
                                 final GitService gitService,
                                 final RepositoryFactory repositoryFactory) {
        this.userService = userService;
        this.repositoryRepository = repositoryRepository;
        this.assignmentService = assignmentService;
        this.subAssignmentService = subAssignmentService;
        this.gitService = gitService;
        this.repositoryFactory = repositoryFactory;
    }

    @Override
    public void assignCommitter(final long userId, final Long platformId, final CreateAssignmentDTO dto) {
        LOGGER.info("assignCommitter with userId {} and platformId {} and DTO {}", userId, platformId, dto);

        User user = userService.getUser(userId);

        Repository repositoryEntity;
        Optional<Repository> repositoryOptional = findRepositoryByPlatformIdAndUser(platformId, user);

        // Create Repository if it does NotExist
        repositoryEntity = repositoryOptional.orElseGet(() -> createRepository(platformId, user));

        Assignment assignment = assignmentService.getOrCreateAssignment(repositoryEntity, dto.getKey());

        subAssignmentService.addSubAssignment(assignment, dto.getAssignedName());
    }

    @Override
    public List<Assignment> getAssignments(final long userId, final Long platformId) {
        LOGGER.info("getAssignments with userId {} and platformId {}", userId, platformId);
        User user = userService.getUser(userId);
        Optional<Repository> repository = findRepositoryByPlatformIdAndUser(platformId, user);

        if (repository.isEmpty()) {
            LOGGER.info("getAssignments finished with empty repository for platformId {}", platformId);
            throw new NotFoundException("Repository not found");
        }

        LOGGER.info("getAssignments finished for platformId {}", platformId);

        return repository.get().getAssignments();
    }

    @Override
    public void deleteAssignment(final Long userId, final Long platformId, final Long subAssignmentId) {
        LOGGER.info("deleteAssignment with userId {} and platformId {} and subAssignmentId {}",
                    userId, platformId, subAssignmentId);
        User user = userService.getUser(userId);
        Optional<Repository> repository = findRepositoryByPlatformIdAndUser(platformId, user);

        if (repository.isEmpty()) {
            LOGGER.info("getAssignments finished with empty repository for platformId {}", platformId);
            throw new NotFoundException("Repository not found");
        }

        assignmentService.deleteSubAssignmentById(repository.get(), subAssignmentId);
    }

    @Override
    public void deleteAllNotAccessibleRepositoryEntities(final Long userId, final List<Long> gitRepositoryIds) {
        LOGGER.info("deleteAllNotAccessibleRepositoryEntities with userId {} and gitRepositoryIds {}",
                    userId, gitRepositoryIds);

        User user = userService.getUser(userId);
        List<Repository> repositories = repositoryRepository.findByUser(user);

        for (Repository repository : repositories) {
            if (!gitRepositoryIds.contains(repository.getPlatformId())) {
                repositoryRepository.delete(repository);
            }
        }
    }

    @Override
    public List<StatsInternalDTO> getStats(final long userId, final Long platformId, final String branch,
                                           final boolean mappedByAssignments)
        throws GitException, NoProviderFoundException {
        LOGGER.info("getStats with userId {} and platformId {} and branch {} and mappedByAssignments {}",
                    userId, platformId, branch, mappedByAssignments);

        List<StatsInternalDTO> stats = gitService.getStats(userId, platformId, branch);

        if (mappedByAssignments) {
            try {
                List<Assignment> assignments = getAssignments(userId, platformId);
                stats = mapStatsByAssignments(stats, assignments);
            } catch (NotFoundException e) {
                LOGGER.info("getStats finished with empty repository for platformId {}", platformId);
            }
        }
        return stats;
    }

    @Override
    public List<CommitInternalDTO> getCommits(final long userId, final Long platformId, final String branch,
                                              final Boolean mappedByAssignments)
        throws GitException, NoProviderFoundException {
        LOGGER.info("getAllCommits with userId {} and platformId {} and branch {} and mappedByAssignments {}",
                    userId, platformId, branch, mappedByAssignments);

        List<CommitInternalDTO> commits = gitService.getAllCommits(userId, platformId, branch);

        if (mappedByAssignments) {
            try {
                List<Assignment> assignments = getAssignments(userId, platformId);
                commits = mapCommitsByAssignments(commits, assignments);
            } catch (NotFoundException e) {
                LOGGER.info("getAllCommits finished with empty repository for platformId {}", platformId);
            }
        }

        return commits;
    }

    private List<CommitInternalDTO> mapCommitsByAssignments(final List<CommitInternalDTO> commits,
                                                            final List<Assignment> assignments) {
        for (CommitInternalDTO commit : commits) {
            for (Assignment assignment : assignments) {

                String key = assignment.getKey();

                for (SubAssignment subAssignment : assignment.getSubAssignments()) {
                    if (commit.getAuthor().equals(subAssignment.getAssignedName())) {
                        commit.setAuthor(key);
                    }
                }
            }
        }
        return commits;
    }

    private List<StatsInternalDTO> mapStatsByAssignments(final List<StatsInternalDTO> stats,
                                                         final List<Assignment> assignments) {

        ConcurrentMap<String, StatsInternalDTO> statsMap = stats.stream().collect(
            Collectors.toConcurrentMap(StatsInternalDTO::getCommitter, Function.identity()));

        for (String committer : statsMap.keySet()) {
            for (Assignment assignment : assignments) {

                String key = assignment.getKey();

                for (SubAssignment subAssignment : assignment.getSubAssignments()) {
                    if (committer.equals(subAssignment.getAssignedName())) {
                        mapFromAssignmentToKey(statsMap, key, subAssignment.getAssignedName());
                    }
                }
            }
        }

        return new ArrayList<>(statsMap.values());
    }

    private Optional<Repository> findRepositoryByPlatformIdAndUser(final Long platformId, final User user) {
        LOGGER.info("findRepositoryByPlatformIdAndUser with platformId {} and user {}", platformId, user);
        return repositoryRepository.findByUserAndPlatformId(user, platformId);
    }

    private Repository createRepository(final Long platformId, final User user) {
        Repository repositoryEntity;
        repositoryEntity = repositoryFactory.create();
        repositoryEntity.setUser(user);
        repositoryEntity.setPlatformId(platformId);
        repositoryRepository.save(repositoryEntity);
        return repositoryEntity;
    }

    private void mapFromAssignmentToKey(final Map<String, StatsInternalDTO> statsMap, final String key,
                                        final String assignedName) {
        StatsInternalDTO oldStatsObject = statsMap.get(assignedName);
        StatsInternalDTO newStatsObject;
        if (statsMap.containsKey(key)) {
            newStatsObject = statsMap.get(key);
        } else {
            newStatsObject = new StatsInternalDTO();
            newStatsObject.setCommitter(key);
        }

        newStatsObject.setNumberOfCommits(
            newStatsObject.getNumberOfCommits() + oldStatsObject.getNumberOfCommits());
        newStatsObject.setNumberOfDeletions(
            newStatsObject.getNumberOfDeletions() + oldStatsObject.getNumberOfDeletions());
        newStatsObject.setNumberOfAdditions(
            newStatsObject.getNumberOfAdditions() + oldStatsObject.getNumberOfAdditions());

        statsMap.put(key, newStatsObject);
        statsMap.remove(assignedName);
    }
}
