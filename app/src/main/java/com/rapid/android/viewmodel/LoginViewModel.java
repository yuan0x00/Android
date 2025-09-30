package com.rapid.android.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.core.presentation.viewmodel.BaseViewModel;
import com.rapid.android.data.model.LoginBean;
import com.rapid.android.data.repository.RepositoryProvider;
import com.rapid.android.data.repository.user.UserRepository;
import com.rapid.android.data.session.AuthSessionManager;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LoginViewModel extends BaseViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LoginViewModel() {
        this.userRepository = RepositoryProvider.getUserRepository();
    }

    public void login(String username, String password) {
        autoDispose(
                userRepository.login(username, password)
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
                                return userRepository.saveAuthData(token, userId, storedUsername, password)
                                        .andThen(io.reactivex.rxjava3.core.Observable.just(response));
                            }
                            return io.reactivex.rxjava3.core.Observable.just(response);
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    boolean success = response != null && response.getData() != null;
                                    loginSuccess.setValue(success);
                                    if (success) {
                                        AuthSessionManager.notifyLogin();
                                        errorMessage.setValue(null);
                                    } else {
                                        errorMessage.setValue("登录失败");
                                    }
                                },
                                throwable -> errorMessage.setValue(throwable.getMessage())
                        )
        );
    }

    @NonNull
    private String safeString(String value) {
        return value != null ? value : "";
    }

    public LiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
