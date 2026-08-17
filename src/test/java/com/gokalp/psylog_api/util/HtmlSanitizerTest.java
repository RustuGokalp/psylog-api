package com.gokalp.psylog_api.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Unit tests for HtmlSanitizer — allowlist behaviour for rich text fields
class HtmlSanitizerTest {

    // ─── Dangerous content is dropped ───────────────────────────────────────

    @Test
    void shouldRemoveScriptTag() {
        String result = HtmlSanitizer.sanitize("<p>Merhaba</p><script>alert('xss')</script>");
        assertEquals("<p>Merhaba</p>", result);
    }

    @Test
    void shouldRemoveStyleAndIframeTags() {
        String result = HtmlSanitizer.sanitize(
                "<style>body{display:none}</style><p>Metin</p><iframe src=\"https://evil.com\"></iframe>");
        assertEquals("<p>Metin</p>", result);
    }

    @Test
    void shouldRemoveOnErrorHandler() {
        String result = HtmlSanitizer.sanitize("<img src=\"https://example.com/a.png\" onerror=\"alert(1)\">");
        assertFalse(result.contains("onerror"));
        assertTrue(result.contains("src=\"https://example.com/a.png\""));
    }

    @Test
    void shouldRemoveOnClickHandler() {
        String result = HtmlSanitizer.sanitize("<p onclick=\"steal()\">Metin</p>");
        assertEquals("<p>Metin</p>", result);
    }

    @Test
    void shouldDropJavascriptProtocolHref() {
        String result = HtmlSanitizer.sanitize("<a href=\"javascript:alert(1)\">tikla</a>");
        assertFalse(result.contains("javascript"));
        assertEquals("<a>tikla</a>", result);
    }

    @Test
    void shouldDropObfuscatedJavascriptProtocolHref() {
        assertEquals("<a>a</a>", HtmlSanitizer.sanitize("<a href=\"JaVaScRiPt:alert(1)\">a</a>"));
        assertEquals("<a>b</a>", HtmlSanitizer.sanitize("<a href=\"  javascript:alert(1)\">b</a>"));
        assertEquals("<a>c</a>", HtmlSanitizer.sanitize("<a href=\"&#106;avascript:alert(1)\">c</a>"));
        assertEquals("<a>d</a>", HtmlSanitizer.sanitize("<a href=\"vbscript:msgbox(1)\">d</a>"));
    }

    @Test
    void shouldDropDataProtocolImageSource() {
        String result = HtmlSanitizer.sanitize(
                "<img src=\"data:text/html;base64,PHNjcmlwdD5hbGVydCgxKTwvc2NyaXB0Pg==\">");
        assertFalse(result.contains("data:"));
    }

    // ─── Allowed content is preserved ───────────────────────────────────────

    @Test
    void shouldKeepAllowedEditorTags() {
        String html = "<h1>Baslik</h1><h2>Alt</h2><h3>Alt2</h3>"
                + "<p><strong>kalin</strong> <em>italik</em> <u>alti</u> <s>ustu</s></p>"
                + "<ul><li>bir</li></ul><ol><li>iki</li></ol>"
                + "<blockquote>alinti</blockquote><pre><code>kod</code></pre>"
                + "<hr><br>";
        assertEquals(html, HtmlSanitizer.sanitize(html));
    }

    @Test
    void shouldKeepSafeLinksAndImages() {
        String html = "<p><a href=\"https://example.com\" title=\"site\">baglanti</a>"
                + "<a href=\"mailto:info@example.com\">eposta</a>"
                + "<img src=\"https://example.com/a.png\" alt=\"resim\" title=\"baslik\"></p>";
        assertEquals(html, HtmlSanitizer.sanitize(html));
    }

    @Test
    void shouldKeepRelativeUrls() {
        String html = "<p><a href=\"/hakkimda\">hakkimda</a></p>";
        assertEquals(html, HtmlSanitizer.sanitize(html));
    }

    @Test
    void shouldNotEscapeTurkishCharacters() {
        String html = "<p>Çocuk ve ergen psikolojisi: ğüşiöçİĞÜŞÖÇ</p>";
        String result = HtmlSanitizer.sanitize(html);
        assertEquals(html, result);
        assertFalse(result.contains("&#"));
        assertFalse(result.contains("&U"));
    }

    @Test
    void shouldKeepPlainTextUnchanged() {
        String plain = "Kısa bir özet metni, HTML içermiyor.";
        assertEquals(plain, HtmlSanitizer.sanitize(plain));
    }

    // ─── Edge cases ─────────────────────────────────────────────────────────

    @Test
    void shouldReturnNullForNullInput() {
        assertNull(HtmlSanitizer.sanitize(null));
    }

    @Test
    void shouldReturnEmptyStringForEmptyInput() {
        assertEquals("", HtmlSanitizer.sanitize(""));
    }
}
