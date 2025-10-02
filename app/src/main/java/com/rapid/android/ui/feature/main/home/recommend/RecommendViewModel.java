package com.rapid.android.ui.feature.main.home.recommend;

import androidx.lifecycle.MutableLiveData;

import com.core.ui.presentation.BaseViewModel;
import com.lib.data.repository.RepositoryProvider;
import com.lib.domain.model.ArticleListBean;
import com.lib.domain.model.BannerItemBean;
import com.lib.domain.repository.HomeRepository;
import com.lib.domain.result.DomainError;
import com.lib.domain.result.DomainResult;
import com.rapid.android.ui.common.paging.PagingController;
import com.rapid.android.ui.common.paging.PagingPayload;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class RecommendViewModel extends BaseViewModel {

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<BannerItemBean>> bannerList = new MutableLiveData<>(new ArrayList<>());
    private final HomeRepository repository = RepositoryProvider.getHomeRepository();

    private final PagingController<ArticleListBean.Data> pagingController =
            new PagingController<>(this, 0, this::fetchArticlePage);

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public MutableLiveData<ArrayList<BannerItemBean>> getBannerList() {
        return bannerList;
    }

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

    public MutableLiveData<String> getPagingError() {
        return pagingController.getErrorLiveData();
    }

    public void refreshAll() {
        loadBanner();
        pagingController.refresh();
    }

    public void loadMoreArticles() {
        pagingController.loadMore();
    }

    private void loadBanner() {
        autoDispose(repository.banner()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.isSuccess() && result.getData() != null) {
                        bannerList.setValue(result.getData());
                    } else {
                        DomainError error = result.getError();
                        errorMessage.setValue(error != null ? error.getMessage() : "获取 Banner 失败");
                    }
                }, throwable -> errorMessage.setValue(throwable.getMessage())));
    }

    private io.reactivex.rxjava3.core.Observable<DomainResult<PagingPayload<ArticleListBean.Data>>> fetchArticlePage(int page) {
        return repository.homeArticles(page)
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
                    return DomainResult.failure(error != null ? error : DomainError.of(DomainError.UNKNOWN_CODE, "获取文章失败"));
                });
    }
}
