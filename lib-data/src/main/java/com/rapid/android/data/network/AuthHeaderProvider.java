package com.rapid.android.data.network;

import androidx.annotation.NonNull;

import com.core.network.client.NetworkConfig;
import com.rapid.android.data.local.AuthStorage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 自动添加认证头信息的提供者
 * 从AuthStorage中获取token并添加到请求头中
 */
public class AuthHeaderProvider implements NetworkConfig.HeaderProvider {
    
    private final AuthStorage authStorage = AuthStorage.getInstance();
    
    @NonNull
    @Override
    public Map<String, String> provideHeaders() {
        // 同步获取token，这在Interceptor中执行，不会阻塞UI线程
        try {
            String token = authStorage.getAuthToken().blockingGet();
            if (token != null && !token.isEmpty()) {
                Map<String, String> headers = new HashMap<>();
                // 根据API的要求设置认证头，通常是Authorization: Bearer <token>
                // 或者直接使用固定格式的header
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        } catch (Exception e) {
            // 如果获取token失败，返回空headers
            e.printStackTrace();
        }
        return Collections.emptyMap();
    }
}