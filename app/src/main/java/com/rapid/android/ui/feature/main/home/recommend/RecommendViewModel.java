package com.rapid.android.ui.feature.main.home.recommend;

import androidx.lifecycle.MutableLiveData;

import com.rapid.android.R;
import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.data.repository.RepositoryProvider;
import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.core.domain.model.BannerItemBean;
import com.rapid.android.core.domain.model.CategoryNodeBean;
import com.rapid.android.core.domain.model.PopularColumnBean;
import com.rapid.android.core.domain.repository.ContentRepository;
import com.rapid.android.core.domain.repository.HomeRepository;
import com.rapid.android.core.domain.result.DomainError;
import com.rapid.android.core.domain.result.DomainResult;
import com.rapid.android.core.ui.presentation.BaseViewModel;
import com.rapid.android.ui.common.paging.PagingController;
import com.rapid.android.ui.common.paging.PagingPayload;
import com.rapid.android.utils.AppPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class RecommendViewModel extends BaseViewModel {

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<BannerItemBean>> bannerList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<ArticleListBean.Data>> topArticles = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<HomePopularSection>> popularSections = new MutableLiveData<>(Collections.emptyList());
    private final HomeRepository repository = RepositoryProvider.getHomeRepository();
    private final ContentRepository contentRepository = RepositoryProvider.getContentRepository();

    private final PagingController<ArticleListBean.Data> pagingController =
            new PagingController<>(this, 0, this::fetchArticlePage);

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public MutableLiveData<ArrayList<BannerItemBean>> getBannerList() {
        return bannerList;
    }

    public MutableLiveData<List<ArticleListBean.Data>> getTopArticles() {
        return topArticles;
    }

    public MutableLiveData<List<ArticleListBean.Data>> getArticleItems() {
        return pagingController.getItemsLiveData();
    }

    public MutableLiveData<List<HomePopularSection>> getPopularSections() {
        return popularSections;
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
        loadHighlights();
        loadPopularSections();
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
                        if (error != null && error.getMessage() != null) {
                            errorMessage.setValue(error.getMessage());
                        } else {
                            errorMessage.setValue(BaseApplication.getAppContext()
                                    .getString(R.string.home_banner_load_failed));
                        }
                    }
                }, throwable -> errorMessage.setValue(throwable != null && throwable.getMessage() != null
                        ? throwable.getMessage()
                        : BaseApplication.getAppContext().getString(R.string.home_banner_load_failed))));
    }

    private void loadHighlights() {
        if (AppPreferences.isHomeTopEnabled()) {
            autoDispose(repository.topArticles()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
                        if (result.isSuccess() && result.getData() != null) {
                            topArticles.setValue(result.getData());
                        }
                    }, throwable -> {}));
        } else {
            topArticles.setValue(new ArrayList<>());
        }

    }

    private void loadPopularSections() {
        Observable<DomainResult<List<ArticleListBean.Data>>> wendaObservable = contentRepository.popularWenda();
        Observable<DomainResult<List<PopularColumnBean>>> columnObservable = contentRepository.popularColumns();
        Observable<DomainResult<List<CategoryNodeBean>>> routeObservable = contentRepository.popularRoutes();

        autoDispose(Observable.zip(wendaObservable, columnObservable, routeObservable,
                        this::buildPopularSections)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(popularSections::setValue, throwable -> popularSections.setValue(Collections.emptyList())));
    }

    private List<HomePopularSection> buildPopularSections(DomainResult<List<ArticleListBean.Data>> wendaResult,
                                                         DomainResult<List<PopularColumnBean>> columnResult,
                                                         DomainResult<List<CategoryNodeBean>> routeResult) {
        List<HomePopularSection> sections = new ArrayList<>();

        if (wendaResult != null && wendaResult.isSuccess() && wendaResult.getData() != null) {
            List<CategoryNodeBean> items = mapWendaToChapters(wendaResult.getData());
            if (!items.isEmpty()) {
                sections.add(new HomePopularSection(BaseApplication.getAppContext().getString(R.string.discover_tab_wenda), items));
            }
        }

        if (columnResult != null && columnResult.isSuccess() && columnResult.getData() != null) {
            List<CategoryNodeBean> items = mapColumnsToChapters(columnResult.getData());
            if (!items.isEmpty()) {
                sections.add(new HomePopularSection(BaseApplication.getAppContext().getString(R.string.discover_hot_columns), items));
            }
        }

        if (routeResult != null && routeResult.isSuccess() && routeResult.getData() != null) {
            List<CategoryNodeBean> items = mapRoutesToChapters(routeResult.getData());
            if (!items.isEmpty()) {
                sections.add(new HomePopularSection(BaseApplication.getAppContext().getString(R.string.discover_tab_routes), items));
            }
        }

        return sections;
    }

    private List<CategoryNodeBean> mapWendaToChapters(List<ArticleListBean.Data> source) {
        List<CategoryNodeBean> items = new ArrayList<>();
        if (source == null) {
            return items;
        }
        int count = Math.min(source.size(), 8);
        for (int i = 0; i < count; i++) {
            ArticleListBean.Data data = source.get(i);
            if (data == null || data.getLink() == null || data.getLink().isEmpty()) {
                continue;
            }
            CategoryNodeBean bean = new CategoryNodeBean();
            bean.setId(data.getId());
            bean.setName(data.getTitle());
            bean.setLink(data.getLink());
            bean.setDesc(data.getAuthor() != null ? data.getAuthor() : data.getShareUser());
            items.add(bean);
        }
        return items;
    }

    private List<CategoryNodeBean> mapColumnsToChapters(List<PopularColumnBean> source) {
        List<CategoryNodeBean> items = new ArrayList<>();
        if (source == null) {
            return items;
        }
        int count = Math.min(source.size(), 8);
        for (int i = 0; i < count; i++) {
            PopularColumnBean data = source.get(i);
            if (data == null) {
                continue;
            }
            CategoryNodeBean bean = new CategoryNodeBean();
            bean.setId(data.getId());
            String title = !isNullOrEmpty(data.getName()) ? data.getName() : data.getSubChapterName();
            bean.setName(title);
            bean.setLink(resolveColumnUrl(data));
            String subtitle = !isNullOrEmpty(data.getChapterName()) ? data.getChapterName() : data.getSubChapterName();
            bean.setDesc(subtitle);
            items.add(bean);
        }
        return items;
    }

    private List<CategoryNodeBean> mapRoutesToChapters(List<CategoryNodeBean> source) {
        List<CategoryNodeBean> items = new ArrayList<>();
        if (source == null) {
            return items;
        }
        int count = Math.min(source.size(), 8);
        for (int i = 0; i < count; i++) {
            CategoryNodeBean node = source.get(i);
            if (node == null) {
                continue;
            }
            CategoryNodeBean bean = new CategoryNodeBean();
            bean.setId(node.getId());
            bean.setName(node.getName());
            bean.setDesc(node.getDesc());
            bean.setLink(resolveRouteLink(node));
            bean.setChildren(node.getChildren());
            items.add(bean);
        }
        return items;
    }

    private String resolveRouteLink(CategoryNodeBean node) {
        if (node == null) {
            return "";
        }
        if (node.getLink() != null && !node.getLink().isEmpty()) {
            return node.getLink();
        }
        if (node.getChildren() == null) {
            return "";
        }
        for (CategoryNodeBean child : node.getChildren()) {
            if (child != null && child.getLink() != null && !child.getLink().isEmpty()) {
                return child.getLink();
            }
        }
        return "";
    }

    private String resolveColumnUrl(PopularColumnBean data) {
        if (data == null) {
            return "";
        }
        if (!isNullOrEmpty(data.getUrl())) {
            return data.getUrl();
        }
        if (data.getId() > 0) {
            return "https://www.wanandroid.com/column/detail/" + data.getId();
        }
        if (data.getColumnId() > 0) {
            return "https://www.wanandroid.com/column/list/" + data.getColumnId();
        }
        return "";
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
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
                    if (error != null) {
                        return DomainResult.failure(error);
                    }
                    return DomainResult.failure(DomainError.of(DomainError.UNKNOWN_CODE,
                            BaseApplication.getAppContext().getString(R.string.home_article_load_failed)));
                });
    }
}
