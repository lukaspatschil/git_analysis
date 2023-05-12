package com.tuwien.gitanalyser.service.implementation;

import com.tuwien.gitanalyser.endpoints.dtos.assignment.CreateAssignmentDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitAggregatedInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitterInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.StatsInternalDTO;
import com.tuwien.gitanalyser.entity.Assignment;
import com.tuwien.gitanalyser.entity.Repository;
import com.tuwien.gitanalyser.entity.RepositoryFactory;
import com.tuwien.gitanalyser.entity.SubAssignment;
import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.exception.GitException;
import com.tuwien.gitanalyser.exception.IllegalArgumentException;
import com.tuwien.gitanalyser.exception.NoProviderFoundException;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.repository.RepositoryRepository;
import com.tuwien.gitanalyser.service.AssignmentService;
import com.tuwien.gitanalyser.service.GitService;
import com.tuwien.gitanalyser.service.RepositoryService;
import com.tuwien.gitanalyser.service.SubAssignmentService;
import com.tuwien.gitanalyser.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RepositoryServiceImpl implements RepositoryService {

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

    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Override
    public void addAssignment(final long userId, final Long platformId, final CreateAssignmentDTO dto)
        throws IllegalArgumentException {

        User user = userService.getUser(userId);

        Optional<Repository> repositoryOptional = findRepositoryByPlatformIdAndUser(platformId, user);

        // Create Repository if it does NotExist
        Repository repositoryEntity = repositoryOptional.orElseGet(() -> createRepository(platformId, user));

        checkIAssignmentsDoesNotProduceCircularAssignments(dto, repositoryEntity);

        Assignment assignment = assignmentService.getOrCreateAssignment(repositoryEntity, dto.getKey());

        for (String assignedName : dto.getAssignedNames()) {
            boolean overwritten = false;
            for (Assignment tempAssignment : repositoryEntity.getAssignments()) {
                for (SubAssignment subAssignment : tempAssignment.getSubAssignments()) {
                    if (subAssignment.getAssignedName().equals(assignedName)) {
                        overwriteSubAssignment(dto.getKey(), assignedName, repositoryEntity, subAssignment);
                        overwritten = true;
                    }
                }
            }
            if (!overwritten) {
                subAssignmentService.addSubAssignment(assignment, assignedName);
            }
        }
    }

    @Override
    public List<Assignment> getAssignments(final long userId, final Long platformId) {
        User user = userService.getUser(userId);
        Optional<Repository> repository = findRepositoryByPlatformIdAndUser(platformId, user);

        if (repository.isEmpty()) {
            throw new NotFoundException("Repository not found");
        }

        return repository.get().getAssignments();
    }

    @Override
    public void deleteAssignment(final Long userId, final Long platformId, final Long subAssignmentId) {
        User user = userService.getUser(userId);
        Optional<Repository> repository = findRepositoryByPlatformIdAndUser(platformId, user);

        if (repository.isEmpty()) {
            throw new NotFoundException("Repository not found");
        }

        assignmentService.deleteSubAssignmentById(repository.get(), subAssignmentId);
    }

    @Override
    public void deleteAllNotAccessibleRepositoryEntities(final Long userId, final List<Long> gitRepositoryIds) {

        User user =
            userService.getUser(userId);
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

        List<StatsInternalDTO> stats = gitService.getStats(userId, platformId, branch);

        if (mappedByAssignments) {
            try {
                List<Assignment> assignments = getAssignments(userId, platformId);
                stats = mapStatsByAssignments(stats, assignments);
            } catch (NotFoundException ignored) {
            }
        }
        return stats;
    }

    @Override
    public List<CommitAggregatedInternalDTO> getCommits(final long userId, final Long platformId, final String branch,
                                                        final Boolean mappedByAssignments, final String name)
        throws GitException, NoProviderFoundException {

        List<CommitInternalDTO> internalCommits = gitService.getAllCommits(userId, platformId, branch);

        if (mappedByAssignments) {
            try {
                List<Assignment> assignments = getAssignments(userId, platformId);
                mapCommitsByAssignments(internalCommits, assignments);
            } catch (NotFoundException ignored) {
            }
        }

        if (name != null) {
            internalCommits = internalCommits.stream()
                                             .filter(commit -> commit.getAuthor().equals(name))
                                             .collect(Collectors.toList());
        }

        List<CommitAggregatedInternalDTO> resultCommit = new ArrayList<>();

        int overallLineOfCode = 0;
        for (CommitInternalDTO commit : internalCommits) {
            overallLineOfCode = overallLineOfCode + commit.getAdditions() - commit.getDeletions();
            var aggregatedCommit = new CommitAggregatedInternalDTO(commit.getId(), commit.getMessage(),
                                                                   commit.getAuthor(), commit.getTimestamp(),
                                                                   commit.getParentIds(), commit.isMergeCommit(),
                                                                   commit.getAdditions(), commit.getDeletions(),
                                                                   overallLineOfCode);
            resultCommit.add(aggregatedCommit);
        }

        return resultCommit;
    }

    @Override
    public Set<CommitterInternalDTO> getCommitters(final long userId, final Long platformId, final String branch,
                                                   final Boolean mappedByAssignments)
        throws GitException, NoProviderFoundException {

        Set<CommitterInternalDTO> result;

        List<CommitAggregatedInternalDTO> internalCommits = getCommits(userId, platformId, branch,
                                                                       mappedByAssignments, null);

        result = internalCommits.stream()
                                .map(commit -> new CommitterInternalDTO(commit.getAuthor()))
                                .collect(Collectors.toSet());

        return result;
    }

    private void overwriteSubAssignment(final String key,
                                        final String assignedName,
                                        final Repository repositoryEntity,
                                        final SubAssignment subAssignmentToRemoveSubAssignmentFrom) {
        assignmentService.deleteSubAssignmentById(repositoryEntity, subAssignmentToRemoveSubAssignmentFrom.getId());
        Assignment assignment = assignmentService.getOrCreateAssignment(repositoryEntity, key);
        subAssignmentService.addSubAssignment(assignment, assignedName);
    }

    private void checkIAssignmentsDoesNotProduceCircularAssignments(final CreateAssignmentDTO dto,
                                                                    final Repository repositoryEntity)
        throws IllegalArgumentException {

        for (String assignedName : dto.getAssignedNames()) {
            if (dto.getKey().equals(assignedName)) {
                throw new IllegalArgumentException("Assignment key and assigned name must not be equal");
            }

            for (Assignment assignment : repositoryEntity.getAssignments()) {
                if (assignment.getKey().equals(assignedName)) {
                    String message = "Assigned name must not be equal to an existing assignment key";
                    throw new IllegalArgumentException(message);
                }
                for (SubAssignment subAssignment : assignment.getSubAssignments()) {
                    if (subAssignment.getAssignedName().equals(dto.getKey())) {
                        String message = "Assignment key must not be equal to an existing sub assignment name";
                        throw new IllegalArgumentException(message);
                    }
                }
            }
        }

    }

    private void mapCommitsByAssignments(final Collection<CommitInternalDTO> commits,
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
        return repositoryRepository.findByUserAndPlatformId(user, platformId);
    }

    private Repository createRepository(final Long platformId, final User user) {
        Repository repositoryEntity;
        repositoryEntity = repositoryFactory.create();
        repositoryEntity.setUser(user);
        repositoryEntity.setPlatformId(platformId);
        return repositoryRepository.save(repositoryEntity);
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
