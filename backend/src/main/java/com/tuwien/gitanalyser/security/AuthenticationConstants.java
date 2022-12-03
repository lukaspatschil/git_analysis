package com.tuwien.gitanalyser.security;

public class AuthenticationConstants {

    public static final String ROLE_PREFIX = "ROLE_";
    public static final String JWT_CLAIM_AUTHORITY = "aut";
    public static final String JWT_CLAIM_PRINCIPAL = "pri";
    public static final String JWT_CLAIM_PRINCIPAL_ID = "pid";
    public static final String TOKEN_PREFIX = "Bearer ";


    public static final String[] GITHUB_SCOPES = {"user:email", "read:user", "repo"};
    public static final String GITHUB_CLIENT_NAME = "GitHub";
    public static final String GITHUB_REGISTRATION_ID = "github";
    public static final String GITHUB_CLIENT_ID = "abee8ba24898a4e6fd58";
    public static final String GITHUB_CLIENT_SECRET = "958218d36a6a1c85f70b04f57b4bc05afffc861f";
    public static final String GITHUB_REDIRECT_URI = "http://localhost:8080/login/oauth2/code/github";
    public static final String GITHUB_AUTHORIZATION_URI = "https://github.com/login/oauth/authorize";
    public static final String GITHUB_TOKEN_URI = "https://github.com/login/oauth/access_token";
    public static final String GITHUB_USER_INFO_URI = "https://api.github.com/user";



    public static final String GITLAB_REGISTRATION_ID = "gitlab";
    public static final String GITLAB_CLIENT_NAME = "GitLab";
    public static final String GITLAB_CLIENT_URL = "https://gitlab.com";
    public static final String GITLAB_CLIENT_ID = "b311e29e32f10daee03811fa223639fd597379efe20d86242551ad178950a29d";
    public static final String GITLAB_CLIENT_SECRET = "9b2fe521fe0776c9cbdcae6229b8586d7a32fb2dd376613b2c9e09a5138ae5d9";
    public static final String GITLAB_REDIRECT_URI = "http://localhost:8080/login/oauth2/code/gitlab";
    public static final String[] GITLAB_SCOPES = {"read_user", "read_repository", "read_api", "api"};
    public static final String GITLAB_AUTHORIZATION_URI = "https://gitlab.com/oauth/authorize";
    public static final String GITLAB_TOKEN_URI = "https://gitlab.com/oauth/token";
    public static final String GITLAB_USER_INFO_URI = "https://gitlab.com/api/v4/user";

}
