package com.tuwien.gitanalyser.security;

public class GitLabOAuthProviderProperties {

    static final String REGISTRATION_ID = "gitlab";

    public static final String CLIENT_URL = "https://gitlab.com";
    static final String CLIENT_ID = "b311e29e32f10daee03811fa223639fd597379efe20d86242551ad178950a29d";
    static final String CLIENT_SECRET = "9b2fe521fe0776c9cbdcae6229b8586d7a32fb2dd376613b2c9e09a5138ae5d9";
    static final String REDIRECT_URI = "http://localhost:8080/login/oauth2/code/gitlab";
    static final String[] SCOPES = {"read_user", "read_repository", "read_api", "api"};
    static final String AUTHORIZATION_URI = "https://gitlab.com/oauth/authorize";
    static final String TOKEN_URI = "https://gitlab.com/oauth/token";
    static final String USER_INFO_URI = "https://gitlab.com/api/v4/user";
    public static final String CLIENT_NAME = "GitLab";
}
