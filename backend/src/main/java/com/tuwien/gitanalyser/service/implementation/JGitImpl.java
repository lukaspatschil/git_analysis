package com.tuwien.gitanalyser.service.implementation;

import com.tuwien.gitanalyser.service.JGit;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class JGitImpl implements JGit {

    private static final Logger LOGGER = LoggerFactory.getLogger(JGitImpl.class);

    @Override
    public void cloneRepository(final String url, final Long databaseId, final String accessToken) {
        LOGGER.info("JGitImpl: clone " + url + " " + databaseId);
        try {
            Git
                .cloneRepository()
                .setURI(url)
                .setDirectory(new File("./repos/" + databaseId))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider("token", accessToken))
                .call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }
}
