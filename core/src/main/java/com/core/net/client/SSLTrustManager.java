package com.core.net.client;

import android.annotation.SuppressLint;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

/**
 * 信任所有证书（仅用于测试环境）
 * 生产环境请使用正规证书校验
 */
public class SSLTrustManager {

    @SuppressLint("CustomX509TrustManager")
    private static final X509TrustManager TRUST_ALL_MANAGER = new X509TrustManager() {
        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
            // 信任所有客户端证书（空实现）
        }

        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
            // 信任所有服务端证书（空实现）
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    };
    private static final X509TrustManager[] TRUST_MANAGERS = new X509TrustManager[]{TRUST_ALL_MANAGER};

    private SSLTrustManager() {
        // 私有构造，防止实例化
    }

    public static SSLSocketFactory getSSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, TRUST_MANAGERS, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SSLSocketFactory", e);
        }
    }

    public static X509TrustManager[] getTrustManager() {
        return TRUST_MANAGERS.clone(); // 返回副本，避免外部修改
    }

    public static HostnameVerifier getHostnameVerifier() {
        return (hostname, session) -> true; // 信任所有主机名
    }
}