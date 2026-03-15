package com.gokalp.psylog_api.service;

import com.gokalp.psylog_api.dto.request.PostRequest;
import com.gokalp.psylog_api.dto.response.CommentPublicResponse;
import com.gokalp.psylog_api.dto.response.PostDetailResponse;
import com.gokalp.psylog_api.dto.response.PostSummaryResponse;
import com.gokalp.psylog_api.entity.CommentStatus;
import com.gokalp.psylog_api.entity.Post;
import com.gokalp.psylog_api.exception.ResourceNotFoundException;
import com.gokalp.psylog_api.repository.CommentRepository;
import com.gokalp.psylog_api.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;

@Service
public class PostService {

    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public PostService(PostRepository postRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    // Public

    @Transactional(readOnly = true)
    public List<PostSummaryResponse> getPublishedPosts() {
        return postRepository.findByPublishedTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public PostDetailResponse getPublishedPostBySlug(String slug) {
        Post post = postRepository.findBySlugAndPublishedTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + slug));
        List<CommentPublicResponse> comments = commentRepository
                .findByPostAndStatusOrderByCreatedAtAsc(post, CommentStatus.APPROVED)
                .stream()
                .map(c -> new CommentPublicResponse(c.getId(), c.getAuthor(), c.getContent(), c.getCreatedAt()))
                .toList();
        return toDetail(post, comments);
    }

    // Admin

    @Transactional(readOnly = true)
    public List<PostSummaryResponse> getAllPosts() {
        return postRepository.findAll()
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional
    public PostDetailResponse createPost(PostRequest request) {
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setSlug(generateUniqueSlug(request.getTitle(), null));
        post.setSummary(request.getSummary());
        post.setContent(request.getContent());
        post.setCoverImage(request.getCoverImage());
        post.setTags(request.getTags() != null ? request.getTags() : List.of());
        post.setPublished(request.getPublished());
        post.setPublishAt(request.getPublishAt());
        Post saved = postRepository.save(post);
        log.info("Post created: [id={}] {}", saved.getId(), saved.getTitle());
        return toDetail(saved, List.of());
    }

    @Transactional
    public PostDetailResponse updatePost(Long id, PostRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + id));
        post.setTitle(request.getTitle());
        post.setSlug(generateUniqueSlug(request.getTitle(), post.getId()));
        post.setSummary(request.getSummary());
        post.setContent(request.getContent());
        post.setCoverImage(request.getCoverImage());
        post.setTags(request.getTags() != null ? request.getTags() : List.of());
        post.setPublished(request.getPublished());
        post.setPublishAt(request.getPublishAt());
        Post saved = postRepository.save(post);
        log.info("Post updated: [id={}] {}", saved.getId(), saved.getTitle());
        return toDetail(saved, List.of());
    }

    @Transactional
    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new ResourceNotFoundException("Post not found: " + id);
        }
        postRepository.deleteById(id);
        log.info("Post deleted: [id={}]", id);
    }

    // Mappers

    private PostSummaryResponse toSummary(Post post) {
        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getSummary(),
                post.getCoverImage(),
                post.getTags(),
                post.isPublished(),
                post.getCreatedAt()
        );
    }

    private PostDetailResponse toDetail(Post post, List<CommentPublicResponse> comments) {
        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getSummary(),
                post.getContent(),
                post.getCoverImage(),
                post.getTags(),
                post.isPublished(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                comments
        );
    }

    private String generateUniqueSlug(String title, Long excludeId) {
        String turkishReplaced = title
                .replace('ı', 'i').replace('İ', 'i')
                .replace('ğ', 'g').replace('Ğ', 'g')
                .replace('ü', 'u').replace('Ü', 'u')
                .replace('ş', 's').replace('Ş', 's')
                .replace('ö', 'o').replace('Ö', 'o')
                .replace('ç', 'c').replace('Ç', 'c');
        String normalized = Normalizer.normalize(turkishReplaced, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
        String baseSlug = normalized
                .toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");

        String slug = baseSlug;
        int counter = 2;
        while (isSlugTaken(slug, excludeId)) {
            slug = baseSlug + "-" + counter++;
        }
        return slug;
    }

    private boolean isSlugTaken(String slug, Long excludeId) {
        if (excludeId == null) {
            return postRepository.existsBySlug(slug);
        }
        return postRepository.existsBySlugAndIdNot(slug, excludeId);
    }
}
