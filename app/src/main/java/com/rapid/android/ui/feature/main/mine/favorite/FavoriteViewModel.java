package com.rapid.android.ui.feature.main.mine.favorite;

import androidx.lifecycle.MutableLiveData;

import com.core.common.app.BaseApplication;
import com.core.data.repository.RepositoryProvider;
import com.core.domain.model.ArticleListBean;
import com.core.domain.repository.UserRepository;
import com.core.domain.result.DomainError;
import com.core.domain.result.DomainResult;
import com.core.ui.presentation.BaseViewModel;
import com.rapid.android.R;
import com.rapid.android.ui.common.paging.PagingController;
import com.rapid.android.ui.common.paging.PagingPayload;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class FavoriteViewModel extends BaseViewModel {

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final UserRepository repository = RepositoryProvider.getUserRepository();
    private final PagingController<ArticleListBean.Data> pagingController =
            new PagingController<>(this, 0, this::fetchFavoritePage);

    public MutableLiveData<List<ArticleListBean.Data>> getFavoriteItems() {
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

    private Observable<DomainResult<PagingPayload<ArticleListBean.Data>>> fetchFavoritePage(int page) {
        return repository.favoriteArticles(page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(result -> {
                    if (result.isSuccess() && result.getData() != null) {
                        ArticleListBean pageBean = result.getData();
                        int next = pageBean.getCurPage() + 1;
                        boolean more = !pageBean.isOver() && pageBean.getCurPage() < pageBean.getPageCount();
                        return DomainResult.success(new PagingPayload<>(pageBean.getDatas(), next, more));
                    }
                    DomainError error = result.getError();
                    String message = (error != null && error.getMessage() != null)
                            ? error.getMessage()
                            : BaseApplication.getAppContext().getString(R.string.favorite_error_load_failed);
                    errorMessage.setValue(message);
                    return DomainResult.failure(error != null ? error
                            : DomainError.of(DomainError.UNKNOWN_CODE, message));
                });
    }
}
