package com.rapid.android.ui.feature.main.wechat;

import androidx.lifecycle.MutableLiveData;

import com.core.common.app.BaseApplication;
import com.core.data.repository.RepositoryProvider;
import com.core.domain.model.ArticleListBean;
import com.core.domain.model.WxChapterBean;
import com.core.domain.repository.ContentRepository;
import com.core.domain.result.DomainError;
import com.core.domain.result.DomainResult;
import com.core.ui.presentation.BaseViewModel;
import com.rapid.android.R;
import com.rapid.android.ui.common.paging.PagingPayload;

import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class WechatViewModel extends BaseViewModel {

    private final MutableLiveData<List<WxChapterBean>> chapters = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> articleErrorMessage = new MutableLiveData<>();

    private final ContentRepository repository = RepositoryProvider.getContentRepository();

    public MutableLiveData<List<WxChapterBean>> getChapters() {
        return chapters;
    }

    public MutableLiveData<Boolean> getLoading() {
        return loading;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public MutableLiveData<String> getArticleErrorMessage() {
        return articleErrorMessage;
    }

    public void loadWechatChapters(boolean forceRefresh) {
        loading.setValue(true);
        autoDispose(repository.wechatChapters()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<DomainResult<List<WxChapterBean>>>() {
                    @Override
                    public void onNext(DomainResult<List<WxChapterBean>> result) {
                        if (result.isSuccess() && result.getData() != null) {
                            chapters.setValue(result.getData());
                        } else {
                            DomainError error = result.getError();
                            if (error != null && error.getMessage() != null) {
                                errorMessage.setValue(error.getMessage());
                            } else {
                                errorMessage.setValue(BaseApplication.getAppContext()
                                        .getString(R.string.wechat_error_load_chapters));
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        loading.setValue(false);
                        if (e != null && e.getMessage() != null) {
                            errorMessage.setValue(e.getMessage());
                        } else {
                            errorMessage.setValue(BaseApplication.getAppContext()
                                    .getString(R.string.wechat_error_load_chapters));
                        }
                    }

                    @Override
                    public void onComplete() {
                        loading.setValue(false);
                    }
                }));
    }

    Observable<DomainResult<PagingPayload<ArticleListBean.Data>>> fetchWechatArticles(int chapterId, int page) {
        if (chapterId <= 0) {
            String message = BaseApplication.getAppContext().getString(R.string.wechat_error_load_failed);
            articleErrorMessage.setValue(message);
            return Observable.just(DomainResult.failure(DomainError.of(DomainError.UNKNOWN_CODE, message)));
        }

        return repository.wechatArticles(chapterId, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(result -> {
                    if (result.isSuccess() && result.getData() != null) {
                        ArticleListBean bean = result.getData();
                        int nextPage = bean.getCurPage() + 1;
                        boolean hasMore = !bean.isOver();
                        return DomainResult.success(new PagingPayload<>(bean.getDatas(), nextPage, hasMore));
                    }

                    DomainError error = result.getError();
                    if (error != null && error.getMessage() != null) {
                        articleErrorMessage.setValue(error.getMessage());
                        return DomainResult.failure(error);
                    }

                    String message = BaseApplication.getAppContext().getString(R.string.wechat_error_load_failed);
                    articleErrorMessage.setValue(message);
                    return DomainResult.failure(DomainError.of(DomainError.UNKNOWN_CODE, message));
                });
    }
}
