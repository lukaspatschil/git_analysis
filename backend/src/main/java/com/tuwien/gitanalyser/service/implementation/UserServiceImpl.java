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
    public User getUser(final @NotNull Long id) throws NotFoundException {
        return userRepository.findById(id).orElseThrow(() -> {
            LOGGER.error("User: Could not find user with id " + id);
            return new NotFoundException("User: Could not find user with id " + id);
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

    @Override
    public User processOAuthPostLogin(final BasicAuth2User auth2User, final String accessToken) {
        User userWithDatabaseId;

        List<User> existUsers = userRepository.findByPlatformAndPlatformId(
            auth2User.getAuthenticationProvider(),
            auth2User.getPlatformId());

        if (existUsers.size() == 0) {
            // create new user
            User newUser = new User();
            newUser.setEmail(auth2User.getEmail());
            newUser.setUsername(auth2User.getName());
            newUser.setPlatformId(auth2User.getPlatformId());
            newUser.setAuthenticationProvider(auth2User.getAuthenticationProvider());
            newUser.setAccessToken(accessToken);

            userWithDatabaseId = userRepository.save(newUser);
        } else {
            userWithDatabaseId = existUsers.get(0);
        }

        return userWithDatabaseId;

    }
}
