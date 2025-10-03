package com.rapid.android.ui.feature.main.mine.coin;

import android.text.TextUtils;

import androidx.lifecycle.MutableLiveData;

import com.core.common.app.BaseApplication;
import com.core.ui.presentation.BaseViewModel;
import com.lib.data.repository.RepositoryProvider;
import com.lib.data.session.SessionManager;
import com.lib.domain.model.*;
import com.lib.domain.repository.ContentRepository;
import com.lib.domain.result.DomainError;
import com.lib.domain.result.DomainResult;
import com.rapid.android.R;
import com.rapid.android.ui.common.paging.PagingController;
import com.rapid.android.ui.common.paging.PagingPayload;

import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class CoinViewModel extends BaseViewModel {

    private final MutableLiveData<CoinHeaderUiState> headerState =
            new MutableLiveData<>(CoinHeaderUiState.empty());

    private final MutableLiveData<String> recordError = new MutableLiveData<>();
    private final MutableLiveData<String> rankError = new MutableLiveData<>();

    private final ContentRepository contentRepository = RepositoryProvider.getContentRepository();
    private final SessionManager sessionManager = SessionManager.getInstance();

    private final PagingController<CoinRecordBean> recordPaging =
            new PagingController<>(this, 1, this::fetchCoinRecords);
    private final PagingController<CoinRankBean> rankPaging =
            new PagingController<>(this, 1, this::fetchCoinRank);

    public MutableLiveData<CoinHeaderUiState> getHeaderState() {
        return headerState;
    }

    public MutableLiveData<List<CoinRecordBean>> getRecordItems() {
        return recordPaging.getItemsLiveData();
    }

    public MutableLiveData<Boolean> getRecordLoading() {
        return recordPaging.getLoadingLiveData();
    }

    public MutableLiveData<Boolean> getRecordLoadingMore() {
        return recordPaging.getLoadingMoreLiveData();
    }

    public MutableLiveData<Boolean> getRecordHasMore() {
        return recordPaging.getHasMoreLiveData();
    }

    public MutableLiveData<String> getRecordPagingError() {
        return recordPaging.getErrorLiveData();
    }

    public MutableLiveData<String> getRecordError() {
        return recordError;
    }

    public MutableLiveData<List<CoinRankBean>> getRankItems() {
        return rankPaging.getItemsLiveData();
    }

    public MutableLiveData<Boolean> getRankLoading() {
        return rankPaging.getLoadingLiveData();
    }

    public MutableLiveData<Boolean> getRankLoadingMore() {
        return rankPaging.getLoadingMoreLiveData();
    }

    public MutableLiveData<Boolean> getRankHasMore() {
        return rankPaging.getHasMoreLiveData();
    }

    public MutableLiveData<String> getRankPagingError() {
        return rankPaging.getErrorLiveData();
    }

    public MutableLiveData<String> getRankError() {
        return rankError;
    }

    public void applySessionState(SessionManager.SessionState state) {
        if (state == null || !state.isLoggedIn()) {
            headerState.setValue(CoinHeaderUiState.empty());
            return;
        }

        UserInfoBean userInfo = state.getUserInfo();
        CoinBean coinBean = userInfo != null ? userInfo.getCoinInfo() : null;
        String placeholder = BaseApplication.getAppContext().getString(R.string.mine_placeholder_dash);

        String coinDisplay = coinBean != null ? String.valueOf(coinBean.getCoinCount()) : placeholder;
        String levelDisplay;
        if (coinBean != null) {
            levelDisplay = BaseApplication.getAppContext()
                    .getString(R.string.mine_membership_level_format, coinBean.getLevel());
        } else {
            levelDisplay = placeholder;
        }

        String rankDisplay = placeholder;
        if (coinBean != null) {
            String rankValue = coinBean.getRank();
            if (!TextUtils.isEmpty(rankValue)) {
                rankDisplay = rankValue.startsWith("#") ? rankValue : String.format(Locale.getDefault(), "#%s", rankValue);
            }
        }

        headerState.setValue(new CoinHeaderUiState(coinDisplay, levelDisplay, rankDisplay));
    }

    public void refreshRecords() {
        sessionManager.refreshUserInfo();
        recordPaging.refresh();
    }

    public void refreshRank() {
        sessionManager.refreshUserInfo();
        rankPaging.refresh();
    }

    public void loadMoreRecords() {
        recordPaging.loadMore();
    }

    public void loadMoreRank() {
        rankPaging.loadMore();
    }

    public void ensureRecords() {
        if (!recordPaging.isInitialized()) {
            refreshRecords();
        }
    }

    public void ensureRank() {
        if (!rankPaging.isInitialized()) {
            refreshRank();
        }
    }

    private Observable<DomainResult<PagingPayload<CoinRecordBean>>> fetchCoinRecords(int page) {
        return contentRepository.coinRecords(page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(result -> {
                    if (result.isSuccess() && result.getData() != null) {
                        PageBean<CoinRecordBean> pageBean = result.getData();
                        int next = pageBean.getCurPage() + 1;
                        boolean more = !pageBean.isOver() && pageBean.getCurPage() < pageBean.getPageCount();
                        return DomainResult.success(new PagingPayload<>(pageBean.getDatas(), next, more));
                    }
                    DomainError error = result.getError();
                    String message = (error != null && !TextUtils.isEmpty(error.getMessage()))
                            ? error.getMessage()
                            : BaseApplication.getAppContext().getString(R.string.coin_error_load_records);
                    recordError.setValue(message);
                    return DomainResult.failure(error != null ? error
                            : DomainError.of(DomainError.UNKNOWN_CODE, message));
                });
    }

    private Observable<DomainResult<PagingPayload<CoinRankBean>>> fetchCoinRank(int page) {
        return contentRepository.coinRank(page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(result -> {
                    if (result.isSuccess() && result.getData() != null) {
                        PageBean<CoinRankBean> pageBean = result.getData();
                        int next = pageBean.getCurPage() + 1;
                        boolean more = !pageBean.isOver() && pageBean.getCurPage() < pageBean.getPageCount();
                        return DomainResult.success(new PagingPayload<>(pageBean.getDatas(), next, more));
                    }
                    DomainError error = result.getError();
                    String message = (error != null && !TextUtils.isEmpty(error.getMessage()))
                            ? error.getMessage()
                            : BaseApplication.getAppContext().getString(R.string.coin_error_load_rank);
                    rankError.setValue(message);
                    return DomainResult.failure(error != null ? error
                            : DomainError.of(DomainError.UNKNOWN_CODE, message));
                });
    }

    public static class CoinHeaderUiState {
        private final String coinDisplay;
        private final String levelDisplay;
        private final String rankDisplay;

        private CoinHeaderUiState(String coinDisplay, String levelDisplay, String rankDisplay) {
            this.coinDisplay = coinDisplay;
            this.levelDisplay = levelDisplay;
            this.rankDisplay = rankDisplay;
        }

        public static CoinHeaderUiState empty() {
            String placeholder = BaseApplication.getAppContext().getString(R.string.mine_placeholder_dash);
            return new CoinHeaderUiState(placeholder, placeholder, placeholder);
        }

        public String getCoinDisplay() {
            return coinDisplay;
        }

        public String getLevelDisplay() {
            return levelDisplay;
        }

        public String getRankDisplay() {
            return rankDisplay;
        }
    }
}
