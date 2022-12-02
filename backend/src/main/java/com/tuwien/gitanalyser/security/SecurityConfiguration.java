package com.tuwien.gitanalyser.security;

import com.tuwien.gitanalyser.security.OAuth2.CustomOAuth2UserService;
import com.tuwien.gitanalyser.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

@Configuration
public class SecurityConfiguration {

    private static final String H_2_CONSOLE_PATH = "/h2-console";
    @Autowired
    private CustomOAuth2UserService oauthUserService;

    @Autowired
    private UserService userService;

    @Configuration
    @EnableWebSecurity
    public class OAuth2LoginConfig {

        @Bean
        public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {

            /*http.cors().and().csrf().disable();
            http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);*/

            http
                .authorizeHttpRequests(authorize -> authorize
                                                        .mvcMatchers("/login/oauth2/code/**").permitAll()
                                                        .anyRequest().authenticated())
                .oauth2Login(settings -> settings
                                             .successHandler(successHandler())
                                             .userInfoEndpoint(config -> config.userService(oauthUserService)));

            return http.build();
        }

        @Bean
        public WebSecurityCustomizer webSecurityCustomizer() {
            return (web) -> web.ignoring()
                               // Spring Security should completely ignore URLs starting with /resources/
                               .antMatchers(H_2_CONSOLE_PATH + "/**");
        }

        // Used by spring security if CORS is enabled.
        /*@Bean
        public CorsFilter corsFilter() {
            UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowCredentials(true);
            config.addAllowedOrigin("*");
            config.addAllowedHeader("*");
            config.addAllowedMethod("*");
            source.registerCorsConfiguration("/**", config);
            return new CorsFilter(source);
        }*/

        @Bean
        public ClientRegistrationRepository clientRegistrationRepository() {
            return new InMemoryClientRegistrationRepository(this.gitHubClientRegistration(),
                                                            this.gitLabClientRegistration());
        }

        @Bean
        public SimpleUrlAuthenticationSuccessHandler successHandler() {
            return new CustomAuthenticationSuccessHandler(userService);
        }

        private ClientRegistration gitLabClientRegistration() {
            return this.baseGitRegistration(GitLabOAuthProviderProperties.REGISTRATION_ID,
                                            GitLabOAuthProviderProperties.CLIENT_ID,
                                            GitLabOAuthProviderProperties.CLIENT_SECRET,
                                            GitLabOAuthProviderProperties.REDIRECT_URI,
                                            GitLabOAuthProviderProperties.SCOPES,
                                            GitLabOAuthProviderProperties.AUTHORIZATION_URI,
                                            GitLabOAuthProviderProperties.TOKEN_URI,
                                            GitLabOAuthProviderProperties.USER_INFO_URI,
                                            GitLabOAuthProviderProperties.CLIENT_NAME);
        }

        private ClientRegistration gitHubClientRegistration() {
            return this.baseGitRegistration(GitHubOAuthProviderProperties.REGISTRATION_ID,
                                            GitHubOAuthProviderProperties.CLIENT_ID,
                                            GitHubOAuthProviderProperties.CLIENT_SECRET,
                                            GitHubOAuthProviderProperties.REDIRECT_URI,
                                            GitHubOAuthProviderProperties.SCOPES,
                                            GitHubOAuthProviderProperties.AUTHORIZATION_URI,
                                            GitHubOAuthProviderProperties.TOKEN_URI,
                                            GitHubOAuthProviderProperties.USER_INFO_URI,
                                            GitHubOAuthProviderProperties.CLIENT_NAME);
        }

        private ClientRegistration baseGitRegistration(final String registrationId, final String clientId,
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
}
