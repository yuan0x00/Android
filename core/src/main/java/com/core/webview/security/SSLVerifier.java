package com.core.webview.security;

import android.net.http.SslError;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;

/**
 * SSL证书验证器
 * 提供高级的SSL证书验证功能
 */
public class SSLVerifier {
    private static final String TAG = "SSLVerifier";

    private final Set<String> trustedHosts = new HashSet<>();
    private boolean allowSelfSignedCertificates = false;
    private boolean allowExpiredCertificates = false;
    private SSLVerificationListener listener;

    /**
     * 设置SSL验证监听器
     */
    public SSLVerifier setVerificationListener(SSLVerificationListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * 添加信任的主机
     */
    public SSLVerifier addTrustedHost(@NonNull String host) {
        trustedHosts.add(host.toLowerCase());
        return this;
    }

    /**
     * 移除信任的主机
     */
    public SSLVerifier removeTrustedHost(@NonNull String host) {
        trustedHosts.remove(host.toLowerCase());
        return this;
    }

    /**
     * 设置是否允许自签名证书
     */
    public SSLVerifier setAllowSelfSignedCertificates(boolean allow) {
        this.allowSelfSignedCertificates = allow;
        return this;
    }

    /**
     * 设置是否允许过期证书
     */
    public SSLVerifier setAllowExpiredCertificates(boolean allow) {
        this.allowExpiredCertificates = allow;
        return this;
    }

    /**
     * 验证SSL证书
     * @param host 主机名
     * @param sslError SSL错误
     * @return 验证结果
     */
    @NonNull
    public VerificationResult verifyCertificate(@NonNull String host, @NonNull SslError sslError) {
        host = host.toLowerCase();

        // 检查是否是信任主机
        if (trustedHosts.contains(host)) {
            Log.d(TAG, "Host is in trusted list: " + host);
            return VerificationResult.TRUSTED;
        }

        // 分析SSL错误
        int primaryError = sslError.getPrimaryError();

        switch (primaryError) {
            case SslError.SSL_UNTRUSTED:
                Log.w(TAG, "Untrusted certificate for host: " + host);
                if (allowSelfSignedCertificates && isSelfSignedCertificate(sslError)) {
                    return VerificationResult.SELF_SIGNED_ALLOWED;
                }
                return VerificationResult.UNTRUSTED_CA;

            case SslError.SSL_EXPIRED:
                Log.w(TAG, "Expired certificate for host: " + host);
                if (allowExpiredCertificates) {
                    return VerificationResult.EXPIRED_ALLOWED;
                }
                return VerificationResult.CERTIFICATE_EXPIRED;

            case SslError.SSL_IDMISMATCH:
                Log.w(TAG, "Hostname mismatch for host: " + host);
                return VerificationResult.HOSTNAME_MISMATCH;

            case SslError.SSL_INVALID:
                Log.w(TAG, "Invalid certificate for host: " + host);
                return VerificationResult.INVALID_CERTIFICATE;

            default:
                Log.w(TAG, "Unknown SSL error for host: " + host + ", error code: " + primaryError);
                return VerificationResult.UNKNOWN_ERROR;
        }
    }

    /**
     * 处理SSL错误
     * @param host 主机名
     * @param sslError SSL错误
     * @return 是否继续加载
     */
    public boolean handleSSLError(@NonNull String host, @NonNull SslError sslError) {
        VerificationResult result = verifyCertificate(host, sslError);

        // 通知监听器
        if (listener != null) {
            return listener.onSSLVerificationResult(host, sslError, result);
        }

        // 默认行为：只有信任的证书才允许继续
        return result.isAllowed();
    }

    /**
     * 检查是否是自签名证书
     */
    private boolean isSelfSignedCertificate(@NonNull SslError sslError) {
        try {
            // 这里可以实现更复杂的自签名证书检测逻辑
            // 目前使用简单的启发式方法
            String certificateInfo = sslError.getCertificate().toString();
            return certificateInfo.contains("self-signed") ||
                   certificateInfo.contains("selfsigned");
        } catch (Exception e) {
            Log.e(TAG, "Error checking if certificate is self-signed", e);
            return false;
        }
    }

    /**
     * 获取证书信息
     */
    public CertificateInfo getCertificateInfo(@NonNull SslError sslError) {
        try {
            android.net.http.SslCertificate certificate = sslError.getCertificate();
            return new CertificateInfo(
                certificate.getIssuedBy().getDName(),
                certificate.getIssuedTo().getDName(),
                certificate.getValidNotBeforeDate().toString(),
                certificate.getValidNotAfterDate().toString()
            );
        } catch (Exception e) {
            Log.e(TAG, "Error getting certificate info", e);
            return new CertificateInfo("Unknown", "Unknown", "Unknown", "Unknown");
        }
    }

    /**
     * SSL验证结果
     */
    public enum VerificationResult {
        TRUSTED("Certificate is trusted"),
        SELF_SIGNED_ALLOWED("Self-signed certificate allowed"),
        EXPIRED_ALLOWED("Expired certificate allowed"),
        UNTRUSTED_CA("Untrusted certificate authority"),
        HOSTNAME_MISMATCH("Hostname does not match certificate"),
        CERTIFICATE_EXPIRED("Certificate has expired"),
        INVALID_CERTIFICATE("Invalid certificate"),
        UNKNOWN_ERROR("Unknown SSL error");

        private final String description;

        VerificationResult(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public boolean isAllowed() {
            return this == TRUSTED || this == SELF_SIGNED_ALLOWED || this == EXPIRED_ALLOWED;
        }
    }

    public interface SSLVerificationListener {
        /**
         * SSL验证结果
         * @param host 主机名
         * @param error SSL错误
         * @param result 验证结果
         * @return 是否继续加载
         */
        boolean onSSLVerificationResult(@NonNull String host, @NonNull SslError error,
                                      @NonNull VerificationResult result);
    }

    /**
     * 证书信息
     */
    public static class CertificateInfo {
        public final String issuedBy;
        public final String issuedTo;
        public final String validFrom;
        public final String validUntil;

        public CertificateInfo(String issuedBy, String issuedTo, String validFrom, String validUntil) {
            this.issuedBy = issuedBy;
            this.issuedTo = issuedTo;
            this.validFrom = validFrom;
            this.validUntil = validUntil;
        }

        @Override
        public String toString() {
            return "CertificateInfo{" +
                    "issuedBy='" + issuedBy + '\'' +
                    ", issuedTo='" + issuedTo + '\'' +
                    ", validFrom='" + validFrom + '\'' +
                    ", validUntil='" + validUntil + '\'' +
                    '}';
        }
    }
}
