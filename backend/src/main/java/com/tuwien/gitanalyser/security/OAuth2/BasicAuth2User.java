package com.tuwien.gitanalyser.security.OAuth2;

import com.tuwien.gitanalyser.entity.utils.AuthenticationProvider;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface BasicAuth2User extends OAuth2User {

    AuthenticationProvider getAuthenticationProvider();

    Integer getPlatformId();

}
