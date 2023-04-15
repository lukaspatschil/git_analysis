package com.tuwien.gitanalyser.service.apiCalls.factory;

import com.tuwien.gitanalyser.service.GitAPIFactory;
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
