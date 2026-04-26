package com.gokalp.psylog_api.repository;

import com.gokalp.psylog_api.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByPublishedTrue(Pageable pageable);
    Optional<Post> findBySlugAndPublishedTrue(String slug);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Long id);

    @Query("SELECT p FROM Post p " +
           "WHERE p.published = true " +
           "AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(p.summary) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Post> findPublishedByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN p.tags t " +
           "WHERE p.published = true AND t = :tag")
    Page<Post> findPublishedByTag(@Param("tag") String tag, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN p.tags t " +
           "WHERE p.published = true AND t = :tag " +
           "AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(p.summary) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Post> findPublishedByKeywordAndTag(@Param("keyword") String keyword, @Param("tag") String tag, Pageable pageable);

    @Query("SELECT p FROM Post p " +
           "WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "   OR LOWER(p.summary) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Post> findAllByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN p.tags t WHERE t = :tag")
    Page<Post> findAllByTag(@Param("tag") String tag, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN p.tags t " +
           "WHERE t = :tag " +
           "AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(p.summary) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Post> findAllByKeywordAndTag(@Param("keyword") String keyword, @Param("tag") String tag, Pageable pageable);

    @Modifying
    @Query("UPDATE Post p SET p.published = true WHERE p.published = false AND p.publishAt IS NOT NULL AND p.publishAt <= :now")
    int publishScheduledPosts(@Param("now") LocalDateTime now);
}
