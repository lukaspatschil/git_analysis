package com.tuwien.gitanalyser.service.implementation;

import com.tuwien.gitanalyser.entity.Assignment;
import com.tuwien.gitanalyser.entity.SubAssignment;
import com.tuwien.gitanalyser.entity.SubAssignmentFactory;
import com.tuwien.gitanalyser.exception.ConflictException;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.repository.SubAssignmentRepository;
import com.tuwien.gitanalyser.service.SubAssignmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SubAssignmentServiceImpl implements SubAssignmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubAssignmentService.class);

    private final SubAssignmentRepository subAssignmentRepository;
    private final SubAssignmentFactory subAssignmentFactory;

    public SubAssignmentServiceImpl(final SubAssignmentRepository subAssignmentRepository,
                                    final SubAssignmentFactory subAssignmentFactory) {
        this.subAssignmentRepository = subAssignmentRepository;
        this.subAssignmentFactory = subAssignmentFactory;
    }

    @Override
    public SubAssignment addSubAssignment(final Assignment assignment, final String assignedName) {
        LOGGER.info("addSubAssignment with assignment {} and assignedName {}", assignment, assignedName);

        SubAssignment subAssignment = subAssignmentFactory.create();
        subAssignment.setAssignedName(assignedName);
        subAssignment.setAssignment(assignment);

        try {
            return subAssignmentRepository.save(subAssignment);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new ConflictException(e.getMessage());
        }
    }

    @Override
    public void deleteSubAssignmentById(final Long subAssignmentId) {
        LOGGER.info("deleteSubAssignmentById with subAssignmentId {}", subAssignmentId);
        Optional<SubAssignment> subAssignment = subAssignmentRepository.findById(subAssignmentId);
        if (subAssignment.isEmpty()) {
            throw new NotFoundException("SubAssignment not found");
        } else {
            subAssignmentRepository.delete(subAssignment.get());
        }
    }
}
