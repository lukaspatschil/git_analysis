package com.tuwien.gitanalyser.security;

import com.tuwien.gitanalyser.security.OAuth2.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

@Configuration
public class SecurityConfiguration {

    @Autowired
    private CustomOAuth2UserService oauthUserService;

    private static final String H_2_CONSOLEPATH = "/h2-console";

    @Configuration
    public class OAuth2LoginConfig {
        @Bean
        public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
            http
                .authorizeHttpRequests(
                    authorize -> {
                        authorize
                            .antMatchers(H_2_CONSOLEPATH + "/**").permitAll();

                        authorize
                            .anyRequest().authenticated();
                    }
                )
                .oauth2Login(
                    settings -> settings
                                    .successHandler(successHandler())
                                    .userInfoEndpoint().userService(oauthUserService)
                );

            return http.build();
        }

        @Bean
        public ClientRegistrationRepository clientRegistrationRepository() {
            return new InMemoryClientRegistrationRepository(
                this.gitHubClientRegistration(),
                this.gitLabClientRegistration()
            );
        }

        @Bean
        public SimpleUrlAuthenticationSuccessHandler successHandler() {
            return new CustomAuthenticationSuccessHandler();
        }

        private ClientRegistration gitLabClientRegistration() {
            return this.baseGitRegistration(
                GitLabOAuthProviderProperties.REGISTRATION_ID,
                GitLabOAuthProviderProperties.CLIENT_ID,
                GitLabOAuthProviderProperties.CLIENT_SECRET,
                GitLabOAuthProviderProperties.REDIRECT_URI,
                GitLabOAuthProviderProperties.SCOPES,
                GitLabOAuthProviderProperties.AUTHORIZATION_URI,
                GitLabOAuthProviderProperties.TOKEN_URI,
                GitLabOAuthProviderProperties.USER_INFO_URI,
                GitLabOAuthProviderProperties.CLIENT_NAME
            );
        }

        private ClientRegistration gitHubClientRegistration() {
            return this.baseGitRegistration(
                GitHubOAuthProviderProperties.REGISTRATION_ID,
                GitHubOAuthProviderProperties.CLIENT_ID,
                GitHubOAuthProviderProperties.CLIENT_SECRET,
                GitHubOAuthProviderProperties.REDIRECT_URI,
                GitHubOAuthProviderProperties.SCOPES,
                GitHubOAuthProviderProperties.AUTHORIZATION_URI,
                GitHubOAuthProviderProperties.TOKEN_URI,
                GitHubOAuthProviderProperties.USER_INFO_URI,
                GitHubOAuthProviderProperties.CLIENT_NAME
            );
        }

        private ClientRegistration baseGitRegistration(final String registrationId,
                                                       final String clientId,
                                                       final String clientSecret,
                                                       final String redirectUri,
                                                       final String[] scopes,
                                                       final String authorizationUri,
                                                       final String tokenUri,
                                                       final String userInfoUri,
                                                       final String clientName) {
            return ClientRegistration.withRegistrationId(registrationId)
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
}
