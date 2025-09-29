package com.rapid.android.domain.usecase;

import com.core.net.base.BaseResponse;
import com.rapid.android.data.model.LoginBean;
import com.rapid.android.domain.repository.UserRepository;

import io.reactivex.rxjava3.core.Single;

public class ReLoginUseCase {
    private final UserRepository repository;

    public ReLoginUseCase(UserRepository repository) {
        this.repository = repository;
    }

    public Single<BaseResponse<LoginBean>> execute() {
        return repository.reLogin();
    }
}