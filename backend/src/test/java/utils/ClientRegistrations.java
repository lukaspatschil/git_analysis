package utils;

import com.tuwien.gitanalyser.security.AuthenticationConstants;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

public class ClientRegistrations {
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

    public static ClientRegistration baseGitRegistration(final String registrationId, final String clientId,
                                                         final String clientSecret, final String redirectUri,
                                                         final String[] scopes, final String authorizationUri,
                                                         final String tokenUri, final String userInfoUri,
                                                         final String clientName) {
        return ClientRegistration.withRegistrationId(registrationId).clientId(clientId).clientSecret(clientSecret)
                                 .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                 .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                 .redirectUri(redirectUri).scope(scopes).authorizationUri(authorizationUri)
                                 .tokenUri(tokenUri).userInfoUri(userInfoUri).userNameAttributeName("id")
                                 .clientName(clientName).build();
    }
}
