package com.gokalp.psylog_api.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resolves the real client IP behind Cloudflare + the hosting platform's proxy.
 * getRemoteAddr() alone would return the proxy address, which would put every
 * visitor into the same rate-limit bucket.
 *
 * <p>Header priority matters for security:
 *
 * <ol>
 *   <li><b>CF-Connecting-IP</b> — Cloudflare writes this itself on every request and
 *       overwrites whatever the client sent, so it cannot be spoofed. Our traffic goes
 *       through Cloudflare, so this is the trustworthy source.
 *   <li><b>X-Forwarded-For</b> (first entry) — only a fallback for local development and
 *       setups without Cloudflare. On its own it is NOT trustworthy: the client can send
 *       this header itself, and proxies only append what they see to the END of the list.
 *       The first entry therefore stays whatever the caller invented, so a bot writing a
 *       random fake IP on every request would get a fresh rate-limit bucket each time and
 *       bypass the limit completely.
 *   <li><b>getRemoteAddr()</b> — last resort.
 * </ol>
 */
public final class ClientIpResolver {

    private ClientIpResolver() {}

    public static String resolve(HttpServletRequest request) {
        // Set by Cloudflare, unspoofable — always preferred when present.
        String cloudflareIp = firstNonBlank(request.getHeader("CF-Connecting-IP"));
        if (cloudflareIp != null) {
            return cloudflareIp;
        }

        // Fallback only: client-controllable, see the class comment.
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            // Format: "client, proxy1, proxy2" — the first entry is the original client
            String first = firstNonBlank(forwardedFor.split(",")[0]);
            if (first != null) {
                return first;
            }
        }

        String remoteAddr = request.getRemoteAddr();
        return remoteAddr != null ? remoteAddr : "unknown";
    }

    private static String firstNonBlank(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
