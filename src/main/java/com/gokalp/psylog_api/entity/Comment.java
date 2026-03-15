package com.gokalp.psylog_api.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Post post;

    @Column(nullable = false)
    private String author;

    private String email;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommentStatus status = CommentStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now(ZoneId.of("Europe/Istanbul"));
    }

    public Comment() {}

    public Long getId() { return id; }
    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public CommentStatus getStatus() { return status; }
    public void setStatus(CommentStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
