package com.gokalp.psylog_api.service;

import com.gokalp.psylog_api.dto.request.PostPatchRequest;
import com.gokalp.psylog_api.dto.request.PostRequest;
import com.gokalp.psylog_api.dto.response.CommentAdminResponse;
import com.gokalp.psylog_api.dto.response.CommentPublicResponse;
import com.gokalp.psylog_api.dto.response.PagedResponse;
import com.gokalp.psylog_api.dto.response.PostDetailResponse;
import com.gokalp.psylog_api.dto.response.PostSummaryResponse;
import com.gokalp.psylog_api.entity.CommentStatus;
import com.gokalp.psylog_api.entity.Post;
import com.gokalp.psylog_api.exception.ResourceNotFoundException;
import com.gokalp.psylog_api.repository.CommentRepository;
import com.gokalp.psylog_api.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    @Transactional(readOnly = true)
    public PagedResponse<PostSummaryResponse> searchPublishedPosts(String keyword, String tag, int page, int size) {
        String k = (keyword != null && keyword.isBlank()) ? null : keyword;
        String t = (tag != null && tag.isBlank()) ? null : tag;

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Post> posts;
        if (k != null && t != null) {
            posts = postRepository.findPublishedByKeywordAndTag(k, t, pageable);
        } else if (k != null) {
            posts = postRepository.findPublishedByKeyword(k, pageable);
        } else if (t != null) {
            posts = postRepository.findPublishedByTag(t, pageable);
        } else {
            posts = postRepository.findByPublishedTrue(pageable);
        }

        return new PagedResponse<>(posts.map(this::toSummary));
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

    @Transactional(readOnly = true)
    public PostDetailResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + id));
        return toDetail(post, List.of());
    }

    @Transactional(readOnly = true)
    public PagedResponse<PostSummaryResponse> getAllPosts(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return new PagedResponse<>(postRepository.findAll(pageable).map(this::toAdminSummary));
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
        post.setReadingTime(request.getReadingTime());
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
        post.setReadingTime(request.getReadingTime());
        Post saved = postRepository.save(post);
        log.info("Post updated: [id={}] {}", saved.getId(), saved.getTitle());
        return toDetail(saved, List.of());
    }

    @Transactional
    public PostDetailResponse patchPost(Long id, PostPatchRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + id));
        if (request.getTitle() != null) {
            String newTitle = request.getTitle().orElse(null);
            post.setTitle(newTitle);
            post.setSlug(generateUniqueSlug(newTitle != null ? newTitle : post.getTitle(), post.getId()));
        }
        if (request.getSummary() != null) post.setSummary(request.getSummary().orElse(null));
        if (request.getContent() != null) post.setContent(request.getContent().orElse(null));
        if (request.getCoverImage() != null) post.setCoverImage(request.getCoverImage().orElse(null));
        if (request.getTags() != null) post.setTags(request.getTags().orElse(List.of()));
        if (request.getPublished() != null) post.setPublished(request.getPublished().orElse(false));
        if (request.getPublishAt() != null) post.setPublishAt(request.getPublishAt().orElse(null));
        if (request.getReadingTime() != null) post.setReadingTime(request.getReadingTime().orElse(null));
        Post saved = postRepository.save(post);
        log.info("Post patched: [id={}] {}", saved.getId(), saved.getTitle());
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

    private PostSummaryResponse toSummary(Post post) {
        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getSummary(),
                post.getCoverImage(),
                post.getTags(),
                post.isPublished(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getPublishAt(),
                post.getReadingTime(),
                null
        );
    }

    private PostSummaryResponse toAdminSummary(Post post) {
        List<CommentAdminResponse> comments = commentRepository
                .findByPostOrderByCreatedAtAsc(post)
                .stream()
                .map(c -> new CommentAdminResponse(
                        c.getId(), post.getId(), post.getTitle(), post.getSlug(),
                        c.getAuthor(), c.getEmail(), c.getContent(), c.getStatus(), c.getCreatedAt()))
                .toList();
        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getSummary(),
                post.getCoverImage(),
                post.getTags(),
                post.isPublished(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getPublishAt(),
                post.getReadingTime(),
                comments
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
                post.getPublishAt(),
                comments,
                post.getReadingTime()
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
