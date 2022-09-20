package com.tuwien.gitanalyser.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfiguration {

    private static final String[] SCOPE = {"user:email", "read:user", "repo"};

    @Configuration
    public static class OAuth2LoginConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .oauth2Login(withDefaults());

            return http.build();
        }

        @Bean
        public ClientRegistrationRepository clientRegistrationRepository() {
            return new InMemoryClientRegistrationRepository(
                this.gitHubClientRegistration()
            );
        }

        private ClientRegistration gitLabClientRegistration() {
            return this.baseGitRegistration(
                GitLabOAuthProviderProperties.registrationId,
                GitLabOAuthProviderProperties.clientId,
                GitLabOAuthProviderProperties.clientSecret,
                GitLabOAuthProviderProperties.redirectUri,
                GitLabOAuthProviderProperties.authorizationUri,
                GitLabOAuthProviderProperties.tokenUri,
                GitLabOAuthProviderProperties.userInfoUri,
                GitLabOAuthProviderProperties.clientName
            );
        }

        public ClientRegistration gitHubClientRegistration() {
            return this.baseGitRegistration(
                GitHubOAuthProviderProperties.registrationId,
                GitHubOAuthProviderProperties.clientId,
                GitHubOAuthProviderProperties.clientSecret,
                GitHubOAuthProviderProperties.redirectUri,
                GitHubOAuthProviderProperties.authorizationUri,
                GitHubOAuthProviderProperties.tokenUri,
                GitHubOAuthProviderProperties.userInfoUri,
                GitHubOAuthProviderProperties.clientName
            );
        }

        private ClientRegistration baseGitRegistration(String registrationId, String clientId, String clientSecret,
                                                       String redirectUri, String authorizationUri,
                                                       String tokenUri, String userInfoUri, String clientName) {
            return ClientRegistration.withRegistrationId(registrationId)
                                     .clientId(clientId)
                                     .clientSecret(clientSecret)
                                     .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                     .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                     .redirectUri(redirectUri)
                                     .scope(SCOPE)
                                     .authorizationUri(authorizationUri)
                                     .tokenUri(tokenUri)
                                     .userInfoUri(userInfoUri)
                                     .userNameAttributeName("id")
                                     .clientName(clientName)
                                     .build();
        }

        //CommonOAuth2Provider.GITHUB

    }

}
