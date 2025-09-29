package com.rapid.android.domain.usecase;

import com.rapid.android.data.repository.RepositoryProvider;

public class UseCaseProvider {
    public static LoginUserUseCase getLoginUserUseCase() {
        return new LoginUserUseCase(RepositoryProvider.getUserRepository());
    }

    public static LogoutUserUseCase getLogoutUserUseCase() {
        return new LogoutUserUseCase(RepositoryProvider.getUserRepository());
    }

    public static CheckLoginStatusUseCase getCheckLoginStatusUseCase() {
        return new CheckLoginStatusUseCase(RepositoryProvider.getUserRepository());
    }

    public static ReLoginUseCase getReLoginUseCase() {
        return new ReLoginUseCase(RepositoryProvider.getUserRepository());
    }

    public static GetUserProfileUseCase getUserProfileUseCase() {
        return new GetUserProfileUseCase(RepositoryProvider.getUserRepository());
    }
}
