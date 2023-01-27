package com.tuwien.gitanalyser.service.APICalls;

import com.tuwien.gitanalyser.security.AuthenticationConstants;
import org.gitlab4j.api.Constants;
import org.gitlab4j.api.GitLabApi;
import org.springframework.stereotype.Service;

@Service
public class GitLabAPIFactory implements GitAPIFactory<GitLabApi> {

    public GitLabApi createObject(final String accessToken) {
        return new GitLabApi(AuthenticationConstants.GITLAB_CLIENT_URL, Constants.TokenType.OAUTH2_ACCESS, accessToken);
    }
}
