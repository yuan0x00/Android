package com.rapid.android.ui.feature.main.discover.project;

import androidx.lifecycle.MutableLiveData;

import com.rapid.android.R;
import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.data.repository.RepositoryProvider;
import com.rapid.android.core.domain.model.CategoryNodeBean;
import com.rapid.android.core.domain.model.ProjectPageBean;
import com.rapid.android.core.domain.repository.ContentRepository;
import com.rapid.android.core.domain.result.DomainError;
import com.rapid.android.core.domain.result.DomainResult;
import com.rapid.android.core.ui.presentation.BaseViewModel;
import com.rapid.android.ui.common.paging.PagingPayload;

import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ProjectViewModel extends BaseViewModel {

    private final MutableLiveData<List<CategoryNodeBean>> projectCategories = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> articleErrorMessage = new MutableLiveData<>();
    private final ContentRepository repository = RepositoryProvider.getContentRepository();

    public MutableLiveData<List<CategoryNodeBean>> getProjectCategories() {
        return projectCategories;
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

    void emitArticleError(String message) {
        articleErrorMessage.setValue(message);
    }

    public void loadProjects(boolean forceRefresh) {
        loading.setValue(true);
        autoDispose(repository.projectTree()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<DomainResult<List<CategoryNodeBean>>>() {
                    @Override
                    public void onNext(DomainResult<List<CategoryNodeBean>> result) {
                        if (result.isSuccess() && result.getData() != null) {
                            projectCategories.setValue(result.getData());
                        } else {
                            DomainError error = result.getError();
                            if (error != null && error.getMessage() != null) {
                                errorMessage.setValue(error.getMessage());
                            } else {
                                errorMessage.setValue(BaseApplication.getAppContext()
                                        .getString(R.string.project_error_load_failed));
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        loading.setValue(false);
                        if (e.getMessage() != null) {
                            errorMessage.setValue(e.getMessage());
                        } else {
                            errorMessage.setValue(BaseApplication.getAppContext()
                                    .getString(R.string.project_error_load_failed));
                        }
                    }

                    @Override
                    public void onComplete() {
                        loading.setValue(false);
                    }
                }));
    }

    Observable<DomainResult<PagingPayload<ProjectPageBean.ProjectItemBean>>> fetchProjectArticles(int page, int categoryId) {
        if (categoryId <= 0) {
            String message = BaseApplication.getAppContext().getString(R.string.project_error_invalid_category);
            articleErrorMessage.setValue(message);
            return Observable.just(DomainResult.failure(
                    DomainError.of(DomainError.UNKNOWN_CODE, message)));
        }

        return repository.projectArticles(page, categoryId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(result -> {
                    if (result.isSuccess() && result.getData() != null) {
                        ProjectPageBean pageBean = result.getData();
                        int nextPage = pageBean.getCurPage() + 1;
                        boolean hasMore = !pageBean.isOver() && pageBean.getCurPage() < pageBean.getPageCount();
                        return DomainResult.success(new PagingPayload<>(pageBean.getDatas(), nextPage, hasMore));
                    }

                    DomainError error = result.getError();
                    if (error != null && error.getMessage() != null) {
                        articleErrorMessage.setValue(error.getMessage());
                        return DomainResult.failure(error);
                    }

                    String message = BaseApplication.getAppContext().getString(R.string.project_error_load_failed);
                    articleErrorMessage.setValue(message);
                    return DomainResult.failure(DomainError.of(DomainError.UNKNOWN_CODE, message));
                });
    }
}
