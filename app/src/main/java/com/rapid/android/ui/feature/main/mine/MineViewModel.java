package com.rapid.android.ui.feature.main.mine;

import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.core.ui.presentation.BaseViewModel;
import com.lib.data.repository.RepositoryProvider;
import com.lib.data.session.AuthSessionManager;
import com.lib.data.session.SimpleSessionManager;
import com.lib.domain.model.CoinBean;
import com.lib.domain.model.LoginBean;
import com.lib.domain.model.UserInfoBean;
import com.lib.domain.repository.UserRepository;

public class MineViewModel extends BaseViewModel {

    private final MutableLiveData<MineUiState> uiState = new MutableLiveData<>(MineUiState.guest());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private final UserRepository userRepository = RepositoryProvider.getUserRepository();
    private final SimpleSessionManager sessionManager = SimpleSessionManager.getInstance();

    public LiveData<MineUiState> getUiState() {
        return uiState;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getToastMessage() {
        return toastMessage;
    }

    public void refresh() {
        loading.setValue(true);
        sessionManager.refreshUserInfo();
        // 由于SimpleSessionManager会自动更新状态，我们主要依赖状态监听器来更新UI
        // 但也可以在这里直接获取当前状态
        com.lib.data.session.SimpleSessionManager.SessionState currentState = sessionManager.getCurrentState();
        if (currentState != null && currentState.isLoggedIn()) {
            loading.setValue(false);
            UserInfoBean userInfo = currentState.getUserInfo();
            if (userInfo != null) {
                uiState.setValue(MineUiState.from(userInfo));
            } else {
                // 如果没有用户信息，尝试从API获取
                sessionManager.refreshUserInfo();
            }
        } else {
            loading.setValue(false);
            uiState.setValue(MineUiState.guest());
        }
    }

    public void resetToGuest() {
        loading.setValue(false);
        uiState.setValue(MineUiState.guest());
    }

    private void fetchProfile() {
//        autoDispose(
//                sessionRepository.refreshUserInfo()
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(this::handleUserInfo, throwable -> {
//                            loading.setValue(false);
//                            toastMessage.setValue(throwable.getMessage());
//                            uiState.setValue(MineUiState.guest());
//                        })
//        );
    }

    private void handleUserInfo(UserInfoBean userInfo) {
        loading.setValue(false);
        if (userInfo != null) {
            uiState.setValue(MineUiState.from(userInfo));
            AuthSessionManager.updateUserInfo(userInfo);
        } else {
            uiState.setValue(MineUiState.guest());
        }
    }

    public void applyUserInfo(UserInfoBean userInfo) {
        if (userInfo != null) {
            uiState.setValue(MineUiState.from(userInfo));
        } else {
            uiState.setValue(MineUiState.guest());
        }
    }

    /**
     * 执行签到操作
     */
    public void signIn() {
        if (loading.getValue() != null && loading.getValue()) {
            return; // 防止重复点击
        }
        
        loading.setValue(true);
        
        autoDispose(
            userRepository.signIn()
                .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe(
                    result -> {
                        loading.setValue(false);
                        if (result != null && result.isSuccess()) {
                            CoinBean coinInfo = result.getData();
                            if (coinInfo != null) {
                                toastMessage.setValue("签到成功！获得积分: " + coinInfo.getCoinCount());
                                // 刷新用户信息以获取最新的积分
                                refresh();
                            } else {
                                toastMessage.setValue("签到成功，但未获取到积分信息");
                            }
                        } else {
                            String errorMsg = result != null ? result.getError().getMessage() : "签到失败";
                            toastMessage.setValue(errorMsg);
                        }
                    },
                    throwable -> {
                        loading.setValue(false);
                        toastMessage.setValue("签到失败: " + throwable.getMessage());
                    }
                )
        );
    }

    public static class MineUiState {
        private final boolean loggedIn;
        private final String displayName;
        private final String tagline;
        private final String membershipLabel;
        private final boolean showMembership;
        private final String dailyActionText;
        private final boolean dailyActionEnabled;
        private final String coinDisplay;
        private final String favoriteDisplay;
        private final String achievementDisplay;

        private MineUiState(boolean loggedIn,
                            String displayName,
                            String tagline,
                            String membershipLabel,
                            boolean showMembership,
                            String dailyActionText,
                            boolean dailyActionEnabled,
                            String coinDisplay,
                            String favoriteDisplay,
                            String achievementDisplay) {
            this.loggedIn = loggedIn;
            this.displayName = displayName;
            this.tagline = tagline;
            this.membershipLabel = membershipLabel;
            this.showMembership = showMembership;
            this.dailyActionText = dailyActionText;
            this.dailyActionEnabled = dailyActionEnabled;
            this.coinDisplay = coinDisplay;
            this.favoriteDisplay = favoriteDisplay;
            this.achievementDisplay = achievementDisplay;
        }

        public static MineUiState guest() {
            return new MineUiState(
                    false,
                    "未登录用户",
                    "登录解锁云同步与个性化",
                    "",
                    false,
                    "立即登录",
                    true,
                    "--",
                    "--",
                    "--"
            );
        }

        public static MineUiState from(UserInfoBean userInfoBean) {
            LoginBean user = userInfoBean != null ? userInfoBean.getUserInfo() : new LoginBean();
            CoinBean coin = userInfoBean != null ? userInfoBean.getCoinInfo() : new CoinBean();

            String nickname = !TextUtils.isEmpty(user.getNickname()) ? user.getNickname() : user.getPublicName();
            if (TextUtils.isEmpty(nickname)) {
                nickname = user.getUsername();
            }

            String tagline = !TextUtils.isEmpty(user.getUsername())
                    ? "ID: " + user.getUsername()
                    : "保持优雅的生产力";

            String membershipLabel;
            if (coin.getLevel() > 0) {
                String rankText = !TextUtils.isEmpty(coin.getRank()) ? " · 排名" + coin.getRank() : "";
                membershipLabel = "Lv." + coin.getLevel() + rankText;
            } else {
                membershipLabel = "新手成长中";
            }

            int favoriteCount = user.getCollectIds() != null ? user.getCollectIds().size() : 0;
            int achievementCount = user.getChapterTops() != null ? user.getChapterTops().size() : 0;

            return new MineUiState(
                    true,
                    TextUtils.isEmpty(nickname) ? "锤友" : nickname,
                    tagline,
                    membershipLabel,
                    true,
                    "签到 +5",
                    true,
                    String.valueOf(coin.getCoinCount()),
                    String.valueOf(favoriteCount),
                    String.valueOf(achievementCount)
            );
        }

        public boolean isLoggedIn() {
            return loggedIn;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getTagline() {
            return tagline;
        }

        public String getMembershipLabel() {
            return membershipLabel;
        }

        public boolean isShowMembership() {
            return showMembership;
        }

        public String getDailyActionText() {
            return dailyActionText;
        }

        public boolean isDailyActionEnabled() {
            return dailyActionEnabled;
        }

        public String getCoinDisplay() {
            return coinDisplay;
        }

        public String getFavoriteDisplay() {
            return favoriteDisplay;
        }

        public String getAchievementDisplay() {
            return achievementDisplay;
        }
    }
}
