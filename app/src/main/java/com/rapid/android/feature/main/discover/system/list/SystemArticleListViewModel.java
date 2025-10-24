package com.rapid.android.feature.main.discover.system.list;

import androidx.lifecycle.MutableLiveData;

import com.rapid.android.R;
import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.data.repository.RepositoryProvider;
import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.core.domain.repository.ContentRepository;
import com.rapid.android.core.domain.result.DomainError;
import com.rapid.android.core.domain.result.DomainResult;
import com.rapid.android.core.ui.presentation.BaseViewModel;
import com.rapid.android.ui.common.paging.PagingController;
import com.rapid.android.ui.common.paging.PagingPayload;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SystemArticleListViewModel extends BaseViewModel {

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final ContentRepository repository = RepositoryProvider.getContentRepository();
    private int categoryId;
    private final PagingController<ArticleListBean.Data> pagingController =
            new PagingController<>(this, 0, this::fetchArticlesPage);

    public MutableLiveData<List<ArticleListBean.Data>> getArticleItems() {
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

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public MutableLiveData<String> getPagingError() {
        return pagingController.getErrorLiveData();
    }

    public void initialize(int cid) {
        if (this.categoryId != cid) {
            this.categoryId = cid;
            pagingController.refresh();
        } else if (!pagingController.isInitialized()) {
            pagingController.refresh();
        }
    }

    public void refresh() {
        if (categoryId <= 0) {
            return;
        }
        pagingController.refresh();
    }

    public void loadMore() {
        if (categoryId <= 0) {
            return;
        }
        pagingController.loadMore();
    }

    private Observable<DomainResult<PagingPayload<ArticleListBean.Data>>> fetchArticlesPage(int page) {
        if (categoryId <= 0) {
            errorMessage.setValue(BaseApplication.getAppContext()
                    .getString(R.string.system_error_invalid_category));
            return Observable.just(DomainResult.failure(DomainError.of(DomainError.UNKNOWN_CODE,
                    BaseApplication.getAppContext().getString(R.string.system_error_invalid_category))));
        }

        return repository.articlesByCategory(page, categoryId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(result -> {
                    if (result.isSuccess() && result.getData() != null) {
                        ArticleListBean pageBean = result.getData();
                        int nextPage = page + 1;
                        boolean hasMore = !pageBean.isOver() && pageBean.getCurPage() < pageBean.getPageCount();
                        return DomainResult.success(new PagingPayload<>(pageBean.getDatas(), nextPage, hasMore));
                    }

                    DomainError error = result.getError();
                    if (error != null && error.getMessage() != null) {
                        errorMessage.setValue(error.getMessage());
                        return DomainResult.failure(error);
                    }

                    errorMessage.setValue(BaseApplication.getAppContext()
                            .getString(R.string.system_error_article_load_failed));
                    return DomainResult.failure(error != null ? error
                            : DomainError.of(DomainError.UNKNOWN_CODE,
                            BaseApplication.getAppContext().getString(R.string.system_error_article_load_failed)));
                });
    }
}
