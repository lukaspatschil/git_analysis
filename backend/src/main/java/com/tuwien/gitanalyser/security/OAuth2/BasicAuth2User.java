package com.tuwien.gitanalyser.security.OAuth2;

import com.tuwien.gitanalyser.entity.utils.AuthenticationProvider;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface BasicAuth2User extends OAuth2User {

    /**
     * Returns the AuthenticationProvider of the user.
     * @return AuthenticationProvider
     */
    AuthenticationProvider getAuthenticationProvider();

    /**
     * Gets the id which the user has on the platform.
     *
     * @return id
     */
    Integer getPlatformId();

    /**
     * Returns the access token of the user.
     *
     * @return access token
     */
    String getAccessToken();

    /**
     * Returns the refresh token of the user.
     *
     * @return refresh token
     */
    String getRefreshToken();

    /**
     * Returns the email of the user.
     *
     * @return email
     */
    String getEmail();
}
