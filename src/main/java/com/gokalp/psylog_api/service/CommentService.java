package com.gokalp.psylog_api.service;

import com.gokalp.psylog_api.dto.request.CommentRequest;
import com.gokalp.psylog_api.dto.response.CommentAdminResponse;
import com.gokalp.psylog_api.dto.response.CommentPublicResponse;
import com.gokalp.psylog_api.dto.response.PagedResponse;
import com.gokalp.psylog_api.entity.Comment;
import com.gokalp.psylog_api.entity.CommentStatus;
import com.gokalp.psylog_api.entity.Post;
import com.gokalp.psylog_api.exception.ResourceNotFoundException;
import com.gokalp.psylog_api.repository.CommentRepository;
import com.gokalp.psylog_api.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final EmailService emailService;

    public CommentService(CommentRepository commentRepository,
                          PostRepository postRepository,
                          EmailService emailService) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.emailService = emailService;
    }

    @Transactional
    public CommentPublicResponse submitComment(Long postId, CommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(request.getAuthor());
        comment.setEmail(request.getEmail());
        comment.setContent(request.getContent());

        Comment saved = commentRepository.save(comment);
        log.info("Comment submitted: [id={}, postId={}]", saved.getId(), postId);

        emailService.sendCommentNotification(saved);

        return toPublicResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<CommentAdminResponse> getPendingComments(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        return new PagedResponse<>(commentRepository.findByStatus(CommentStatus.PENDING, pageable)
                .map(this::toAdminResponse));
    }

    @Transactional(readOnly = true)
    public List<CommentAdminResponse> getAllCommentsByPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));

        return commentRepository.findByPostOrderByCreatedAtAsc(post)
                .stream()
                .map(this::toAdminResponse)
                .toList();
    }

    @Transactional
    public CommentAdminResponse approveComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));

        comment.setStatus(CommentStatus.APPROVED);
        Comment saved = commentRepository.save(comment);
        log.info("Comment approved: [id={}]", commentId);

        return toAdminResponse(saved);
    }

    @Transactional
    public CommentAdminResponse rejectComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));

        comment.setStatus(CommentStatus.REJECTED);
        Comment saved = commentRepository.save(comment);
        log.info("Comment rejected: [id={}]", commentId);

        return toAdminResponse(saved);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));

        commentRepository.delete(comment);
        log.info("Comment deleted: [id={}]", commentId);
    }

    private CommentPublicResponse toPublicResponse(Comment comment) {
        return new CommentPublicResponse(
                comment.getId(),
                comment.getAuthor(),
                comment.getEmail(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }

    private CommentAdminResponse toAdminResponse(Comment comment) {
        Post post = comment.getPost();
        return new CommentAdminResponse(
                comment.getId(),
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                comment.getAuthor(),
                comment.getEmail(),
                comment.getContent(),
                comment.getStatus(),
                comment.getCreatedAt()
        );
    }
}
