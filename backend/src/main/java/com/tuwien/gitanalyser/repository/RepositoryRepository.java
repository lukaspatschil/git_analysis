package com.tuwien.gitanalyser.repository;

import com.tuwien.gitanalyser.entity.SavedRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@org.springframework.stereotype.Repository
public interface RepositoryRepository extends JpaRepository<SavedRepository, Long> {

    @Query("SELECT r FROM SavedRepository r WHERE r.user.id = :userId AND r.platformId = :platformId")
    SavedRepository findByUserIdAndPlatformId(@Param("userId") Long userId, @Param("platformId") Long platformId);


}
