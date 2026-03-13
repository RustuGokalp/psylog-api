package com.gokalp.psylog_api.repository;

import com.gokalp.psylog_api.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByPublishedTrueOrderByCreatedAtDesc();
    Optional<Post> findBySlugAndPublishedTrue(String slug);
}
