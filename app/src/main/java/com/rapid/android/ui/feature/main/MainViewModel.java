package com.rapid.android.ui.feature.main;

import androidx.lifecycle.MutableLiveData;

import com.core.common.app.BaseApplication;
import com.core.data.repository.RepositoryProvider;
import com.core.data.session.SessionManager;
import com.core.domain.repository.UserRepository;
import com.core.domain.result.DomainError;
import com.core.log.LogKit;
import com.core.ui.presentation.BaseViewModel;
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
                                        SessionManager sessionManager = SessionManager.getInstance();
                                        SessionManager.SessionState currentState = sessionManager.getCurrentState();
                                        if (currentState == null || !currentState.isLoggedIn() || currentState.getUserInfo() == null) {
                                            sessionManager.refreshUserInfo();
                                        }
                                    } else {
                                        attemptReLogin();
                                    }
                                }, throwable -> {
                                    SessionManager.getInstance().forceLogout();
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
                                        SessionManager.getInstance().onLoginSuccess(result.getData());
                                        errorMessage.setValue(null);
                                    } else {
                                        SessionManager.getInstance().forceLogout();
                                        DomainError error = result != null ? result.getError() : null;
                                        if (error != null && error.getMessage() != null) {
                                            LogKit.e(error.getMessage());
//                                            errorMessage.setValue(error.getMessage());
                                        } else {
                                            errorMessage.setValue(BaseApplication.getAppContext()
                                                    .getString(R.string.main_auto_login_failed));
                                        }
                                    }
                                },
                                throwable -> {
                                    SessionManager.getInstance().forceLogout();
                                    errorMessage.setValue(throwable != null && throwable.getMessage() != null
                                            ? throwable.getMessage()
                                            : BaseApplication.getAppContext().getString(R.string.main_auto_login_failed));
                                }
                        )
        );
    }
}
