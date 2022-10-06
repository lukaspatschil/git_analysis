package com.tuwien.gitanalyser.security.OAuth2;

import com.tuwien.gitanalyser.entity.utils.AuthenticationProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public class GitLabOAuth2User implements BasicAuth2User {
    private final OAuth2User oauth2User;

    public GitLabOAuth2User(final OAuth2User oauth2User) {
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
        return AuthenticationProvider.GITLAB;
    }

    public Integer getPlatformId() {
        return oauth2User.getAttribute("id");
    }
}
