package com.rapid.android.ui.feature.main.mine.share;

import androidx.lifecycle.MutableLiveData;

import com.rapid.android.R;
import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.data.repository.RepositoryProvider;
import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.core.domain.model.UserShareBean;
import com.rapid.android.core.domain.repository.UserRepository;
import com.rapid.android.core.domain.result.DomainError;
import com.rapid.android.core.domain.result.DomainResult;
import com.rapid.android.core.ui.presentation.BaseViewModel;
import com.rapid.android.ui.common.paging.PagingController;
import com.rapid.android.ui.common.paging.PagingPayload;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ShareViewModel extends BaseViewModel {

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final UserRepository repository = RepositoryProvider.getUserRepository();
    private final PagingController<ArticleListBean.Data> pagingController =
            new PagingController<>(this, 1, this::fetchSharePage);

    public MutableLiveData<List<ArticleListBean.Data>> getShareItems() {
        return pagingController.getItemsLiveData();
    }

    public MutableLiveData<Boolean> getLoading() {
        return pagingController.getLoadingLiveData();
    }

    public MutableLiveData<Boolean> getLoadingMore() {
        return pagingController.getLoadingMoreLiveData();
    }

    public MutableLiveData<Boolean> getHasMore() {
        return pagingController.getHasMoreLiveData();
    }

    public MutableLiveData<String> getPagingError() {
        return pagingController.getErrorLiveData();
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void initialize() {
        if (!pagingController.isInitialized()) {
            pagingController.refresh();
        }
    }

    public void refresh() {
        pagingController.refresh();
    }

    public void loadMore() {
        pagingController.loadMore();
    }

    private Observable<DomainResult<PagingPayload<ArticleListBean.Data>>> fetchSharePage(int page) {
        return repository.myShareArticles(page, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(result -> {
                    if (result.isSuccess() && result.getData() != null) {
                        UserShareBean shareBean = result.getData();
                        ArticleListBean list = shareBean != null ? shareBean.getShareArticles() : null;
                        if (list == null) {
                            list = new ArticleListBean();
                        }
                        int nextPage = list.getCurPage() + 1;
                        boolean hasMore = !list.isOver() && list.getCurPage() < list.getPageCount();
                        List<ArticleListBean.Data> items = list.getDatas();
                        if (items == null) {
                            items = new java.util.ArrayList<>();
                        }
                        return DomainResult.success(new PagingPayload<>(items, nextPage, hasMore));
                    }
                    DomainError error = result.getError();
                    String message = (error != null && error.getMessage() != null)
                            ? error.getMessage()
                            : BaseApplication.getAppContext().getString(R.string.share_error_load_failed);
                    errorMessage.setValue(message);
                    return DomainResult.failure(error != null ? error
                            : DomainError.of(DomainError.UNKNOWN_CODE, message));
                });
    }
}
