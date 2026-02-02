package net.pyroneon.NotEnoughBuckets.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;

/**
 * A utility that provides methods to resolve the network address (IP address) of a request.
 */
public class RequestIpAddressResolver {

    /**
     * Returns client IP from which the request was made, even if a proxy is being used.
     * Assumes the application is being run behind Cloudflare.
     * First, the <code>CF-Connecting-IP</code> header is checked.
     * As a fallback, the <code>X-Forwarded-For</code> header is checked.
     * Finally, if none of these headers are set, the remote address (proxy) is returned.
     * @return The client's real IP address.
     */
    @NotNull
    public static String getClientIpAddress(HttpServletRequest request) {
        // Try using Cloudflare Connecting IP header first
        String ipAddress = request.getHeader("CF-Connecting-IP");
        if(ipAddress != null) return ipAddress;
        // Otherwise fall back on XFF header.
        String xForwardedHeader = request.getHeader("X-Forwarded-For");
        if(xForwardedHeader != null) return xForwardedHeader.split(",")[0];
        // If XFF header fails, fallback on the raw remote address.
        return request.getRemoteAddr();
    }
}
