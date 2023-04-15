package com.tuwien.gitanalyser.entity.utils;

import com.tuwien.gitanalyser.entity.Assignment;
import com.tuwien.gitanalyser.entity.Factory;
import org.springframework.stereotype.Service;

@Service
public class AssignmentFactory implements Factory<Assignment> {
    @Override
    public Assignment create() {
        return new Assignment();
    }
}
