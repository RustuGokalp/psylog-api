package com.gokalp.psylog_api.service;

import com.gokalp.psylog_api.entity.Comment;
import com.gokalp.psylog_api.entity.ContactMessage;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final JavaMailSender mailSender;
    private final String from;
    private final String notificationEmail;

    // Gmail quota safety net. Field injection keeps the constructor stable for unit tests;
    // the initializer is the fallback used outside a Spring context.
    @Value("${app.mail.daily-limit:50}")
    private int dailyLimit = 50;

    private LocalDate quotaDate = LocalDate.now();
    private int sentToday = 0;

    public EmailService(JavaMailSender mailSender,
                        @Value("${spring.mail.username}") String from,
                        @Value("${app.notification.email}") String notificationEmail) {
        this.mailSender = mailSender;
        this.from = from;
        this.notificationEmail = notificationEmail;
    }

    /**
     * Consumes one slot of the daily notification budget. Returns false when the budget
     * is used up — the caller then skips sending; the record itself is already persisted.
     */
    private synchronized boolean consumeDailyQuota() {
        LocalDate today = LocalDate.now();
        if (!today.equals(quotaDate)) {
            quotaDate = today;
            sentToday = 0;
        }
        if (sentToday >= dailyLimit) {
            return false;
        }
        sentToday++;
        return true;
    }

    @Async
    public void sendContactNotification(ContactMessage msg) {
        if (!consumeDailyQuota()) {
            log.warn("Günlük mail limiti ({}) doldu — iletişim bildirimi gönderilmedi, kayıt veritabanında mevcut", dailyLimit);
            return;
        }
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, "UTF-8");
            helper.setFrom(from);
            helper.setTo(notificationEmail);
            helper.setSubject(msg.getSubject());
            helper.setText(buildHtmlBody(msg), true);
            mailSender.send(mime);
            log.info("Contact notification email sent for: {}", msg.getEmail());
        } catch (MessagingException | MailException e) {
            log.error("Failed to send contact notification email: {}", e.getMessage());
        }
    }

    @Async
    public void sendCommentNotification(Comment comment) {
        if (!consumeDailyQuota()) {
            log.warn("Günlük mail limiti ({}) doldu — yorum bildirimi gönderilmedi, kayıt veritabanında mevcut", dailyLimit);
            return;
        }
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, "UTF-8");
            helper.setFrom(from);
            helper.setTo(notificationEmail);
            helper.setSubject("Yeni yorum: " + comment.getPost().getTitle());
            helper.setText(buildCommentHtmlBody(comment), true);
            mailSender.send(mime);
            log.info("Comment notification email sent for comment id: {}", comment.getId());
        } catch (MessagingException | MailException e) {
            log.error("Failed to send comment notification email: {}", e.getMessage());
        }
    }

    private String buildCommentHtmlBody(Comment comment) {
        String emailValue = comment.getEmail() != null
                ? "<a href='mailto:" + escapeHtml(comment.getEmail())
                  + "' style='color:#1d4ed8;text-decoration:none;'>"
                  + escapeHtml(comment.getEmail()) + "</a>"
                : "-";
        String emailRow = "<tr><td style='color:#6b7280;padding:4px 0;'>E-posta</td>"
                + "<td style='padding:4px 0 4px 16px;'>" + emailValue + "</td></tr>";

        return """
                <!DOCTYPE html>
                <html lang="tr">
                <body style="margin:0;padding:0;background:#f3f4f6;font-family:Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="padding:32px 0;">
                    <tr><td align="center">
                      <table width="560" cellpadding="0" cellspacing="0"
                             style="background:#ffffff;border-radius:8px;overflow:hidden;
                                    box-shadow:0 1px 3px rgba(0,0,0,.1);">

                        <!-- Header -->
                        <tr>
                          <td style="background:#1d4ed8;padding:24px 32px;">
                            <p style="margin:0;font-size:12px;color:#93c5fd;text-transform:uppercase;
                                      letter-spacing:.08em;">Yeni Yorum</p>
                            <h1 style="margin:4px 0 0;font-size:20px;color:#ffffff;">%s</h1>
                          </td>
                        </tr>

                        <!-- Comment body -->
                        <tr>
                          <td style="padding:32px;">
                            <p style="margin:0;font-size:15px;color:#111827;line-height:1.7;
                                      white-space:pre-wrap;">%s</p>
                          </td>
                        </tr>

                        <!-- Divider -->
                        <tr>
                          <td style="padding:0 32px;">
                            <hr style="border:none;border-top:1px solid #e5e7eb;margin:0;">
                          </td>
                        </tr>

                        <!-- Signature -->
                        <tr>
                          <td style="padding:24px 32px 32px;">
                            <p style="margin:0 0 12px;font-size:11px;color:#9ca3af;
                                      text-transform:uppercase;letter-spacing:.07em;">Yorum Yapan</p>
                            <table cellpadding="0" cellspacing="0" style="font-size:14px;color:#111827;">
                              <tr>
                                <td style="color:#6b7280;padding:4px 0;">Ad</td>
                                <td style="padding:4px 0 4px 16px;font-weight:600;">%s</td>
                              </tr>
                              %s
                              <tr>
                                <td style="color:#6b7280;padding:4px 0;">Yazı</td>
                                <td style="padding:4px 0 4px 16px;">%s</td>
                              </tr>
                              <tr>
                                <td style="color:#6b7280;padding:4px 0;">Tarih</td>
                                <td style="padding:4px 0 4px 16px;">%s</td>
                              </tr>
                            </table>
                          </td>
                        </tr>

                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(
                escapeHtml(comment.getPost().getTitle()),
                escapeHtml(comment.getContent()),
                escapeHtml(comment.getAuthor()),
                emailRow,
                escapeHtml(comment.getPost().getTitle()),
                FORMATTER.format(comment.getCreatedAt())
        );
    }

    private String buildHtmlBody(ContactMessage msg) {
        String phone = msg.getMobilePhone() != null
                ? "<tr><td style='color:#6b7280;padding:4px 0;'>Telefon</td>"
                  + "<td style='padding:4px 0 4px 16px;font-weight:500;'>" + escapeHtml(msg.getMobilePhone()) + "</td></tr>"
                : "";

        return """
                <!DOCTYPE html>
                <html lang="tr">
                <body style="margin:0;padding:0;background:#f3f4f6;font-family:Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="padding:32px 0;">
                    <tr><td align="center">
                      <table width="560" cellpadding="0" cellspacing="0"
                             style="background:#ffffff;border-radius:8px;overflow:hidden;
                                    box-shadow:0 1px 3px rgba(0,0,0,.1);">

                        <!-- Header -->
                        <tr>
                          <td style="background:#1d4ed8;padding:24px 32px;">
                            <p style="margin:0;font-size:12px;color:#93c5fd;text-transform:uppercase;
                                      letter-spacing:.08em;">Yeni İletişim Mesajı</p>
                            <h1 style="margin:4px 0 0;font-size:20px;color:#ffffff;">%s</h1>
                          </td>
                        </tr>

                        <!-- Message body -->
                        <tr>
                          <td style="padding:32px;">
                            <p style="margin:0;font-size:15px;color:#111827;line-height:1.7;
                                      white-space:pre-wrap;">%s</p>
                          </td>
                        </tr>

                        <!-- Divider -->
                        <tr>
                          <td style="padding:0 32px;">
                            <hr style="border:none;border-top:1px solid #e5e7eb;margin:0;">
                          </td>
                        </tr>

                        <!-- Signature -->
                        <tr>
                          <td style="padding:24px 32px 32px;">
                            <p style="margin:0 0 12px;font-size:11px;color:#9ca3af;
                                      text-transform:uppercase;letter-spacing:.07em;">Gönderen</p>
                            <table cellpadding="0" cellspacing="0" style="font-size:14px;color:#111827;">
                              <tr>
                                <td style="color:#6b7280;padding:4px 0;">Ad Soyad</td>
                                <td style="padding:4px 0 4px 16px;font-weight:600;">%s</td>
                              </tr>
                              <tr>
                                <td style="color:#6b7280;padding:4px 0;">E-posta</td>
                                <td style="padding:4px 0 4px 16px;">
                                  <a href="mailto:%s" style="color:#1d4ed8;text-decoration:none;">%s</a>
                                </td>
                              </tr>
                              %s
                              <tr>
                                <td style="color:#6b7280;padding:4px 0;">Tarih</td>
                                <td style="padding:4px 0 4px 16px;">%s</td>
                              </tr>
                            </table>
                          </td>
                        </tr>

                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(
                escapeHtml(msg.getSubject()),
                escapeHtml(msg.getMessage()),
                escapeHtml(msg.getFullName()),
                escapeHtml(msg.getEmail()),
                escapeHtml(msg.getEmail()),
                phone,
                FORMATTER.format(msg.getCreatedAt())
        );
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
