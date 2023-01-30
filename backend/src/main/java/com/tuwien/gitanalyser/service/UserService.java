package com.tuwien.gitanalyser.service;

import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.security.OAuth2.BasicAuth2User;

public interface UserService {
    User getUser(Long id) throws NotFoundException;

    User processOAuthPostLogin(BasicAuth2User auth2User, String accessToken);
}
