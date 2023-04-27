package com.tuwien.gitanalyser.service;

import com.tuwien.gitanalyser.endpoints.dtos.internal.RefreshAuthenticationInternalDTO;
import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.entity.utils.UserFingerprintPair;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.security.oauth2.BasicAuth2User;

public interface UserService {

    /**
     * get single user by id.
     *
     * @param id of the user
     * @return single user
     */
    User getUser(Long id) throws NotFoundException;

    /**
     * create new user if the user does not exist yet - otherwise creates it.
     *
     * @param auth2User   basic oauth2 user
     * @param accessToken access token
     * @param refreshToken refresh token
     * @return user with database id
     */
    UserFingerprintPair processOAuthPostLogin(BasicAuth2User auth2User, String accessToken, String refreshToken);

    RefreshAuthenticationInternalDTO refreshAccessToken(String refreshToken, String fingerprint);

    void refreshGitAccessToken(Long userId, String accessToken, String refreshToken);
}
