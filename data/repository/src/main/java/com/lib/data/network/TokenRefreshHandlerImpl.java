package com.lib.data.network;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.core.network.interceptor.AuthInterceptor;
import com.lib.data.local.AuthStorage;
import com.lib.data.repository.RepositoryProvider;
import com.lib.data.session.SessionManager;
import com.lib.domain.model.LoginBean;
import com.lib.domain.repository.UserRepository;
import com.lib.domain.result.DomainError;
import com.lib.domain.result.DomainResult;

import java.util.List;

import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.HttpUrl;
import okhttp3.Request;

/**
 * 默认的 Token 刷新实现：使用持久化的账号信息尝试重新登录，并刷新本地会话。
 */
public class TokenRefreshHandlerImpl implements AuthInterceptor.TokenRefreshHandler {

    private final AuthStorage authStorage = AuthStorage.getInstance();
    private final UserRepository userRepository = RepositoryProvider.getUserRepository();

    @Override
    public boolean canRefresh() {
        return !TextUtils.isEmpty(authStorage.peekUsername())
                && !TextUtils.isEmpty(authStorage.peekPassword());
    }

    @Override
    public boolean refreshToken() throws Exception {
        String username = authStorage.peekUsername();
        String password = authStorage.peekPassword();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            return false;
        }

        DomainResult<LoginBean> result = userRepository.reLogin()
                .subscribeOn(Schedulers.io())
                .blockingFirst(DomainResult.failure(
                        DomainError.of(DomainError.UNKNOWN_CODE, "ReLogin failed")));

        if (!result.isSuccess() || result.getData() == null) {
            return false;
        }

        LoginBean loginBean = result.getData();

        authStorage.saveAuthData(
                loginBean.getToken(),
                String.valueOf(loginBean.getId()),
                loginBean.getUsername(),
                password
        ).blockingAwait();

        SessionManager.getInstance().onLoginSuccess(loginBean);
        return true;
    }

    @Override
    public Request rebuildRequest(@NonNull Request original) {
        Request.Builder builder = original.newBuilder();

        String token = authStorage.peekToken();
        if (!TextUtils.isEmpty(token)) {
            builder.removeHeader("Authorization");
            builder.header("Authorization", "Bearer " + token);
        }

        PersistentCookieStore cookieStore = PersistentCookieStore.getInstance();
        if (cookieStore != null) {
            HttpUrl url = original.url();
            List<String> cookies = cookieStore.loadForRequest(url);
            if (!cookies.isEmpty()) {
                builder.removeHeader("Cookie");
                builder.header("Cookie", mergeCookies(cookies));
            }
        }

        return builder.build();
    }

    private String mergeCookies(@NonNull List<String> cookies) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < cookies.size(); i++) {
            if (i > 0) {
                builder.append("; ");
            }
            builder.append(cookies.get(i));
        }
        return builder.toString();
    }
}
