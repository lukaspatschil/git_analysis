package com.tuwien.gitanalyser.service.implementation;

import com.sun.istack.NotNull;
import com.tuwien.gitanalyser.endpoints.dtos.internal.RefreshAuthenticationInternalDTO;
import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.entity.utils.UserFingerprintPair;
import com.tuwien.gitanalyser.exception.AuthenticationException;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.repository.UserRepository;
import com.tuwien.gitanalyser.security.jwt.FingerprintPair;
import com.tuwien.gitanalyser.security.jwt.FingerprintService;
import com.tuwien.gitanalyser.security.jwt.JWTTokenProvider;
import com.tuwien.gitanalyser.security.oauth2.BasicAuth2User;
import com.tuwien.gitanalyser.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    private final FingerprintService fingerprintService;

    private final JWTTokenProvider jwtTokenProvider;

    public UserServiceImpl(final UserRepository userRepository,
                           final FingerprintService fingerprintService,
                           @Lazy final JWTTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.fingerprintService = fingerprintService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public User getUser(final @NotNull Long id) throws NotFoundException {
        return userRepository.findById(id).orElseThrow(() -> {
            LOGGER.error("User: Could not find user with id {}", id);
            return new NotFoundException("User: Could not find user with id " + id);
        });
    }

    @Override
    public UserFingerprintPair processOAuthPostLogin(final BasicAuth2User auth2User, final String accessToken,
                                                     final String refreshToken) {
        User user;

        Optional<User> existUsers = userRepository.findByAuthenticationProviderAndPlatformId(
            auth2User.getAuthenticationProvider(),
            auth2User.getPlatformId());

        FingerprintPair fingerprintPair = fingerprintService.createFingerprint();

        if (existUsers.isEmpty()) {
            // create new user

            User newUser = new User();
            newUser.setEmail(auth2User.getEmail());
            newUser.setUsername(auth2User.getName());
            newUser.setPlatformId(auth2User.getPlatformId());
            newUser.setAuthenticationProvider(auth2User.getAuthenticationProvider());
            newUser.setAccessToken(accessToken);
            newUser.setRefreshToken(refreshToken);
            newUser.setPictureUrl(auth2User.getPictureUrl());
            newUser.setFingerPrintHash(fingerprintPair.getHash());

            user = userRepository.save(newUser);
        } else {
            user = existUsers.get();
            user.setAccessToken(accessToken);
            user.setRefreshToken(refreshToken);
            user.setFingerPrintHash(fingerprintPair.getHash());
            userRepository.save(user);
        }

        return new UserFingerprintPair(user, fingerprintPair);
    }

    @Override
    public RefreshAuthenticationInternalDTO refreshAccessToken(final String refreshToken, final String fingerprint) {

        Long userId = jwtTokenProvider.getUserId(refreshToken);

        User user = userRepository.findById(userId).orElseThrow(() -> {
            LOGGER.error("User: Could not find user with id {}", userId);
            return new AuthenticationException("user is invalid");
        });

        String hash = fingerprintService.sha256(fingerprint);

        if (hash.equals(user.getFingerPrintHash())) {

            String newAccessToken = jwtTokenProvider.createAccessToken(userId);
            String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);
            FingerprintPair fingerprintPair = fingerprintService.createFingerprint();

            user.setFingerPrintHash(fingerprintPair.getHash());
            userRepository.save(user);

            return new RefreshAuthenticationInternalDTO(newAccessToken,
                                                        newRefreshToken,
                                                        fingerprintPair.getFingerprint());
        } else {
            LOGGER.info("Fingerprint does not match");
            throw new AuthenticationException("Fingerprint does not match");
        }
    }

    @Override
    public void refreshGitAccessToken(final Long userId, final String accessToken, final String refreshToken) {
        LOGGER.info("refreshGitAccessToken save new tokens for user {}", userId);

        User user = getUser(userId);
        user.setAccessToken(accessToken);
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

    }
}
