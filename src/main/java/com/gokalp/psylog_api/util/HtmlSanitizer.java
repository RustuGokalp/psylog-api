package com.gokalp.psylog_api.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;

import java.nio.charset.StandardCharsets;

/**
 * Allowlist based HTML sanitizer for rich text fields (About.message,
 * Post.summary/content, Specialization.summary/content).
 *
 * <p>This is defense in depth: the frontend already sanitizes at render time,
 * but nothing untrusted should reach the database in the first place. Only the
 * tags the admin editor can produce are kept; everything else (script, style,
 * iframe, on* handlers, javascript:/data: URLs) is dropped.
 */
public final class HtmlSanitizer {

    private static final Safelist SAFELIST = new Safelist()
            .addTags(
                    "h1", "h2", "h3", "p", "strong", "em", "u", "s",
                    "ul", "ol", "li", "blockquote", "code", "pre",
                    "hr", "a", "img", "br"
            )
            .addAttributes("a", "href", "title", "target")
            .addAttributes("img", "src", "alt", "title")
            .addProtocols("a", "href", "http", "https", "mailto")
            .addProtocols("img", "src", "http", "https")
            // Keeps internal links such as /hakkimda intact. The protocol check still
            // runs, so javascript:/vbscript:/data: URLs are dropped either way.
            .preserveRelativeLinks(true);

    /**
     * Placeholder base URI. jsoup resolves each href/src against the base URI before
     * running the protocol check, so with an empty base URI a relative link such as
     * "/hakkimda" resolves to nothing and its href is stripped. Because the safelist
     * sets preserveRelativeLinks, the original relative value — not the resolved one —
     * is what ends up in the output, so this placeholder never leaks into stored HTML.
     */
    private static final String BASE_URI = "http://example.com/";

    private HtmlSanitizer() {
    }

    /**
     * Cleans the given HTML fragment. Returns {@code null} for {@code null}
     * input and leaves blank input untouched.
     */
    public static String sanitize(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }
        // Jsoup.clean(html, baseUri, safelist, outputSettings) is not used here: unlike
        // its three-argument sibling it skips the placeholder-base-URI step and therefore
        // drops relative links. Driving the Cleaner directly keeps both relative links
        // and our output settings.
        Document dirty = Jsoup.parseBodyFragment(html, BASE_URI);
        Document clean = new Cleaner(SAFELIST).clean(dirty);
        clean.outputSettings(outputSettings());
        return clean.body().html();
    }

    // xhtml escape mode escapes only & < > " — Turkish characters (ğüşiöçİĞÜŞÖÇ)
    // stay as-is instead of being turned into HTML entities.
    private static Document.OutputSettings outputSettings() {
        return new Document.OutputSettings()
                .prettyPrint(false)
                .charset(StandardCharsets.UTF_8)
                .escapeMode(Entities.EscapeMode.xhtml);
    }
}
