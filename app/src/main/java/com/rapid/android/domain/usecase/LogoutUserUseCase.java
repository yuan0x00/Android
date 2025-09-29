package com.rapid.android.domain.usecase;

import com.core.net.base.BaseResponse;
import com.rapid.android.domain.repository.UserRepository;

import io.reactivex.rxjava3.core.Single;

public class LogoutUserUseCase {
    private final UserRepository repository;

    public LogoutUserUseCase(UserRepository repository) {
        this.repository = repository;
    }

    public Single<BaseResponse<String>> execute() {
        return repository.logout();
    }
}