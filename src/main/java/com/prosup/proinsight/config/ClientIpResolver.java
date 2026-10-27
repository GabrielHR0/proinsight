package com.prosup.proinsight.config;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resolves the client IP used for rate limiting.
 *
 * <p>Forwarded headers are only trusted when the application is explicitly
 * configured to run behind a trusted proxy. Otherwise a client could spoof
 * {@code X-Forwarded-For} and bypass per-IP limits.</p>
 */
public final class ClientIpResolver {

    private ClientIpResolver() {
    }

    public static String resolve(HttpServletRequest request, boolean trustProxyHeaders) {
        if (trustProxyHeaders) {
            String forwardedFor = request.getHeader("X-Forwarded-For");
            if (forwardedFor != null && !forwardedFor.isBlank()) {
                // The trusted proxy appends the address it observed to the
                // right side of the list. Client-supplied entries on the left
                // must not be used, otherwise the rate limit can be bypassed
                // by spoofing X-Forwarded-For.
                String[] hops = forwardedFor.split(",");
                for (int i = hops.length - 1; i >= 0; i--) {
                    String hop = hops[i].trim();
                    if (!hop.isBlank()) {
                        return hop;
                    }
                }
            }
        }
        return request.getRemoteAddr();
    }
}
