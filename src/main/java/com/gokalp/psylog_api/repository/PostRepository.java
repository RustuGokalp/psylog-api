package com.gokalp.psylog_api.repository;

import com.gokalp.psylog_api.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByPublishedTrueOrderByCreatedAtDesc();
    Optional<Post> findBySlugAndPublishedTrue(String slug);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Long id);

    @Modifying
    @Query("UPDATE Post p SET p.published = true WHERE p.published = false AND p.publishAt IS NOT NULL AND p.publishAt <= :now")
    int publishScheduledPosts(@Param("now") LocalDateTime now);
}
