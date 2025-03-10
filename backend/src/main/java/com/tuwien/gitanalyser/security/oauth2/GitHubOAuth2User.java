package com.tuwien.gitanalyser.security.oauth2;

import com.tuwien.gitanalyser.entity.utils.AuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class GitHubOAuth2User implements BasicAuth2User {

    private final OAuth2User oauth2User;

    public static final Logger LOGGER = LoggerFactory.getLogger(GitHubOAuth2User.class);

    public GitHubOAuth2User(final OAuth2User oauth2User) {
        this.oauth2User = oauth2User;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return oauth2User.getAuthorities();
    }

    @Override
    public String getName() {
        return oauth2User.getAttribute("name");
    }

    public AuthenticationProvider getAuthenticationProvider() {
        return AuthenticationProvider.GITHUB;
    }

    public Integer getPlatformId() {
        return oauth2User.getAttribute("id");
    }

    @Override
    public String getAccessToken() {
        return oauth2User.getAttribute("access_token");
    }

    @Override
    public String getRefreshToken() {
        return oauth2User.getAttribute("refresh_token");
    }

    @Override
    public String getEmail() {
        String email = oauth2User.getAttribute("email");
        LOGGER.info("Email: {}", email);
        return email;
    }

    @Override
    public String getPictureUrl() {
        return oauth2User.getAttribute("avatar_url");
    }

    @Override
    public int hashCode() {
        return Objects.hash(oauth2User);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GitHubOAuth2User that = (GitHubOAuth2User) o;
        return Objects.equals(oauth2User, that.oauth2User);
    }
}
