package com.tuwien.gitanalyser.service.implementation;

import com.tuwien.gitanalyser.endpoints.dtos.assignment.CreateAssignmentDTO;
import com.tuwien.gitanalyser.entity.Assignment;
import com.tuwien.gitanalyser.entity.Repository;
import com.tuwien.gitanalyser.entity.RepositoryFactory;
import com.tuwien.gitanalyser.entity.SubAssignment;
import com.tuwien.gitanalyser.entity.SubAssignmentFactory;
import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.repository.RepositoryRepository;
import com.tuwien.gitanalyser.service.AssignmentService;
import com.tuwien.gitanalyser.service.RepositoryService;
import com.tuwien.gitanalyser.service.SubAssignmentService;
import com.tuwien.gitanalyser.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RepositoryServiceImpl implements RepositoryService {

    public static final Logger LOGGER = LoggerFactory.getLogger(RepositoryServiceImpl.class);
    private final UserService userService;
    private final RepositoryRepository repositoryRepository;
    private final AssignmentService assignmentService;
    private final SubAssignmentService subAssignmentService;
    private final SubAssignmentFactory subAssignmentFactory;
    private final RepositoryFactory repositoryFactory;

    public RepositoryServiceImpl(final UserService userService,
                                 final RepositoryRepository repositoryRepository,
                                 final AssignmentService assignmentService,
                                 final SubAssignmentService subAssignmentService,
                                 final SubAssignmentFactory subAssignmentFactory,
                                 final RepositoryFactory repositoryFactory) {
        this.userService = userService;
        this.repositoryRepository = repositoryRepository;
        this.assignmentService = assignmentService;
        this.subAssignmentService = subAssignmentService;
        this.subAssignmentFactory = subAssignmentFactory;
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

        SubAssignment subAssignment = subAssignmentFactory.create();
        subAssignment.setAssignedName(dto.getAssignedName());
        subAssignmentService.addSubAssignment(assignment, subAssignment);
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
    public void deleteAllNotAccessibleRepositoryEntities(Long userId, List<Long> gitRepositoryIds) {
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
}
