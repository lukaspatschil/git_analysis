package com.tuwien.gitanalyser.service.implementation;

import com.tuwien.gitanalyser.entity.Assignment;
import com.tuwien.gitanalyser.entity.Repository;
import com.tuwien.gitanalyser.entity.utils.AssignmentFactory;
import com.tuwien.gitanalyser.repository.AssignmentRepository;
import com.tuwien.gitanalyser.service.AssignmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssignmentService.class);

    private final AssignmentRepository assignmentRepository;
    private final AssignmentFactory assignmentFactory;

    public AssignmentServiceImpl(final AssignmentRepository assignmentRepository,
                                 final AssignmentFactory assignmentFactory) {
        this.assignmentRepository = assignmentRepository;
        this.assignmentFactory = assignmentFactory;
    }

    @Override
    public Assignment getOrCreateAssignment(final Repository repository, final String key) {
        LOGGER.info("addAssignment for repository {} with the key {}", repository.getId(), key);
        Optional<Assignment> assignmentOptional = assignmentRepository.findByRepositoryAndKey(repository, key);

        return assignmentOptional.orElseGet(() -> createNewAssignment(repository, key));
    }

    private Assignment createNewAssignment(final Repository repository, final String key) {
        Assignment result;
        Assignment assignment = assignmentFactory.create();
        assignment.setRepository(repository);
        assignment.setKey(key);
        result = assignmentRepository.save(assignment);
        return result;
    }
}
