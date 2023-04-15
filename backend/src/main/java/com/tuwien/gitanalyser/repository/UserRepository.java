package com.tuwien.gitanalyser.repository;

import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.entity.utils.AuthenticationProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByAuthenticationProviderAndPlatformId(AuthenticationProvider authenticationProvider,
                                                             Integer platformId);
}
