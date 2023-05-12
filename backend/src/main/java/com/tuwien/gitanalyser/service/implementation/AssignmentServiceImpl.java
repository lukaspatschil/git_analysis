package com.tuwien.gitanalyser.service.implementation;

import com.tuwien.gitanalyser.entity.Assignment;
import com.tuwien.gitanalyser.entity.Repository;
import com.tuwien.gitanalyser.entity.utils.AssignmentFactory;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.repository.AssignmentRepository;
import com.tuwien.gitanalyser.service.AssignmentService;
import com.tuwien.gitanalyser.service.SubAssignmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    private final SubAssignmentService subAssignmentService;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentFactory assignmentFactory;

    public AssignmentServiceImpl(final SubAssignmentService subAssignmentService,
                                 final AssignmentRepository assignmentRepository,
                                 final AssignmentFactory assignmentFactory) {
        this.subAssignmentService = subAssignmentService;
        this.assignmentRepository = assignmentRepository;
        this.assignmentFactory = assignmentFactory;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Override
    public Assignment getOrCreateAssignment(final Repository repository, final String key) {
        Optional<Assignment> assignmentOptional = assignmentRepository.findByRepositoryAndKey(repository, key);

        return assignmentOptional.orElseGet(() -> createNewAssignment(repository, key));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Override
    public void deleteSubAssignmentById(final Repository repository, final Long subAssignmentId) {

        Optional<Assignment> assignmentOptional = findByRepositoryAndSubAssignment(repository, subAssignmentId);

        if (assignmentOptional.isEmpty()) {
            throw new NotFoundException("Assignment not found");
        }

        Assignment assignment = assignmentOptional.get();
        if (assignment.getSubAssignments().size() == 1) {
            // delete whole assignment
            deleteAssignment(assignment);
        } else if (assignment.getSubAssignments().size() > 1) {
            // delete subAssignment only
            subAssignmentService.deleteSubAssignmentById(subAssignmentId);
        } else {
            throw new NotFoundException("SubAssignment not found");
        }
    }


    private void deleteAssignment(final Assignment assignment) {
        assignmentRepository.delete(assignment);
    }

    private Assignment createNewAssignment(final Repository repository, final String key) {
        Assignment result;
        Assignment assignment = assignmentFactory.create();
        assignment.setRepository(repository);
        assignment.setKey(key);
        result = assignmentRepository.save(assignment);
        return result;
    }

    private Optional<Assignment> findByRepositoryAndSubAssignment(final Repository repository,
                                                                  final Long subAssignmentId) {
        return assignmentRepository.findByRepositoryAndSubAssignments_Id(repository, subAssignmentId);
    }
}
