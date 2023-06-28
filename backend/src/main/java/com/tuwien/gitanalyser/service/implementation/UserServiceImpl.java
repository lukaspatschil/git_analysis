package com.tuwien.gitanalyser.service.implementation;

import com.sun.istack.NotNull;
import com.tuwien.gitanalyser.endpoints.dtos.internal.RefreshAuthenticationInternalDTO;
import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.entity.utils.AuthenticationProvider;
import com.tuwien.gitanalyser.entity.utils.UserFingerprintPair;
import com.tuwien.gitanalyser.exception.AuthenticationException;
import com.tuwien.gitanalyser.exception.GitException;
import com.tuwien.gitanalyser.exception.NoProviderFoundException;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.repository.UserRepository;
import com.tuwien.gitanalyser.security.jwt.FingerprintPair;
import com.tuwien.gitanalyser.security.jwt.FingerprintService;
import com.tuwien.gitanalyser.security.jwt.JWTTokenProvider;
import com.tuwien.gitanalyser.security.oauth2.BasicAuth2User;
import com.tuwien.gitanalyser.service.GitService;
import com.tuwien.gitanalyser.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final FingerprintService fingerprintService;

    private final JWTTokenProvider jwtTokenProvider;

    private final GitService gitService;

    public UserServiceImpl(final UserRepository userRepository,
                           final FingerprintService fingerprintService,
                           @Lazy final JWTTokenProvider jwtTokenProvider,
                           @Lazy final GitService gitService) {
        this.userRepository = userRepository;
        this.fingerprintService = fingerprintService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.gitService = gitService;
    }

    @Override
    public User getUser(final @NotNull Long id) throws NotFoundException {
        return userRepository.findById(id)
                             .orElseThrow(() -> new NotFoundException("User: Could not find user with id " + id));
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

            if (auth2User.getAuthenticationProvider() == AuthenticationProvider.GITHUB
                    && auth2User.getEmail() == null) {
                user = getEMailForGithub(user);
            }
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

        User user = userRepository.findById(userId).orElseThrow(() -> new AuthenticationException("user is invalid"));

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
            throw new AuthenticationException("Fingerprint does not match");
        }
    }

    @Override
    public void refreshGitAccessToken(final Long userId, final String accessToken, final String refreshToken) {
        User user = getUser(userId);
        user.setAccessToken(accessToken);
        user.setRefreshToken(refreshToken);
        userRepository.save(user);
    }

    private User getEMailForGithub(final User user) {
        User updatedUser = user;
        try {
            String email = gitService.getEmail(updatedUser.getId());
            updatedUser.setEmail(email);
            updatedUser = userRepository.save(updatedUser);
        } catch (NoProviderFoundException | GitException e) {
            throw new RuntimeException(e);
        }
        return updatedUser;
    }
}
