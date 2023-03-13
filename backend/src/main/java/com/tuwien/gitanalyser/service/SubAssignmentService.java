package com.tuwien.gitanalyser.service;

import com.tuwien.gitanalyser.entity.Assignment;
import com.tuwien.gitanalyser.entity.SubAssignment;

public interface SubAssignmentService {

    SubAssignment addSubAssignment(Assignment assignment, SubAssignment subAssignment);
}
