package com.tuwien.gitanalyser.security;

public class GitHubOAuthProviderProperties {

    public static final String[] SCOPES = {"user:email", "read:user", "repo"};
    public static final String CLIENT_NAME = "GitHub";
    static final String REGISTRATION_ID = "github";
    static final String CLIENT_ID = "abee8ba24898a4e6fd58";
    static final String CLIENT_SECRET = "958218d36a6a1c85f70b04f57b4bc05afffc861f";
    static final String REDIRECT_URI = "http://localhost:8080/login/oauth2/code/github";
    static final String AUTHORIZATION_URI = "https://github.com/login/oauth/authorize";
    static final String TOKEN_URI = "https://github.com/login/oauth/access_token";
    static final String USER_INFO_URI = "https://api.github.com/user";

}
