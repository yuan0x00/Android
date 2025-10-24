package com.rapid.android.feature.main.mine;

import android.content.Context;
import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rapid.android.R;
import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.data.repository.RepositoryProvider;
import com.rapid.android.core.data.session.SessionManager;
import com.rapid.android.core.datastore.DefaultDataStore;
import com.rapid.android.core.datastore.IDataStore;
import com.rapid.android.core.domain.model.*;
import com.rapid.android.core.domain.repository.UserRepository;
import com.rapid.android.core.ui.presentation.BaseViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MineViewModel extends BaseViewModel {

    private static final String KEY_LAST_SIGN_IN_DATE = "mine_last_sign_in_date";

    private final MutableLiveData<MineUiState> uiState = new MutableLiveData<>(MineUiState.guest());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private final UserRepository userRepository = RepositoryProvider.getUserRepository();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final IDataStore dataStore;
    private boolean profileLoading = false;
    private boolean signedInToday = false;
    private boolean shareCountLoading = false;
    private boolean shareCountLoaded = false;

    public MineViewModel() {
        dataStore = new DefaultDataStore();
        signedInToday = hasSignedInToday();
        // 在 ViewModel 中订阅 SessionManager 状态变化
        observeSessionState();
    }

    private void observeSessionState() {
        sessionManager.state.observeForever(this::handleSessionStateChange);
    }

    private void handleSessionStateChange(SessionManager.SessionState sessionState) {
        signedInToday = hasSignedInToday();
        if (sessionState == null || !sessionState.isLoggedIn()) {
            resetToGuest();
            return;
        }

        UserInfoBean userInfo = sessionState.getUserInfo();
        if (userInfo != null) {
            stopLoading();
            loading.setValue(false);
            uiState.setValue(MineUiState.from(userInfo, signedInToday));
            loadShareCount();
        } else {
            if (!profileLoading) {
                startLoading();
                sessionManager.refreshUserInfo();
            } else {
                loading.setValue(true);
            }
        }
    }

    public LiveData<MineUiState> getUiState() {
        return uiState;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getToastMessage() {
        return toastMessage;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // 移除 SessionManager 观察者，避免内存泄漏
        sessionManager.state.removeObserver(this::handleSessionStateChange);
    }

    public void refresh() {
        if (profileLoading) {
            return;
        }
        startLoading();
        sessionManager.refreshUserInfo();
    }

    public void resetToGuest() {
        stopLoading();
        loading.setValue(false);
        signedInToday = hasSignedInToday();
        shareCountLoaded = false;
        shareCountLoading = false;
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
        stopLoading();
        loading.setValue(false);
        if (userInfo != null) {
            uiState.setValue(MineUiState.from(userInfo, signedInToday));
            SessionManager.getInstance().updateUserInfo(userInfo);
        } else {
            uiState.setValue(MineUiState.guest());
        }
    }

    public void applyUserInfo(UserInfoBean userInfo) {
        if (userInfo != null) {
            uiState.setValue(MineUiState.from(userInfo, signedInToday));
            loadShareCount();
        } else {
            uiState.setValue(MineUiState.guest());
            shareCountLoaded = false;
        }
    }

    /**
     * 执行签到操作
     */
    public void signIn() {
        if (loading.getValue() != null && loading.getValue()) {
            return; // 防止重复点击
        }
        if (signedInToday) {
            toastMessage.setValue(BaseApplication.getAppContext()
                    .getString(R.string.mine_checkin_already_done));
            return;
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
                                toastMessage.setValue(BaseApplication.getAppContext()
                                        .getString(R.string.mine_checkin_success_points, coinInfo.getCoinCount()));
                            } else {
                                toastMessage.setValue(BaseApplication.getAppContext()
                                        .getString(R.string.mine_checkin_success_no_points));
                            }
                            signedInToday = true;
                            saveSignInDate();
                            MineUiState state = uiState.getValue();
                            if (state != null) {
                                uiState.setValue(state.withDailyAction(
                                        BaseApplication.getAppContext().getString(R.string.mine_action_checked_in),
                                        false));
                            }
                            refresh();
                        } else {
                            String errorMsg = result != null && result.getError() != null
                                    ? result.getError().getMessage()
                                    : null;
                            toastMessage.setValue(errorMsg != null
                                    ? BaseApplication.getAppContext().getString(
                                            R.string.mine_checkin_failed_with_reason, errorMsg)
                                    : BaseApplication.getAppContext().getString(R.string.mine_checkin_failed));
                        }
                    },
                    throwable -> {
                        loading.setValue(false);
                        toastMessage.setValue(throwable != null && throwable.getMessage() != null
                                ? BaseApplication.getAppContext().getString(
                                        R.string.mine_checkin_failed_with_reason, throwable.getMessage())
                                : BaseApplication.getAppContext().getString(R.string.mine_checkin_failed));
                    }
                )
        );
    }

    private void startLoading() {
        profileLoading = true;
        loading.setValue(true);
    }

    private void stopLoading() {
        profileLoading = false;
    }

    private void loadShareCount() {
        if (shareCountLoading || shareCountLoaded) {
            return;
        }
        shareCountLoading = true;
        autoDispose(
                userRepository.myShareArticles(1, 1)
                        .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                        .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
                        .subscribe(result -> {
                            shareCountLoading = false;
                            if (result != null && result.isSuccess()) {
                                int total = 0;
                                if (result.getData() != null && result.getData().getShareArticles() != null) {
                                    ArticleListBean shares = result.getData().getShareArticles();
                                    total = shares != null ? shares.getTotal() : 0;
                                }
                                shareCountLoaded = true;
                                updateShareDisplay(String.valueOf(total));
                            }
                        }, throwable -> shareCountLoading = false)
        );
    }

    private void updateShareDisplay(String display) {
        MineUiState state = uiState.getValue();
        if (state != null) {
            uiState.setValue(state.withShareCount(display));
        }
    }

    private boolean hasSignedInToday() {
        String lastDate = dataStore.getString(KEY_LAST_SIGN_IN_DATE, "");
        if (TextUtils.isEmpty(lastDate)) {
            return false;
        }
        return TextUtils.equals(lastDate, getTodayKey());
    }

    private void saveSignInDate() {
        dataStore.putString(KEY_LAST_SIGN_IN_DATE, getTodayKey());
    }

    private String getTodayKey() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return formatter.format(new Date());
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
        private final String shareDisplay;

        private MineUiState(boolean loggedIn,
                            String displayName,
                            String tagline,
                            String membershipLabel,
                            boolean showMembership,
                            String dailyActionText,
                            boolean dailyActionEnabled,
                            String coinDisplay,
                            String favoriteDisplay,
                            String shareDisplay) {
            this.loggedIn = loggedIn;
            this.displayName = displayName;
            this.tagline = tagline;
            this.membershipLabel = membershipLabel;
            this.showMembership = showMembership;
            this.dailyActionText = dailyActionText;
            this.dailyActionEnabled = dailyActionEnabled;
            this.coinDisplay = coinDisplay;
            this.favoriteDisplay = favoriteDisplay;
            this.shareDisplay = shareDisplay;
        }

        public static MineUiState guest() {
            Context context = BaseApplication.getAppContext();
            return new MineUiState(
                    false,
                    context.getString(R.string.mine_guest_display_name),
                    context.getString(R.string.mine_guest_tagline),
                    "",
                    false,
                    context.getString(R.string.mine_action_check_in),
                    false,
                    context.getString(R.string.mine_placeholder_dash),
                    context.getString(R.string.mine_placeholder_dash),
                    context.getString(R.string.mine_placeholder_dash)
            );
        }

        public static MineUiState from(UserInfoBean userInfoBean, boolean signedInToday) {
            LoginBean user = userInfoBean != null ? userInfoBean.getUserInfo() : new LoginBean();
            CoinBean coin = userInfoBean != null ? userInfoBean.getCoinInfo() : new CoinBean();
            CollectArticleInfoBean collectInfo =
                    userInfoBean != null ? userInfoBean.getCollectArticleInfo() : new CollectArticleInfoBean();

            String nickname = !TextUtils.isEmpty(user.getNickname()) ? user.getNickname() : user.getPublicName();
            if (TextUtils.isEmpty(nickname)) {
                nickname = user.getUsername();
            }

            Context context = BaseApplication.getAppContext();

            String tagline = !TextUtils.isEmpty(user.getUsername())
                    ? context.getString(R.string.mine_tagline_id_format, user.getUsername())
                    : context.getString(R.string.mine_tagline_default);

            String membershipLabel;
            if (coin.getLevel() > 0) {
                String rank = !TextUtils.isEmpty(coin.getRank()) ? coin.getRank() : "";
                if (!TextUtils.isEmpty(rank)) {
                    membershipLabel = context.getString(R.string.mine_membership_level_with_rank,
                            coin.getLevel(), rank);
                } else {
                    membershipLabel = context.getString(R.string.mine_membership_level_format, coin.getLevel());
                }
            } else {
                membershipLabel = context.getString(R.string.mine_membership_newbie);
            }

            int favoriteCount = collectInfo != null ? collectInfo.getCount() : 0;
            String dailyText;
            boolean dailyEnabled;
            if (signedInToday) {
                dailyText = context.getString(R.string.mine_action_checked_in);
                dailyEnabled = false;
            } else {
                dailyText = context.getString(R.string.mine_action_check_in);
                dailyEnabled = true;
            }

            return new MineUiState(
                    true,
                    TextUtils.isEmpty(nickname) ? context.getString(R.string.mine_guest_display_name) : nickname,
                    tagline,
                    membershipLabel,
                    true,
                    dailyText,
                    dailyEnabled,
                    String.valueOf(coin.getCoinCount()),
                    String.valueOf(favoriteCount),
                    context.getString(R.string.mine_placeholder_dash)
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

        public String getShareDisplay() {
            return shareDisplay;
        }

        public MineUiState withDailyAction(String text, boolean enabled) {
            return new MineUiState(
                    loggedIn,
                    displayName,
                    tagline,
                    membershipLabel,
                    showMembership,
                    text,
                    enabled,
                    coinDisplay,
                    favoriteDisplay,
                    shareDisplay
            );
        }

        public MineUiState withShareCount(String display) {
            return new MineUiState(
                    loggedIn,
                    displayName,
                    tagline,
                    membershipLabel,
                    showMembership,
                    dailyActionText,
                    dailyActionEnabled,
                    coinDisplay,
                    favoriteDisplay,
                    display
            );
        }
    }
}
