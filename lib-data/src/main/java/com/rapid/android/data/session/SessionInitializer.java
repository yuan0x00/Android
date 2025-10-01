package com.rapid.android.data.session;

/**
 * 会话初始化器（兼容层）
 * 保持原有调用接口，内部使用统一认证管理器
 */
public final class SessionInitializer {

    private SessionInitializer() {
    }

    public static void restore() {
        // 使用统一认证管理器进行状态恢复
        UnifiedAuthManager.getInstance().restore();
    }
}
