package com.core.webview.security;

import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 高级安全管理器
 * 提供全面的WebView安全防护功能
 */
public class AdvancedSecurityManager {
    private static final String TAG = "AdvancedSecurity";

    // 安全配置
    private final Set<String> allowedDomains = new HashSet<>();
    private final Set<String> blockedDomains = new HashSet<>();
    private final Set<Pattern> blockedUrlPatterns = new HashSet<>();
    private final Set<String> allowedSchemes = new HashSet<>();

    private boolean strictDomainChecking = false;
    private boolean enableXSSProtection = true;
    private boolean enableContentFiltering = true;
    private boolean enableSSLVerification = true;

    public AdvancedSecurityManager() {
        // 初始化默认的安全配置
        initializeDefaultSecurity();
    }

    private void initializeDefaultSecurity() {
        // 默认允许的scheme
        allowedSchemes.add("http");
        allowedSchemes.add("https");
        allowedSchemes.add("file");
        allowedSchemes.add("data");
        allowedSchemes.add("blob");

        // 默认阻止的URL模式（XSS相关）
        addBlockedUrlPattern("javascript:");
        addBlockedUrlPattern("data:text/html");
        addBlockedUrlPattern("vbscript:");
    }

    /**
     * 添加允许的域名
     */
    public AdvancedSecurityManager addAllowedDomain(@NonNull String domain) {
        allowedDomains.add(domain.toLowerCase());
        return this;
    }

    /**
     * 添加阻止的域名
     */
    public AdvancedSecurityManager addBlockedDomain(@NonNull String domain) {
        blockedDomains.add(domain.toLowerCase());
        return this;
    }

    /**
     * 添加阻止的URL模式（正则表达式）
     */
    public AdvancedSecurityManager addBlockedUrlPattern(@NonNull String pattern) {
        try {
            blockedUrlPatterns.add(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE));
        } catch (Exception e) {
            Log.e(TAG, "Invalid URL pattern: " + pattern, e);
        }
        return this;
    }

    /**
     * 设置严格域名检查
     */
    public AdvancedSecurityManager setStrictDomainChecking(boolean strict) {
        this.strictDomainChecking = strict;
        return this;
    }

    /**
     * 设置XSS防护
     */
    public AdvancedSecurityManager setXSSProtectionEnabled(boolean enabled) {
        this.enableXSSProtection = enabled;
        return this;
    }

    /**
     * 设置内容过滤
     */
    public AdvancedSecurityManager setContentFilteringEnabled(boolean enabled) {
        this.enableContentFiltering = enabled;
        return this;
    }

    /**
     * 设置SSL验证
     */
    public AdvancedSecurityManager setSSLVerificationEnabled(boolean enabled) {
        this.enableSSLVerification = enabled;
        return this;
    }

    /**
     * 检查URL是否安全
     */
    public SecurityCheckResult checkUrl(@Nullable String url) {
        if (url == null || url.trim().isEmpty()) {
            return SecurityCheckResult.BLOCKED_EMPTY_URL;
        }

        try {
            URL parsedUrl = new URL(url);
            String scheme = parsedUrl.getProtocol().toLowerCase();
            String host = parsedUrl.getHost();

            // 检查scheme
            if (!allowedSchemes.contains(scheme)) {
                Log.w(TAG, "Blocked URL with unsupported scheme: " + scheme);
                return SecurityCheckResult.BLOCKED_UNSUPPORTED_SCHEME;
            }

            // 检查URL模式
            if (enableXSSProtection && matchesBlockedPattern(url)) {
                Log.w(TAG, "Blocked URL matching security pattern: " + url);
                return SecurityCheckResult.BLOCKED_SECURITY_PATTERN;
            }

            // 检查域名
            if (host != null) {
                host = host.toLowerCase();

                // 检查是否在黑名单中
                if (isBlockedDomain(host)) {
                    Log.w(TAG, "Blocked URL from blacklisted domain: " + host);
                    return SecurityCheckResult.BLOCKED_DOMAIN;
                }

                // 如果启用了严格检查，验证是否在白名单中
                if (strictDomainChecking && !isAllowedDomain(host)) {
                    Log.w(TAG, "Blocked URL not in allowed domains: " + host);
                    return SecurityCheckResult.BLOCKED_DOMAIN_STRICT;
                }
            }

            return SecurityCheckResult.ALLOWED;

        } catch (MalformedURLException e) {
            Log.w(TAG, "Blocked malformed URL: " + url);
            return SecurityCheckResult.BLOCKED_MALFORMED_URL;
        }
    }

    /**
     * 检查资源请求
     */
    public SecurityCheckResult checkResourceRequest(@Nullable WebResourceRequest request) {
        if (request == null) {
            return SecurityCheckResult.BLOCKED_EMPTY_REQUEST;
        }

        String url = request.getUrl().toString();
        return checkUrl(url);
    }

    /**
     * 过滤响应内容
     */
    @Nullable
    public WebResourceResponse filterResponse(@Nullable WebResourceResponse response,
                                           @Nullable String url) {
        if (!enableContentFiltering || response == null) {
            return response;
        }

        // 检查内容类型
        String mimeType = response.getMimeType();
        if (mimeType != null && shouldBlockContentType(mimeType)) {
            Log.w(TAG, "Blocked content type: " + mimeType + " for URL: " + url);
            return createBlockedResponse();
        }

        return response;
    }

    /**
     * 检查是否应该阻止该内容类型
     */
    private boolean shouldBlockContentType(String mimeType) {
        // 阻止可执行内容
        if (mimeType.contains("javascript") ||
            mimeType.contains("executable") ||
            mimeType.contains("script")) {
            return true;
        }

        // 可以在这里添加更多内容类型检查
        return false;
    }

    /**
     * 创建被阻止的响应
     */
    private WebResourceResponse createBlockedResponse() {
        return new WebResourceResponse("text/plain", "UTF-8", null);
    }

    /**
     * 检查域名是否被阻止
     */
    private boolean isBlockedDomain(String host) {
        for (String blockedDomain : blockedDomains) {
            if (host.equals(blockedDomain) || host.endsWith("." + blockedDomain)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查域名是否被允许
     */
    private boolean isAllowedDomain(String host) {
        for (String allowedDomain : allowedDomains) {
            if (host.equals(allowedDomain) || host.endsWith("." + allowedDomain)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查URL是否匹配阻止模式
     */
    private boolean matchesBlockedPattern(String url) {
        for (Pattern pattern : blockedUrlPatterns) {
            if (pattern.matcher(url).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 安全检查结果枚举
     */
    public enum SecurityCheckResult {
        ALLOWED("URL is allowed"),
        BLOCKED_EMPTY_URL("Empty URL"),
        BLOCKED_EMPTY_REQUEST("Empty request"),
        BLOCKED_UNSUPPORTED_SCHEME("Unsupported URL scheme"),
        BLOCKED_SECURITY_PATTERN("URL matches security pattern"),
        BLOCKED_DOMAIN("Domain is blacklisted"),
        BLOCKED_DOMAIN_STRICT("Domain not in whitelist (strict mode)"),
        BLOCKED_MALFORMED_URL("Malformed URL"),
        BLOCKED_CONTENT_TYPE("Blocked content type");

        private final String description;

        SecurityCheckResult(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public boolean isAllowed() {
            return this == ALLOWED;
        }
    }
}
