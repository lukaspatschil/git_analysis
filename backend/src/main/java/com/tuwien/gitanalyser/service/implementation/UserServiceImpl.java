package com.tuwien.gitanalyser.service.implementation;

import com.sun.istack.NotNull;
import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.repository.UserRepository;
import com.tuwien.gitanalyser.security.OAuth2.BasicAuth2User;
import com.tuwien.gitanalyser.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    public UserServiceImpl(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * get single user by id.
     *
     * @param id of the user
     * @return single user
     */
    @Override
    public User getUser(final @NotNull Long id) {
        return userRepository.findById(id).orElseThrow(() -> {
            LOGGER.error("User: Could not find event with id " + id);
            return new NotFoundException();
        });
    }

    /**
     * get all users.
     *
     * @return list of users
     */
    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    public void processOAuthPostLogin(final BasicAuth2User user) {
        List<User> existUser = userRepository.findByPlatformAndPlatformId(
            user.getAuthenticationProvider(),
            user.getPlatformId());

        if (existUser.size() == 0) {
            User newUser = new User();
            newUser.setUsername(user.getName());
            newUser.setPlatformId(user.getPlatformId());
            newUser.setAuthenticationProvider(user.getAuthenticationProvider());
            newUser.setAccessToken(user.getAccessToken());
            newUser.setRefreshToken(user.getRefreshToken());

            userRepository.save(newUser);
        }

    }
}
