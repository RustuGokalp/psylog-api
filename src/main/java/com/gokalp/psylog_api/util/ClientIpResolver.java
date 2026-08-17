package com.gokalp.psylog_api.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resolves the real client IP behind Cloudflare + the hosting platform's proxy.
 * getRemoteAddr() would return the proxy address, which would put every visitor
 * into the same rate-limit bucket.
 */
public final class ClientIpResolver {

    private ClientIpResolver() {}

    public static String resolve(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            // Format: "client, proxy1, proxy2" — the first entry is the original client
            String first = forwardedFor.split(",")[0].trim();
            if (!first.isEmpty()) {
                return first;
            }
        }
        String remoteAddr = request.getRemoteAddr();
        return remoteAddr != null ? remoteAddr : "unknown";
    }
}
