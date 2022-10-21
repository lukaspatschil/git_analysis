package com.tuwien.gitanalyser.security;

public class GitHubOAuthProviderProperties {

    public static final String[] SCOPES = {"user:email", "read:user", "repo"};
    public static final String CLIENT_NAME = "GitHub";
    public static final String REGISTRATION_ID = "github";
    public static final String CLIENT_ID = "abee8ba24898a4e6fd58";
    public static final String CLIENT_SECRET = "958218d36a6a1c85f70b04f57b4bc05afffc861f";
    public static final String REDIRECT_URI = "http://localhost:8080/login/oauth2/code/github";
    public static final String AUTHORIZATION_URI = "https://github.com/login/oauth/authorize";
    public static final String TOKEN_URI = "https://github.com/login/oauth/access_token";
    public static final String USER_INFO_URI = "https://api.github.com/user";

}
