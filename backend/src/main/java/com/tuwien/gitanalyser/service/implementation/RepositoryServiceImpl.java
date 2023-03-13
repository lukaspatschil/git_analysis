package com.tuwien.gitanalyser.service.implementation;

import com.tuwien.gitanalyser.endpoints.dtos.assignment.CreateAssignmentDTO;
import com.tuwien.gitanalyser.entity.Assignment;
import com.tuwien.gitanalyser.entity.Repository;
import com.tuwien.gitanalyser.entity.RepositoryFactory;
import com.tuwien.gitanalyser.entity.SubAssignment;
import com.tuwien.gitanalyser.entity.SubAssignmentFactory;
import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.exception.ForbiddenException;
import com.tuwien.gitanalyser.repository.RepositoryRepository;
import com.tuwien.gitanalyser.service.AssignmentService;
import com.tuwien.gitanalyser.service.GitService;
import com.tuwien.gitanalyser.service.RepositoryService;
import com.tuwien.gitanalyser.service.SubAssignmentService;
import com.tuwien.gitanalyser.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RepositoryServiceImpl implements RepositoryService {

    public static final Logger LOGGER = LoggerFactory.getLogger(RepositoryServiceImpl.class);
    private final UserService userService;
    private final GitService gitService;
    private final RepositoryRepository repositoryRepository;
    private final AssignmentService assignmentService;
    private final SubAssignmentService subAssignmentService;
    private final SubAssignmentFactory subAssignmentFactory;
    private final RepositoryFactory repositoryFactory;

    public RepositoryServiceImpl(final UserService userService,
                                 final GitService gitService,
                                 final RepositoryRepository repositoryRepository,
                                 final AssignmentService assignmentService,
                                 final SubAssignmentService subAssignmentService,
                                 final SubAssignmentFactory subAssignmentFactory,
                                 final RepositoryFactory repositoryFactory) {
        this.userService = userService;
        this.gitService = gitService;
        this.repositoryRepository = repositoryRepository;
        this.assignmentService = assignmentService;
        this.subAssignmentService = subAssignmentService;
        this.subAssignmentFactory = subAssignmentFactory;
        this.repositoryFactory = repositoryFactory;
    }

    @Override
    public void assignCommitter(final long userId, final Long platformId, final CreateAssignmentDTO dto) {
        LOGGER.info("assignCommitter with userId {} and platformId {} and DTO {}", userId, platformId, dto);

        if (!gitService.repositoryAccessibleByUser(userId, platformId)) {
            throw new ForbiddenException("User is not allowed to access this repository");
        }

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

    private Optional<Repository> findRepositoryByPlatformIdAndUser(final Long platformId, final User user) {
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
