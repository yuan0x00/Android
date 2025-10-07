package com.rapid.android.ui.feature.main.mine.favorite;

import androidx.lifecycle.MutableLiveData;

import com.rapid.android.R;
import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.data.repository.RepositoryProvider;
import com.rapid.android.core.domain.model.ArticleListBean;
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

public class FavoriteViewModel extends BaseViewModel {

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();

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

    public MutableLiveData<String> getToastMessage() {
        return toastMessage;
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

    public void updateFavorite(int articleId, String title, String link, String author) {
        autoDispose(repository.updateCollectedArticle(articleId, title, link, author)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.isSuccess()) {
                        toastMessage.setValue(BaseApplication.getAppContext().getString(R.string.favorite_update_success));
                        pagingController.refresh();
                    } else {
                        DomainError error = result.getError();
                        toastMessage.setValue(error != null && error.getMessage() != null
                                ? error.getMessage()
                                : BaseApplication.getAppContext().getString(R.string.share_article_error_submit));
                    }
                }, throwable -> toastMessage.setValue(throwable != null && throwable.getMessage() != null
                        ? throwable.getMessage()
                        : BaseApplication.getAppContext().getString(R.string.share_article_error_submit))));
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
