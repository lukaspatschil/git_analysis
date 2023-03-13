package com.tuwien.gitanalyser.service.implementation;

import com.sun.istack.NotNull;
import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.repository.UserRepository;
import com.tuwien.gitanalyser.security.oAuth2.BasicAuth2User;
import com.tuwien.gitanalyser.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    public UserServiceImpl(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public User getUser(final @NotNull Long id) throws NotFoundException {
        return userRepository.findById(id).orElseThrow(() -> {
            LOGGER.error("User: Could not find user with id {}", id);
            return new NotFoundException("User: Could not find user with id " + id);
        });
    }

    @Override
    public User processOAuthPostLogin(final BasicAuth2User auth2User, final String accessToken) {
        User user;

        Optional<User> existUsers = userRepository.findByAuthenticationProviderAndPlatformId(
            auth2User.getAuthenticationProvider(),
            auth2User.getPlatformId());

        if (existUsers.isEmpty()) {
            // create new user
            User newUser = new User();
            newUser.setEmail(auth2User.getEmail());
            newUser.setUsername(auth2User.getName());
            newUser.setPlatformId(auth2User.getPlatformId());
            newUser.setAuthenticationProvider(auth2User.getAuthenticationProvider());
            newUser.setAccessToken(accessToken);
            newUser.setPictureUrl(auth2User.getPictureUrl());

            user = userRepository.save(newUser);
        } else {
            user = existUsers.get();
        }

        return user;

    }
}
