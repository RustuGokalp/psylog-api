package com.gokalp.psylog_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommentRequest {

    @NotBlank(message = "Yazar adı boş olamaz")
    private String author;

    @Email(message = "Email formatı hatalı")
    private String email;

    @NotBlank(message = "Yorum içeriği boş olamaz")
    @Size(max = 1000, message = "Yorum en fazla 1000 karakter olabilir")
    private String content;

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
