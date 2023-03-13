package com.tuwien.gitanalyser.service.implementation;

import com.tuwien.gitanalyser.entity.Assignment;
import com.tuwien.gitanalyser.entity.SubAssignment;
import com.tuwien.gitanalyser.repository.SubAssignmentRepository;
import com.tuwien.gitanalyser.service.SubAssignmentService;
import org.springframework.stereotype.Service;

@Service
public class SubAssignmentServiceImpl implements SubAssignmentService {

    private final SubAssignmentRepository subAssignmentRepository;

    public SubAssignmentServiceImpl(final SubAssignmentRepository subAssignmentRepository) {
        this.subAssignmentRepository = subAssignmentRepository;
    }

    @Override
    public SubAssignment addSubAssignment(final Assignment assignment, final SubAssignment subAssignment) {
        subAssignment.setAssignment(assignment);
        return subAssignmentRepository.save(subAssignment);
    }
}
