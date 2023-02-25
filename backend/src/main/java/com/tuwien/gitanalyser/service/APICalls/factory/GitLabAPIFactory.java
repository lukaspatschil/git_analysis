package com.tuwien.gitanalyser.service.APICalls.factory;

import com.tuwien.gitanalyser.security.AuthenticationConstants;
import com.tuwien.gitanalyser.service.GitAPIFactory;
import org.gitlab4j.api.Constants;
import org.gitlab4j.api.GitLabApi;
import org.springframework.stereotype.Service;

@Service
public class GitLabAPIFactory implements GitAPIFactory<GitLabApi> {

    public GitLabApi createObject(final String accessToken) {
        return new GitLabApi(AuthenticationConstants.GITLAB_CLIENT_URL, Constants.TokenType.OAUTH2_ACCESS, accessToken);
    }
}
