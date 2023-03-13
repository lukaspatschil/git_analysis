package com.tuwien.gitanalyser.repository;

import com.tuwien.gitanalyser.entity.Repository;
import com.tuwien.gitanalyser.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@org.springframework.stereotype.Repository
public interface RepositoryRepository extends JpaRepository<Repository, Long> {
    Optional<Repository> findByUserAndPlatformId(User user, Long platformId);
}
