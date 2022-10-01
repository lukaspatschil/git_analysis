package com.tuwien.gitanalyser.security;

public class GitLabOAuthProviderProperties {


    // TODO

    final static String registrationId = "gitlab";
    final static String clientId = "b311e29e32f10daee03811fa223639fd597379efe20d86242551ad178950a29d";
    final static String clientSecret = "9b2fe521fe0776c9cbdcae6229b8586d7a32fb2dd376613b2c9e09a5138ae5d9";
    final static String redirectUri = "http://localhost:8080/login/oauth2/code/gitlab";
    public static String[] scopes = {"read_user", "read_repository"};
    final static String authorizationUri = "https://gitlab.com/oauth/authorize";
    final static String tokenUri = "https://gitlab.com/oauth/token";
    final static String userInfoUri = "https://gitlab.com/api/v4/user";
    final static String clientName = "GitLab";
}
