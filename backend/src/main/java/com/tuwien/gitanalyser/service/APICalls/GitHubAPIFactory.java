package com.tuwien.gitanalyser.service.APICalls;

import org.kohsuke.github.GitHub;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GitHubAPIFactory implements GitAPIFactory<GitHub> {
    @Override
    public GitHub createObject(final String accessToken) throws IOException {
        return GitHub.connectUsingOAuth(accessToken);
    }
}
