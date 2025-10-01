package com.lib.data.session;

import com.lib.domain.model.UserInfoBean;

import io.reactivex.rxjava3.core.Single;

/**
 * 会话状态仓库（兼容层）
 * 保持原有接口，内部使用统一认证管理器
 */
public final class SessionStateRepository {

    private static final SessionStateRepository INSTANCE = new SessionStateRepository();
    private final UnifiedAuthManager unifiedManager = UnifiedAuthManager.getInstance();

    private SessionStateRepository() {}

    public static SessionStateRepository getInstance() {
        return INSTANCE;
    }

    public Single<UserInfoBean> getCachedUserInfo() {
        return unifiedManager.getCachedOrRefreshUserInfo();
    }

    public Single<UserInfoBean> refreshUserInfo() {
        return unifiedManager.refreshUserInfo();
    }

    public void restore() {
        // 使用统一管理器进行状态恢复
        unifiedManager.restore();
    }
}
