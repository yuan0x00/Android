package com.rapid.android.domain.usecase;

import com.core.net.base.BaseResponse;
import com.rapid.android.data.model.UserInfoBean;
import com.rapid.android.domain.repository.UserRepository;

import io.reactivex.rxjava3.core.Single;

public class GetUserProfileUseCase {

    private final UserRepository repository;

    public GetUserProfileUseCase(UserRepository repository) {
        this.repository = repository;
    }

    public Single<UserInfoBean> execute() {
        return repository.fetchUserProfile()
                .map(BaseResponse::requireData);
    }
}
