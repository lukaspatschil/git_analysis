package com.tuwien.gitanalyser.entity;

import org.springframework.stereotype.Service;

@Service
public class SubAssignmentFactory implements Factory<SubAssignment> {
    @Override
    public SubAssignment create() {
        return new SubAssignment();
    }
}
