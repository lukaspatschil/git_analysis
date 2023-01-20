package com.tuwien.gitanalyser.service;

import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.security.OAuth2.BasicAuth2User;

import java.util.List;

public interface UserService {
    User getUser(Long id) throws NotFoundException;

    List<User> getAll();

    User processOAuthPostLogin(BasicAuth2User auth2User, String accessToken);
}
