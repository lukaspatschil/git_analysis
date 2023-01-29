package com.tuwien.gitanalyser.security;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.Base64;

public class AuthenticationConstants {

    public static final String ROLE_PREFIX = "ROLE_";
    public static final String TOKEN_PREFIX = "Bearer ";

    public static final String[] GITHUB_SCOPES = {"user:email", "read:user", "repo"};
    public static final String GITHUB_CLIENT_NAME = "GitHub";
    public static final String GITHUB_REGISTRATION_ID = "github";
    public static final String GITHUB_CLIENT_ID = "abee8ba24898a4e6fd58";
    public static final String GITHUB_CLIENT_SECRET = System.getenv("GITHUB_CLIENT_SECRET");
    public static final String GITHUB_REDIRECT_URI = "http://localhost:8080/login/oauth2/code/github";
    public static final String GITHUB_AUTHORIZATION_URI = "https://github.com/login/oauth/authorize";
    public static final String GITHUB_TOKEN_URI = "https://github.com/login/oauth/access_token";
    public static final String GITHUB_USER_INFO_URI = "https://api.github.com/user";
    public static final String GITLAB_REGISTRATION_ID = "gitlab";
    public static final String GITLAB_CLIENT_NAME = "GitLab";
    public static final String GITLAB_CLIENT_URL = "https://gitlab.com";
    public static final String GITLAB_CLIENT_ID = "b311e29e32f10daee03811fa223639fd597379efe20d86242551ad178950a29d";
    public static final String GITLAB_CLIENT_SECRET = System.getenv("GITLAB_CLIENT_SECRET");
    public static final String GITLAB_REDIRECT_URI = "http://localhost:8080/login/oauth2/code/gitlab";
    public static final String[] GITLAB_SCOPES = {"read_user", "read_repository", "read_api", "api"};
    public static final String GITLAB_AUTHORIZATION_URI = "https://gitlab.com/oauth/authorize";
    public static final String GITLAB_TOKEN_URI = "https://gitlab.com/oauth/token";
    public static final String GITLAB_USER_INFO_URI = "https://gitlab.com/api/v4/user";
    public static final String FRONTEND_REDIRECT_AFTER_LOGIN_URL = "http://localhost:5173/"; // change to
    // redirect
    public static final long JWT_VALIDITY_IN_MILLISECONDS = 3600000; // 1h
    private static final String JWT_SECRET_KEY_PLAIN = "mySecretKey";
    public static final String JWT_SECRET_KEY =
        Base64.getEncoder().encodeToString(AuthenticationConstants.JWT_SECRET_KEY_PLAIN.getBytes());

    public static ClientRegistration gitLabClientRegistration() {
        return baseGitRegistration(AuthenticationConstants.GITLAB_REGISTRATION_ID,
                                   AuthenticationConstants.GITLAB_CLIENT_ID,
                                   AuthenticationConstants.GITLAB_CLIENT_SECRET,
                                   AuthenticationConstants.GITLAB_REDIRECT_URI,
                                   AuthenticationConstants.GITLAB_SCOPES,
                                   AuthenticationConstants.GITLAB_AUTHORIZATION_URI,
                                   AuthenticationConstants.GITLAB_TOKEN_URI,
                                   AuthenticationConstants.GITLAB_USER_INFO_URI,
                                   AuthenticationConstants.GITLAB_CLIENT_NAME);
    }

    public static ClientRegistration gitHubClientRegistration() {
        return baseGitRegistration(AuthenticationConstants.GITHUB_REGISTRATION_ID,
                                   AuthenticationConstants.GITHUB_CLIENT_ID,
                                   AuthenticationConstants.GITHUB_CLIENT_SECRET,
                                   AuthenticationConstants.GITHUB_REDIRECT_URI,
                                   AuthenticationConstants.GITHUB_SCOPES,
                                   AuthenticationConstants.GITHUB_AUTHORIZATION_URI,
                                   AuthenticationConstants.GITHUB_TOKEN_URI,
                                   AuthenticationConstants.GITHUB_USER_INFO_URI,
                                   AuthenticationConstants.GITHUB_CLIENT_NAME);
    }

    private static ClientRegistration baseGitRegistration(final String registrationId, final String clientId,
                                                          final String clientSecret, final String redirectUri,
                                                          final String[] scopes, final String authorizationUri,
                                                          final String tokenUri, final String userInfoUri,
                                                          final String clientName) {
        return ClientRegistration
                   .withRegistrationId(registrationId)
                   .clientId(clientId)
                   .clientSecret(clientSecret)
                   .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                   .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                   .redirectUri(redirectUri)
                   .scope(scopes)
                   .authorizationUri(authorizationUri)
                   .tokenUri(tokenUri)
                   .userInfoUri(userInfoUri)
                   .userNameAttributeName("id")
                   .clientName(clientName)
                   .build();
    }

}
