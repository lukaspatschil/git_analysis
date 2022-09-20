package com.tuwien.gitanalyser.security;

public class GitHubOAuthProviderProperties {

    final static String registrationId = "github";
    final static String clientId = "abee8ba24898a4e6fd58";
    final static String clientSecret = "958218d36a6a1c85f70b04f57b4bc05afffc861f";
    final static String redirectUri = "http://localhost:8080/login/oauth2/code/github";
    final static String authorizationUri = "https://github.com/login/oauth/authorize";
    final static String tokenUri = "https://github.com/login/oauth/access_token";
    final static String userInfoUri = "https://api.github.com/user";
    final static String clientName = "github";

}
