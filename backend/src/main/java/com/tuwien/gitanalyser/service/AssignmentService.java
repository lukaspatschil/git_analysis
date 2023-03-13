package com.tuwien.gitanalyser.service;

import com.tuwien.gitanalyser.entity.Assignment;
import com.tuwien.gitanalyser.entity.Repository;

public interface AssignmentService {

    Assignment getOrCreateAssignment(Repository repository, String key);
}
