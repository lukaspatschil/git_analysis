package com.tuwien.gitanalyser.service;

import com.tuwien.gitanalyser.entity.User;
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
     * @return user with database id
     */
    User processOAuthPostLogin(BasicAuth2User auth2User, String accessToken);
}
