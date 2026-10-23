package com.anush.hamrospace.core.security.util;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class ClientMetaDataUtils {

    public String extractIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");

        if (ipAddress == null || ipAddress.isBlank() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isBlank() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isBlank() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        // If there is a chain of multiple proxy IPs (e.g. Client -> Cloudflare ->
        // Nginx),
        // the first IP in the comma-separated list is always the original client.
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        return ipAddress;
    }

    public String extractUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return (userAgent != null && !userAgent.isBlank()) ? userAgent : "Unknown Device";
    }

    public String extractDeviceInfo(HttpServletRequest request) {
        String customHeader = request.getHeader("X-Device-Info");
        if (customHeader != null && !customHeader.isBlank()) {
            return customHeader;
        }

        String userAgent = extractUserAgent(request);

        String ua = userAgent.toLowerCase();
        if (ua.contains("windows"))
            return "Windows Desktop";
        if (ua.contains("macintosh"))
            return "macOS Desktop";
        if (ua.contains("iphone"))
            return "iPhone Mobile";
        if (ua.contains("android"))
            return "Android Mobile";
        if (ua.contains("linux"))
            return "Linux Desktop";
        if (ua.contains("postman"))
            return "Postman API Client";
        if (ua.contains("bruno"))
            return "Bruno API Client";

        return "Web Browser";
    }
}
