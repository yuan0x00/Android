package com.rapid.android.ui.feature.main;

import androidx.lifecycle.MutableLiveData;

import com.core.common.app.BaseApplication;
import com.core.ui.presentation.BaseViewModel;
import com.lib.data.repository.RepositoryProvider;
import com.lib.domain.repository.UserRepository;
import com.lib.domain.result.DomainError;
import com.rapid.android.R;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainViewModel extends BaseViewModel {

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final UserRepository userRepository;

    public MainViewModel() {
        this.userRepository = RepositoryProvider.getUserRepository();
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void refreshLoginState() {
        autoDispose(
                userRepository.isLoggedIn()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(isLogged -> {
                                    if (isLogged != null && isLogged) {
                                        com.lib.data.session.SessionManager sessionManager = com.lib.data.session.SessionManager.getInstance();
                                        com.lib.data.session.SessionManager.SessionState currentState = sessionManager.getCurrentState();
                                        if (currentState == null || !currentState.isLoggedIn() || currentState.getUserInfo() == null) {
                                            sessionManager.refreshUserInfo();
                                        }
                                    } else {
                                        attemptReLogin();
                                    }
                                }, throwable -> {
                                    com.lib.data.session.SessionManager.getInstance().forceLogout();
                                    errorMessage.setValue(throwable != null && throwable.getMessage() != null
                                            ? throwable.getMessage()
                                            : BaseApplication.getAppContext().getString(R.string.main_auto_login_failed));
                                }
                        )
        );
    }

    private void attemptReLogin() {
        autoDispose(
                userRepository.reLogin()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> {
                                    if (result != null && result.isSuccess() && result.getData() != null) {
                                        com.lib.data.session.SessionManager.getInstance().onLoginSuccess(result.getData());
                                        errorMessage.setValue(null);
                                    } else {
                                        com.lib.data.session.SessionManager.getInstance().forceLogout();
                                        DomainError error = result != null ? result.getError() : null;
                                        if (error != null && error.getMessage() != null) {
                                            errorMessage.setValue(error.getMessage());
                                        } else {
                                            errorMessage.setValue(BaseApplication.getAppContext()
                                                    .getString(R.string.main_auto_login_failed));
                                        }
                                    }
                                },
                                throwable -> {
                                    com.lib.data.session.SessionManager.getInstance().forceLogout();
                                    errorMessage.setValue(throwable != null && throwable.getMessage() != null
                                            ? throwable.getMessage()
                                            : BaseApplication.getAppContext().getString(R.string.main_auto_login_failed));
                                }
                        )
        );
    }
}
