package com.tuwien.gitanalyser.repository;

import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.entity.utils.AuthenticationProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("select distinct u from User u where "
               + "u.authenticationProvider = :authenticationProvider and "
               + "u.platformId = :platformId")
    List<User> findByPlatformAndPlatformId(
        @Param("authenticationProvider") AuthenticationProvider authenticationProvider,
        @Param("platformId") Integer platformId);
}
