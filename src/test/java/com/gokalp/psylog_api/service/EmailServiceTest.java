package com.gokalp.psylog_api.service;

import com.gokalp.psylog_api.entity.Comment;
import com.gokalp.psylog_api.entity.ContactMessage;
import com.gokalp.psylog_api.entity.Post;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Unit tests for EmailService — verifies notification building/sending in isolation.
// JavaMailSender is mocked; we capture the built MimeMessage and assert on its
// recipients, subject, and HTML body (including HTML-escaping for XSS safety).
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    private static final String FROM = "noreply@psylog.test";
    private static final String NOTIFICATION_EMAIL = "admin@psylog.test";

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setup() {
        emailService = new EmailService(mailSender, FROM, NOTIFICATION_EMAIL);
        // Real MimeMessage so the helper can populate it; needs a (null) Session.
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((jakarta.mail.Session) null));
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private Comment buildComment(Long id, String author, String email, String content, String postTitle) {
        Post post = new Post();
        post.setTitle(postTitle);

        Comment comment = new Comment();
        ReflectionTestUtils.setField(comment, "id", id);
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setEmail(email);
        comment.setContent(content);
        ReflectionTestUtils.setField(comment, "createdAt", LocalDateTime.of(2026, 5, 23, 14, 30));
        return comment;
    }

    /**
     * Returns the decoded HTML body of the sent message. getContent() reverses any
     * transfer-encoding (quoted-printable / charset), so substring assertions are
     * reliable — unlike writeTo(), which would wrap and encode the raw MIME stream.
     */
    private String sentBody() throws Exception {
        return captureSentMessage().getContent().toString();
    }

    private MimeMessage captureSentMessage() {
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        return captor.getValue();
    }

    // ─── sendCommentNotification ──────────────────────────────────────────────

    // Sends to the configured notification address, from the configured sender,
    // with a subject carrying the post title.
    @Test
    void sendCommentNotification_setsRecipientSenderAndSubject() throws Exception {
        Comment comment = buildComment(1L, "Ayşe", "ayse@example.com", "Harika bir yazı.", "Anksiyete Üzerine");

        emailService.sendCommentNotification(comment);

        MimeMessage sent = captureSentMessage();
        assertEquals(FROM, sent.getFrom()[0].toString());
        assertEquals(NOTIFICATION_EMAIL, sent.getAllRecipients()[0].toString());
        assertTrue(sent.getSubject().contains("Anksiyete Üzerine"));
    }

    // Body carries the author name, email (as a mailto link) and the comment text.
    @Test
    void sendCommentNotification_withEmail_includesAuthorEmailAndContent() throws Exception {
        Comment comment = buildComment(1L, "Mehmet", "mehmet@example.com", "Çok faydalı oldu.", "Depresyon");

        emailService.sendCommentNotification(comment);

        String body = sentBody();
        assertTrue(body.contains("Mehmet"), "author should be present");
        assertTrue(body.contains("mehmet@example.com"), "email should be present");
        assertTrue(body.contains("mailto:mehmet@example.com"), "email should be a mailto link");
        assertTrue(body.contains("Çok faydalı oldu."), "content should be present");
    }

    // When the commenter left no email, the field still renders, showing a dash.
    @Test
    void sendCommentNotification_withoutEmail_rendersDashPlaceholder() throws Exception {
        Comment comment = buildComment(1L, "Anonim", null, "Teşekkürler.", "Stres Yönetimi");

        emailService.sendCommentNotification(comment);

        String body = sentBody();
        assertTrue(body.contains("E-posta"), "email label should still be rendered");
        assertFalse(body.contains("mailto:"), "no mailto link without an email");
        assertTrue(body.contains(">-<"), "missing email should render as a dash");
    }

    // User-supplied fields must be HTML-escaped to prevent injection in the email.
    @Test
    void sendCommentNotification_escapesHtmlInUserInput() throws Exception {
        Comment comment = buildComment(
                1L,
                "<script>alert(1)</script>",
                "x@example.com",
                "<b>bold</b> & \"quoted\"",
                "Post");

        emailService.sendCommentNotification(comment);

        String body = sentBody();
        assertFalse(body.contains("<script>alert(1)</script>"), "raw script tag must not appear");
        assertTrue(body.contains("&lt;script&gt;"), "script tag should be escaped");
        assertTrue(body.contains("&amp;"), "ampersand should be escaped");
    }

    // A mail backend failure must be swallowed (logged), never propagated to the caller,
    // so a comment submission is not rolled back by a flaky SMTP server.
    @Test
    void sendCommentNotification_whenSendFails_doesNotThrow() {
        Comment comment = buildComment(1L, "Ayşe", "ayse@example.com", "Yorum", "Post");
        doThrow(new org.springframework.mail.MailSendException("smtp down"))
                .when(mailSender).send(any(MimeMessage.class));

        assertDoesNotThrow(() -> emailService.sendCommentNotification(comment));
    }

    // ─── sendContactNotification (existing behaviour, regression guard) ─────────

    @Test
    void sendContactNotification_setsRecipientSubjectAndBody() throws Exception {
        ContactMessage msg = new ContactMessage();
        msg.setFullName("Zeynep");
        msg.setEmail("zeynep@example.com");
        msg.setSubject("Randevu Talebi");
        msg.setMessage("Görüşmek isterim.");
        ReflectionTestUtils.setField(msg, "createdAt", LocalDateTime.of(2026, 5, 23, 9, 0));

        emailService.sendContactNotification(msg);

        MimeMessage sent = captureSentMessage();
        assertEquals(NOTIFICATION_EMAIL, sent.getAllRecipients()[0].toString());
        assertEquals("Randevu Talebi", sent.getSubject());
        String body = sentBody();
        assertTrue(body.contains("Zeynep"));
        assertTrue(body.contains("Görüşmek isterim."));
    }

    // ─── Daily quota (Gmail safety net) ───────────────────────────────────────

    // Once the daily budget is used up no further mail leaves the server. The caller
    // is not told about it — the record is already persisted by the calling service.
    @Test
    void dailyQuota_whenExhausted_stopsSendingMail() {
        ReflectionTestUtils.setField(emailService, "dailyLimit", 2);

        emailService.sendCommentNotification(buildComment(1L, "A", "a@example.com", "1", "Post"));
        emailService.sendCommentNotification(buildComment(2L, "B", "b@example.com", "2", "Post"));
        emailService.sendCommentNotification(buildComment(3L, "C", "c@example.com", "3", "Post"));

        verify(mailSender, times(2)).send(any(MimeMessage.class));
    }

    // The budget is shared by both notification types — it guards one Gmail account.
    @Test
    void dailyQuota_isSharedBetweenContactAndCommentNotifications() {
        ReflectionTestUtils.setField(emailService, "dailyLimit", 1);

        ContactMessage msg = new ContactMessage();
        msg.setFullName("Zeynep");
        msg.setEmail("zeynep@example.com");
        msg.setSubject("Konu");
        msg.setMessage("Mesaj");
        ReflectionTestUtils.setField(msg, "createdAt", LocalDateTime.of(2026, 5, 23, 9, 0));

        emailService.sendContactNotification(msg);
        emailService.sendCommentNotification(buildComment(1L, "A", "a@example.com", "içerik", "Post"));

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    // A new day resets the counter.
    @Test
    void dailyQuota_resetsWhenTheDayChanges() {
        ReflectionTestUtils.setField(emailService, "dailyLimit", 1);

        emailService.sendCommentNotification(buildComment(1L, "A", "a@example.com", "1", "Post"));
        // Pretend the last send happened yesterday.
        ReflectionTestUtils.setField(emailService, "quotaDate", LocalDate.now().minusDays(1));
        emailService.sendCommentNotification(buildComment(2L, "B", "b@example.com", "2", "Post"));

        verify(mailSender, times(2)).send(any(MimeMessage.class));
    }
}
