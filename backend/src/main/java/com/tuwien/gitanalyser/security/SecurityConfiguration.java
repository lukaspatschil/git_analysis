package com.tuwien.gitanalyser.security;

import com.tuwien.gitanalyser.security.jwt.JWTTokenProviderImpl;
import com.tuwien.gitanalyser.security.jwt.JwtTokenFilter;
import com.tuwien.gitanalyser.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.Filter;
import java.util.Collections;
import java.util.List;

@Configuration
public class SecurityConfiguration {

    private static final String H_2_CONSOLE_PATH = "/h2-console";

    private final UserService userService;

    public SecurityConfiguration(final UserService userService) {
        this.userService = userService;
    }

    @Configuration
    @EnableWebSecurity
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    @Order(SecurityProperties.BASIC_AUTH_ORDER)
    public class OAuth2LoginConfig {

        @Autowired
        private OAuth2AuthorizedClientRepository authorizedClientRepository;

        @Autowired
        private JWTTokenProviderImpl jwtTokenProviderImpl;

        @Bean
        public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {

            http.cors().and().csrf().disable()
                .sessionManagement(config -> {
                    config.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
                .authorizeHttpRequests(
                    authorize -> {
                        authorize
                            .antMatchers("/login/oauth2/code/**", "/oauth2/authorization/**").permitAll()
                            .antMatchers("/v3/api-docs/**",
                                         "/swagger-ui/**",
                                         "/swagger-ui.html").permitAll()
                            .anyRequest().authenticated();
                    }
                )
                .oauth2Login(settings -> {
                    settings.successHandler(successHandler());
                })
                .httpBasic().disable();

            http.addFilterBefore(jwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

            return http.build();
        }

        private Filter jwtTokenFilter() {
            return new JwtTokenFilter(jwtTokenProviderImpl);
        }

        @Bean
        public WebSecurityCustomizer webSecurityCustomizer() {
            return (web) -> web.ignoring()
                               // Spring Security should completely ignore URLs starting with /h2-console
                               .antMatchers(H_2_CONSOLE_PATH + "/**")
                               .antMatchers("/login/oauth2/authorization/**")
                               .antMatchers("/v3/api-docs/**",
                                            "/swagger-ui/**",
                                            "/swagger-ui.html");
        }

        @Bean
        public SimpleUrlAuthenticationSuccessHandler successHandler() {
            return new CustomAuthenticationSuccessHandler(userService,
                                                          authorizedClientRepository,
                                                          jwtTokenProviderImpl);
        }

        @Bean
        public ClientRegistrationRepository clientRegistrationRepository() {
            return new InMemoryClientRegistrationRepository(AuthenticationConstants.gitHubClientRegistration(),
                                                            AuthenticationConstants.gitLabClientRegistration());
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
            final List<String> permitAll = Collections.singletonList("*");
            final List<String> permitMethods =
                List.of(HttpMethod.GET.name(),
                        HttpMethod.POST.name(),
                        HttpMethod.PUT.name(),
                        HttpMethod.PATCH.name(),
                        HttpMethod.DELETE.name(),
                        HttpMethod.OPTIONS.name(),
                        HttpMethod.HEAD.name(),
                        HttpMethod.TRACE.name());
            final CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedHeaders(permitAll);
            configuration.setAllowedOrigins(permitAll);
            configuration.setAllowedMethods(permitMethods);
            final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", configuration);
            return source;
        }
    }
}
