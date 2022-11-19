package com.tuwien.gitanalyser.security;

public class GitLabOAuthProviderProperties {

    public static final String REGISTRATION_ID = "gitlab";
    public static final String CLIENT_NAME = "GitLab";
    public static final String CLIENT_URL = "https://gitlab.com";
    public static final String CLIENT_ID = "b311e29e32f10daee03811fa223639fd597379efe20d86242551ad178950a29d";
    public static final String CLIENT_SECRET = "";
    public static final String REDIRECT_URI = "http://localhost:8080/login/oauth2/code/gitlab";
    public static final String[] SCOPES = {"read_user", "read_repository", "read_api", "api"};
    public static final String AUTHORIZATION_URI = "https://gitlab.com/oauth/authorize";
    public static final String TOKEN_URI = "https://gitlab.com/oauth/token";
    public static final String USER_INFO_URI = "https://gitlab.com/api/v4/user";
}
