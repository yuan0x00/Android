package com.rapid.android.domain.usecase;

import androidx.annotation.NonNull;

import com.core.net.base.BaseResponse;
import com.rapid.android.data.model.LoginBean;
import com.rapid.android.domain.repository.UserRepository;

import io.reactivex.rxjava3.core.Single;

public class LoginUserUseCase {
    private final UserRepository repository;

    public LoginUserUseCase(UserRepository repository) {
        this.repository = repository;
    }

    public Single<BaseResponse<LoginBean>> execute(String username, String password) {
        return repository.login(username, password)
                .flatMap(response -> {
                    if (response != null && response.isSuccess() && response.getData() != null) {
                        LoginBean data = response.getData();
                        String token = safeString(data.getToken());
                        String userId = String.valueOf(data.getId());
                        String storedUsername = safeString(data.getUsername());
                        if (storedUsername.isEmpty()) {
                            storedUsername = safeString(data.getPublicName());
                        }
                        if (storedUsername.isEmpty()) {
                            storedUsername = username;
                        }
                        return repository.saveAuthData(token, userId, storedUsername, password)
                                .andThen(Single.just(response));
                    }
                    return Single.just(response);
                });
    }

    @NonNull
    private String safeString(String value) {
        return value != null ? value : "";
    }
}
