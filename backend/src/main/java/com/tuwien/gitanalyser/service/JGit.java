package com.tuwien.gitanalyser.service;

public interface JGit {
    void cloneRepository(String url, Long databaseId, String accessToken);
}
