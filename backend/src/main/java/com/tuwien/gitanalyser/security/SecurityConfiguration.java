package com.tuwien.gitanalyser.security;

import com.tuwien.gitanalyser.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

@Configuration
public class SecurityConfiguration {

    private static final String H_2_CONSOLE_PATH = "/h2-console";

    @Autowired
    private UserService userService;

    @Configuration
    @EnableWebSecurity
    public class OAuth2LoginConfig {

        @Autowired
        private OAuth2AuthorizedClientRepository authorizedClientRepository;

        @Bean
        public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {

            /*http.cors().and().csrf().disable();
            http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);*/

            http
                .authorizeHttpRequests(authorize -> authorize
                                                        .mvcMatchers("/login/oauth2/code/**").permitAll()
                                                        .anyRequest().authenticated())
                .oauth2Login(settings -> settings.successHandler(successHandler()));

            return http.build();
        }

        @Bean
        public WebSecurityCustomizer webSecurityCustomizer() {
            return (web) -> web.ignoring()
                               // Spring Security should completely ignore URLs starting with /resources/
                               .antMatchers(H_2_CONSOLE_PATH + "/**");
        }

        @Bean
        public SimpleUrlAuthenticationSuccessHandler successHandler() {
            return new CustomAuthenticationSuccessHandler(userService, authorizedClientRepository);
        }

        @Bean
        public ClientRegistrationRepository clientRegistrationRepository() {
            return new InMemoryClientRegistrationRepository(AuthenticationConstants.gitHubClientRegistration(),
                                                            AuthenticationConstants.gitLabClientRegistration());
        }

    }
}
