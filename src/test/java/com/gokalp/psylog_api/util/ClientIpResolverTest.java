package com.gokalp.psylog_api.util;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Requests arrive through Cloudflare + the platform proxy, so the real client IP is not
// getRemoteAddr(). Priority: CF-Connecting-IP (unspoofable) > X-Forwarded-For > getRemoteAddr().
class ClientIpResolverTest {

    // ─── CF-Connecting-IP wins ──────────────────────────────────────────────

    @Test
    void prefersCloudflareHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("CF-Connecting-IP", "203.0.113.5");

        assertEquals("203.0.113.5", ClientIpResolver.resolve(request));
    }

    // The client can put anything in X-Forwarded-For, so when the two disagree the
    // Cloudflare header must win — otherwise a bot rotating fake values in
    // X-Forwarded-For would get a fresh rate-limit bucket on every request.
    @Test
    void cloudflareHeaderOverridesSpoofedForwardedFor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("CF-Connecting-IP", "203.0.113.5");
        request.addHeader("X-Forwarded-For", "1.2.3.4, 203.0.113.5, 10.0.0.1");

        assertEquals("203.0.113.5", ClientIpResolver.resolve(request));
    }

    @Test
    void trimsWhitespaceAroundCloudflareHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("CF-Connecting-IP", "  198.51.100.9  ");

        assertEquals("198.51.100.9", ClientIpResolver.resolve(request));
    }

    @Test
    void blankCloudflareHeaderFallsThroughToForwardedFor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("CF-Connecting-IP", "   ");
        request.addHeader("X-Forwarded-For", "203.0.113.7");

        assertEquals("203.0.113.7", ClientIpResolver.resolve(request));
    }

    // ─── X-Forwarded-For fallback (local dev / no Cloudflare) ───────────────

    @Test
    void usesFirstEntryOfForwardedForWhenCloudflareHeaderMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("X-Forwarded-For", "203.0.113.5, 70.41.3.18, 10.0.0.1");

        assertEquals("203.0.113.5", ClientIpResolver.resolve(request));
    }

    @Test
    void trimsWhitespaceAroundSingleForwardedValue() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("X-Forwarded-For", "  198.51.100.7  ");

        assertEquals("198.51.100.7", ClientIpResolver.resolve(request));
    }

    // ─── getRemoteAddr fallback ─────────────────────────────────────────────

    @Test
    void fallsBackToRemoteAddrWhenNoHeaderPresent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.50");

        assertEquals("192.168.1.50", ClientIpResolver.resolve(request));
    }

    @Test
    void fallsBackToRemoteAddrWhenBothHeadersBlank() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.51");
        request.addHeader("CF-Connecting-IP", "  ");
        request.addHeader("X-Forwarded-For", "   ");

        assertEquals("192.168.1.51", ClientIpResolver.resolve(request));
    }
}
