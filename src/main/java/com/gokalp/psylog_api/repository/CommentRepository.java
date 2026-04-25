package com.gokalp.psylog_api.repository;

import com.gokalp.psylog_api.entity.Comment;
import com.gokalp.psylog_api.entity.CommentStatus;
import com.gokalp.psylog_api.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostAndStatusOrderByCreatedAtAsc(Post post, CommentStatus status);
    List<Comment> findByPostOrderByCreatedAtAsc(Post post);
    Page<Comment> findByStatus(CommentStatus status, Pageable pageable);
}
