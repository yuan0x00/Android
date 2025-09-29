package com.rapid.android.domain.usecase;

import com.rapid.android.domain.repository.UserRepository;

import io.reactivex.rxjava3.core.Single;

public class CheckLoginStatusUseCase {
    private final UserRepository repository;

    public CheckLoginStatusUseCase(UserRepository repository) {
        this.repository = repository;
    }

    public Single<Boolean> execute() {
        return repository.isLoggedIn();
    }
}