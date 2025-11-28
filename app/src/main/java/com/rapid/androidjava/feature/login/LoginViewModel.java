package com.rapid.android.feature.login;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rapid.android.R;
import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.data.repository.RepositoryProvider;
import com.rapid.android.core.data.session.SessionManager;
import com.rapid.android.core.domain.model.LoginBean;
import com.rapid.android.core.domain.repository.UserRepository;
import com.rapid.android.core.storage.AuthStorage;
import com.rapid.android.core.ui.presentation.BaseViewModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LoginViewModel extends BaseViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> infoMessage = new MutableLiveData<>();
    private final AuthStorage authStorage;

    public LoginViewModel() {
        authStorage = AuthStorage.getInstance();
        userRepository = RepositoryProvider.getUserRepository();
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
                                return authStorage.saveAuthData(token, userId, storedUsername, password)
                                        .andThen(Observable.just(response));
                            }
                            return Observable.just(response);
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    boolean success = response != null && response.getData() != null;
                                    loginSuccess.setValue(success);
                                    if (success) {
                                        SessionManager.getInstance().onLoginSuccess(response.getData());
                                        errorMessage.setValue(null);
                                    } else {
                                        errorMessage.setValue(BaseApplication.getAppContext()
                                                .getString(R.string.login_error));
                                    }
                                },
                                throwable -> errorMessage.setValue(
                                        throwable != null && throwable.getMessage() != null
                                                ? throwable.getMessage()
                                                : BaseApplication.getAppContext()
                                                .getString(R.string.login_error))
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

    public LiveData<String> getInfoMessage() {
        return infoMessage;
    }

    public void register(String username, String password, String rePassword) {
        autoDispose(userRepository.register(username, password, rePassword)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result != null && result.isSuccess()) {
                        infoMessage.setValue(BaseApplication.getAppContext().getString(R.string.login_register_success));
                        login(username, password);
                    } else {
                        errorMessage.setValue(BaseApplication.getAppContext().getString(R.string.login_error));
                    }
                }, throwable -> errorMessage.setValue(
                        throwable != null && throwable.getMessage() != null
                                ? throwable.getMessage()
                                : BaseApplication.getAppContext().getString(R.string.login_error))));
    }
}
