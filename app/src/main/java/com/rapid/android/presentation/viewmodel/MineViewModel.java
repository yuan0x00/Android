package com.rapid.android.presentation.viewmodel;

import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.core.base.vm.BaseViewModel;
import com.rapid.android.data.model.CoinBean;
import com.rapid.android.data.model.LoginBean;
import com.rapid.android.data.model.UserInfoBean;
import com.rapid.android.data.session.AuthSessionManager;
import com.rapid.android.data.session.SessionStateRepository;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

public class MineViewModel extends BaseViewModel {

    private final MutableLiveData<MineUiState> uiState = new MutableLiveData<>(MineUiState.guest());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private final SessionStateRepository sessionRepository = SessionStateRepository.getInstance();

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
        autoDispose(
                sessionRepository.getCachedUserInfo()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleUserInfo, throwable -> {
                            loading.setValue(false);
                            toastMessage.setValue(throwable.getMessage());
                            uiState.setValue(MineUiState.guest());
                        })
        );
    }

    public void resetToGuest() {
        loading.setValue(false);
        uiState.setValue(MineUiState.guest());
    }

    private void fetchProfile() {
        autoDispose(
                sessionRepository.refreshUserInfo()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleUserInfo, throwable -> {
                            loading.setValue(false);
                            toastMessage.setValue(throwable.getMessage());
                            uiState.setValue(MineUiState.guest());
                        })
        );
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
