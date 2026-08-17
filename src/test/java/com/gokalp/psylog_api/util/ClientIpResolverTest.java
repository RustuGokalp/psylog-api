package com.gokalp.psylog_api.util;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Requests arrive through Cloudflare + the platform proxy, so the real client IP
// lives in X-Forwarded-For; getRemoteAddr() would be the proxy.
class ClientIpResolverTest {

    @Test
    void usesFirstEntryOfForwardedForHeader() {
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

    @Test
    void fallsBackToRemoteAddrWhenHeaderMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.50");

        assertEquals("192.168.1.50", ClientIpResolver.resolve(request));
    }

    @Test
    void fallsBackToRemoteAddrWhenHeaderBlank() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.51");
        request.addHeader("X-Forwarded-For", "   ");

        assertEquals("192.168.1.51", ClientIpResolver.resolve(request));
    }
}
