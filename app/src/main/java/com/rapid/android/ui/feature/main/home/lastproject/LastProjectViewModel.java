package com.rapid.android.ui.feature.main.home.lastproject;

import androidx.lifecycle.MutableLiveData;

import com.core.ui.presentation.BaseViewModel;
import com.lib.data.repository.RepositoryProvider;
import com.lib.domain.model.ArticleListBean;
import com.lib.domain.repository.HomeRepository;
import com.lib.domain.result.DomainError;
import com.lib.domain.result.DomainResult;
import com.rapid.android.ui.common.paging.PagingController;
import com.rapid.android.ui.common.paging.PagingPayload;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LastProjectViewModel extends BaseViewModel {
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final HomeRepository repository = RepositoryProvider.getHomeRepository();

    private final PagingController<ArticleListBean.Data> pagingController =
            new PagingController<>(this, 0, this::fetchProjectPage);

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public MutableLiveData<List<ArticleListBean.Data>> getProjectItems() {
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

    public void refresh() {
        pagingController.refresh();
    }

    public void loadMore() {
        pagingController.loadMore();
    }

    private io.reactivex.rxjava3.core.Observable<DomainResult<PagingPayload<ArticleListBean.Data>>> fetchProjectPage(int page) {
        return repository.projectArticles(page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(result -> {
                    if (result.isSuccess() && result.getData() != null) {
                        ArticleListBean bean = result.getData();
                        int next = bean.getCurPage();
                        boolean more = !bean.isOver();
                        return DomainResult.success(new PagingPayload<>(bean.getDatas(), next, more));
                    }
                    DomainError error = result.getError();
                    if (error != null) {
                        errorMessage.setValue(error.getMessage());
                    }
                    return DomainResult.failure(error != null ? error : DomainError.of(DomainError.UNKNOWN_CODE, "获取项目失败"));
                });
    }
}
