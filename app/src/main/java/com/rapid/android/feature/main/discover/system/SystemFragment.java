package com.rapid.android.feature.main.discover.system;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.core.domain.model.CategoryNodeBean;
import com.rapid.android.core.ui.presentation.BaseFragment;
import com.rapid.android.databinding.FragmentSystemBinding;
import com.rapid.android.ui.common.ContentStateController;
import com.rapid.android.ui.common.RecyclerViewDecorations;

import java.util.ArrayList;
import java.util.List;

public class SystemFragment extends BaseFragment<SystemViewModel, FragmentSystemBinding> {

    private final List<CategoryNodeBean> categories = new ArrayList<>();
    private ContentStateController stateController;
    private SystemCategoryAdapter adapter;

    @Override
    protected SystemViewModel createViewModel() {
        return new ViewModelProvider(this).get(SystemViewModel.class);
    }

    @Override
    protected FragmentSystemBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentSystemBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initializeViews() {
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadSystem(true));

        stateController = new ContentStateController(binding.swipeRefresh, binding.progressBar, binding.emptyView);

        adapter = new SystemCategoryAdapter(new SystemCategoryAdapter.OnCategoryClickListener() {
            @Override
            public void onCategoryClick(@NonNull CategoryNodeBean category, int position) {
                // 点击卡片时跳转到第一个子项
                launchCategoryDetail(category, 0);
            }

            @Override
            public void onChildClick(@NonNull CategoryNodeBean parent, @NonNull CategoryNodeBean child,
                                     int parentPosition, int childPosition) {
                launchCategoryDetail(parent, childPosition);
            }
        });
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
        RecyclerViewDecorations.addSpacing(binding.recyclerView);

    }

    private void launchCategoryDetail(@NonNull CategoryNodeBean category, int childPosition) {
        ArrayList<CategoryNodeBean> children = new ArrayList<>(resolveChildren(category));
        ArrayList<Integer> childIds = new ArrayList<>();
        ArrayList<String> childNames = new ArrayList<>();
        for (CategoryNodeBean child : children) {
            if (child == null) {
                continue;
            }
            childIds.add(child.getId());
            childNames.add(child.getName());
        }
        SystemCategoryDetailActivity.start(
                requireContext(),
                category.getId(),
                category.getName(),
                childPosition,
                childIds,
                childNames
        );
    }

    private List<CategoryNodeBean> resolveChildren(@NonNull CategoryNodeBean parent) {
        List<CategoryNodeBean> children = parent.getChildren();
        if (children != null && !children.isEmpty()) {
            return children;
        }

        CategoryNodeBean single = new CategoryNodeBean();
        single.setId(parent.getId());
        single.setName(parent.getName());
        single.setDesc(parent.getDesc());
        single.setLink(parent.getLink());
        single.setLisenseLink(parent.getLisenseLink());
        single.setAuthor(parent.getAuthor());
        single.setCover(parent.getCover());
        single.setOrder(parent.getOrder());
        single.setParentChapterId(parent.getParentChapterId());
        single.setCourseId(parent.getCourseId());
        single.setType(parent.getType());
        List<ArticleListBean.Data> articleList = parent.getArticleList();
        if (articleList != null) {
            single.setArticleList(new ArrayList<>(articleList));
        }
        ArrayList<CategoryNodeBean> fallback = new ArrayList<>();
        fallback.add(single);
        return fallback;
    }

    @Override
    protected void loadData() {
        viewModel.loadSystem(false);
    }

    @Override
    protected void setupObservers() {
        viewModel.getCategories().observe(this, list -> {
            categories.clear();
            if (list != null) {
                categories.addAll(list);
            }

            stateController.setEmpty(categories.isEmpty());
            adapter.submitList(new ArrayList<>(categories));
        });

        viewModel.getLoading().observe(this, loading ->
                stateController.setLoading(Boolean.TRUE.equals(loading)));

    }

    @Override
    public void onDestroyView() {
        binding.recyclerView.setAdapter(null);
        adapter = null;
        super.onDestroyView();
    }
}
