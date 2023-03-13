package com.tuwien.gitanalyser.entity;

import org.springframework.stereotype.Service;

@Service
public class RepositoryFactory implements Factory<Repository> {

    @Override
    public Repository create() {
        return new Repository();
    }
}
