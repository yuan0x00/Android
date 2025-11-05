package com.rapid.android.feature.main.mine;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.rapid.android.R;
import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.data.repository.RepositoryProvider;
import com.rapid.android.core.data.session.SessionManager;
import com.rapid.android.core.domain.model.*;
import com.rapid.android.core.domain.repository.UserRepository;
import com.rapid.android.core.domain.result.DomainResult;
import com.rapid.android.core.storage.PreferenceHelper;
import com.rapid.android.core.ui.presentation.BaseViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MineViewModel extends BaseViewModel {

    private static final String KEY_LAST_SIGN_IN_DATE = "mine_last_sign_in_date";

    // 使用 MediatorLiveData 统一管理 UI 状态
    private final MediatorLiveData<MineUiState> uiState = new MediatorLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();

    private final UserRepository userRepository = RepositoryProvider.getUserRepository();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final PreferenceHelper dataStore;

    private boolean signedInToday = false;
    private boolean shareCountLoaded = false;

    public MineViewModel() {
        dataStore = PreferenceHelper.getDefault();
        signedInToday = hasSignedInToday();

        // 使用 MediatorLiveData 管理会话状态
        uiState.addSource(sessionManager.state, this::onSessionStateChanged);

        // 初始状态
        SessionManager.SessionState currentState = sessionManager.getCurrentState();
        onSessionStateChanged(currentState);
    }

    /**
     * 统一的会话状态处理
     */
    private void onSessionStateChanged(SessionManager.SessionState sessionState) {
        Log.d("MineViewModel", "会话状态变化: " + (sessionState != null ? sessionState.isLoggedIn() : "null"));

        if (sessionState == null || !sessionState.isLoggedIn()) {
            handleGuestState();
            return;
        }

        handleLoggedInState(sessionState);
    }

    /**
     * 处理未登录状态
     */
    private void handleGuestState() {
        signedInToday = false;
        shareCountLoaded = false;
        uiState.setValue(buildGuestUiState());
        loading.setValue(false);
    }

    /**
     * 处理已登录状态
     */
    private void handleLoggedInState(SessionManager.SessionState sessionState) {
        UserInfoBean userInfo = sessionState.getUserInfo();

        if (userInfo != null) {
            // 有用户信息，更新UI
            updateUiWithUserInfo(userInfo);
        } else {
            // 用户信息为空，需要刷新
            refreshUserInfoIfNeeded();
        }
    }

    /**
     * 使用用户信息更新UI
     */
    private void updateUiWithUserInfo(UserInfoBean userInfo) {
        signedInToday = hasSignedInToday();
        uiState.setValue(buildLoggedInUiState(userInfo, signedInToday));
        loading.setValue(false);

        if (!shareCountLoaded) {
            loadShareCount();
        }
    }

    /**
     * 刷新用户信息（如果需要）
     */
    private void refreshUserInfoIfNeeded() {
        if (Boolean.TRUE.equals(loading.getValue())) {
            return; // 已经在加载中
        }

        loading.setValue(true);
        sessionManager.refreshUserInfo();

        // 设置超时保护（5秒后自动停止加载）
        autoDispose(
                io.reactivex.rxjava3.core.Observable.timer(5, java.util.concurrent.TimeUnit.SECONDS)
                        .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
                        .subscribe(
                                timeout -> {
                                    if (Boolean.TRUE.equals(loading.getValue())) {
                                        loading.setValue(false);
                                        toastMessage.setValue("加载超时，请重试");
                                    }
                                },
                                throwable -> {} // 忽略错误
                        )
        );
    }

    // LiveData 暴露方法
    public LiveData<MineUiState> getUiState() { return uiState; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getToastMessage() { return toastMessage; }

    /**
     * 刷新数据
     */
    public void refresh() {
        if (sessionManager.isLoggedIn()) {
            sessionManager.refreshUserInfo();
        }
    }

    /**
     * 执行签到操作
     */
    public void signIn() {
        if (Boolean.TRUE.equals(loading.getValue())) {
            return; // 防止重复点击
        }

        if (signedInToday) {
            toastMessage.setValue(getString(R.string.mine_checkin_already_done));
            return;
        }

        if (!sessionManager.isLoggedIn()) {
            toastMessage.setValue("请先登录");
            return;
        }

        loading.setValue(true);

        autoDispose(
                userRepository.signIn()
                        .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                        .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> handleSignInResult(result),
                                throwable -> handleSignInError(throwable)
                        )
        );
    }

    private void handleSignInResult(DomainResult<CoinBean> result) {
        loading.setValue(false);

        if (result != null && result.isSuccess()) {
            CoinBean coinInfo = result.getData();
            if (coinInfo != null) {
                toastMessage.setValue(BaseApplication.getAppContext().getString(R.string.mine_checkin_success_points, coinInfo.getCoinCount()));
            } else {
                toastMessage.setValue(getString(R.string.mine_checkin_success_no_points));
            }
            signedInToday = true;
            saveSignInDate();
            refresh(); // 刷新用户信息以更新硬币数量
        } else {
            String errorMsg = result != null && result.getError() != null ? result.getError().getMessage() : null;
            toastMessage.setValue(errorMsg != null ?
                    BaseApplication.getAppContext().getString(R.string.mine_checkin_failed_with_reason, errorMsg) :
                    getString(R.string.mine_checkin_failed));
        }
    }

    private void handleSignInError(Throwable throwable) {
        loading.setValue(false);
        String errorMsg = throwable != null ? throwable.getMessage() : null;
        toastMessage.setValue(errorMsg != null ?
                BaseApplication.getAppContext().getString(R.string.mine_checkin_failed_with_reason, errorMsg) :
                getString(R.string.mine_checkin_failed));
    }

    // 分享数量加载
    private void loadShareCount() {
        if (shareCountLoaded || !sessionManager.isLoggedIn()) {
            return;
        }

        autoDispose(
                userRepository.myShareArticles(1, 1)
                        .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                        .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> {
                                    if (result != null && result.isSuccess() && result.getData() != null) {
                                        ArticleListBean shares = result.getData().getShareArticles();
                                        int total = shares != null ? shares.getTotal() : 0;
                                        shareCountLoaded = true;
                                        updateShareDisplay(String.valueOf(total));
                                    }
                                },
                                throwable -> {
                                    // 静默失败，不影响主流程
                                }
                        )
        );
    }

    private void updateShareDisplay(String shareCountDisplay) {
        MineUiState currentState = uiState.getValue();
        if (currentState != null) {
            uiState.setValue(currentState.withShareCount(shareCountDisplay));
        }
    }

    private MineUiState buildGuestUiState() {
        return MineUiState.guest();
    }

    private MineUiState buildLoggedInUiState(UserInfoBean userInfo, boolean hasSignedInToday) {
        return MineUiState.from(userInfo, hasSignedInToday);
    }

    // 工具方法
    private boolean hasSignedInToday() {
        String lastDate = dataStore.getString(KEY_LAST_SIGN_IN_DATE, "");
        return TextUtils.equals(lastDate, getTodayKey());
    }

    private void saveSignInDate() {
        dataStore.putString(KEY_LAST_SIGN_IN_DATE, getTodayKey());
    }

    private String getTodayKey() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return formatter.format(new Date());
    }

    private String getString(int resId) {
        return BaseApplication.getAppContext().getString(resId);
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
