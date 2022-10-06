package com.tuwien.gitanalyser.service;

import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.security.OAuth2.BasicAuth2User;

import java.util.List;

public interface UserService {
    User getUser(Long id);

    List<User> getAll();

    void processOAuthPostLogin(BasicAuth2User gitlabOAuth2User);
}
