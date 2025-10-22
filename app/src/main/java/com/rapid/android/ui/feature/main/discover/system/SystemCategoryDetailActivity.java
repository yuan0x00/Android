package com.rapid.android.ui.feature.main.discover.system;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.rapid.android.R;
import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.core.domain.model.CategoryNodeBean;
import com.rapid.android.core.ui.presentation.BaseActivity;
import com.rapid.android.databinding.ActivitySystemCategoryDetailBinding;
import com.rapid.android.ui.common.ContentStateController;
import com.rapid.android.ui.common.UiFeedback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SystemCategoryDetailActivity extends BaseActivity<SystemViewModel, ActivitySystemCategoryDetailBinding> {

    private static final String EXTRA_CATEGORY_ID = "extra_category_id";
    private static final String EXTRA_CATEGORY_TITLE = "extra_category_title";
    private static final String EXTRA_CHILD_POSITION = "extra_child_position";
    private static final String EXTRA_CHILD_IDS = "extra_child_ids";
    private static final String EXTRA_CHILD_NAMES = "extra_child_names";
    private final List<CategoryNodeBean> categories = new ArrayList<>();
    private ContentStateController stateController;
    private SystemCategoryChildPagerAdapter pagerAdapter;
    private TabLayoutMediator tabMediator;
    private int requestedCategoryId;
    private int pendingChildIndex;
    private final ViewPager2.OnPageChangeCallback pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            pendingChildIndex = position;
            selectTab(position);
        }
    };
    private boolean shouldReapplyChildSelection = true;
    private String initialCategoryTitle;
    private ArrayList<Integer> preloadChildIds = new ArrayList<>();
    private ArrayList<String> preloadChildNames = new ArrayList<>();

    public static void start(@NonNull Context context,
                              int categoryId,
                              @NonNull String categoryTitle,
                              int childPosition,
                              @NonNull ArrayList<Integer> childIds,
                              @NonNull ArrayList<String> childNames) {
        Intent intent = new Intent(context, SystemCategoryDetailActivity.class);
        intent.putExtra(EXTRA_CATEGORY_ID, categoryId);
        intent.putExtra(EXTRA_CATEGORY_TITLE, categoryTitle);
        intent.putExtra(EXTRA_CHILD_POSITION, childPosition);
        intent.putIntegerArrayListExtra(EXTRA_CHILD_IDS, childIds);
        intent.putStringArrayListExtra(EXTRA_CHILD_NAMES, childNames);
        context.startActivity(intent);
    }

    @Override
    protected SystemViewModel createViewModel() {
        return new ViewModelProvider(this).get(SystemViewModel.class);
    }

    @Override
    protected ActivitySystemCategoryDetailBinding createViewBinding() {
        return ActivitySystemCategoryDetailBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initializeViews() {
        // 安全地接收 Intent 参数
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        requestedCategoryId = intent.getIntExtra(EXTRA_CATEGORY_ID, -1);
        initialCategoryTitle = intent.getStringExtra(EXTRA_CATEGORY_TITLE);
        pendingChildIndex = intent.getIntExtra(EXTRA_CHILD_POSITION, 0);
        pendingChildIndex = Math.max(0, pendingChildIndex);

        // 安全获取数组参数，避免空指针
        ArrayList<Integer> extraIds = intent.getIntegerArrayListExtra(EXTRA_CHILD_IDS);
        ArrayList<String> extraNames = intent.getStringArrayListExtra(EXTRA_CHILD_NAMES);
        if (extraIds != null && !extraIds.isEmpty()) {
            preloadChildIds = new ArrayList<>(extraIds);
        }
        if (extraNames != null && !extraNames.isEmpty()) {
            preloadChildNames = new ArrayList<>(extraNames);
        }

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        stateController = new ContentStateController(binding.swipeRefresh, binding.progressBar, binding.emptyView);

        pagerAdapter = new SystemCategoryChildPagerAdapter(this);
        binding.viewPager.setAdapter(pagerAdapter);
        binding.viewPager.registerOnPageChangeCallback(pageChangeCallback);

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadSystem(true));
        binding.swipeRefresh.setOnChildScrollUpCallback((parent, child) -> {
            SystemCategoryChildFragment fragment = getCurrentChildFragment();
            return fragment != null && fragment.canScrollUp();
        });

        tabMediator = new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    CategoryNodeBean child = pagerAdapter.getItem(position);
                    tab.setText(child != null ? child.getName() : "");
                });
        tabMediator.attach();

        // 仅在有预加载数据时初始化 UI，避免重复提交导致闪动
        List<CategoryNodeBean> preloadedChildren = buildChildrenFromExtras(preloadChildIds, preloadChildNames);
        if (!preloadedChildren.isEmpty()) {
            pagerAdapter.submitChildren(preloadedChildren);
            binding.viewPager.setVisibility(View.VISIBLE);
            stateController.setEmpty(false);
            // 延迟设置初始页面，避免与 ViewPager2 初始化冲突
            binding.viewPager.post(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    int safeIndex = Math.max(0, Math.min(pendingChildIndex, preloadedChildren.size() - 1));
                    binding.viewPager.setCurrentItem(safeIndex, false);
                    selectTab(safeIndex);
                }
            });
        } else {
            binding.viewPager.setVisibility(View.GONE);
            stateController.setEmpty(true);
        }

        updateToolbarTitle(null);
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
            handleCategories();
        });

        viewModel.getLoading().observe(this, loading ->
                stateController.setLoading(Boolean.TRUE.equals(loading)));

        UiFeedback.observeError(this, provideDialogController(), viewModel.getErrorMessage());
    }

    private void handleCategories() {
        ActivitySystemCategoryDetailBinding currentBinding = this.binding;
        if (currentBinding == null || isFinishing() || isDestroyed()) {
            return;
        }

        if (categories.isEmpty()) {
            // 如果没有预加载数据，才设置为空状态
            if (pagerAdapter.getItemCount() == 0) {
                stateController.setEmpty(true);
                currentBinding.viewPager.setVisibility(View.GONE);
            }
            updateToolbarTitle(null);
            return;
        }

        CategoryNodeBean parent = findParentCategory();
        if (parent == null) {
            parent = categories.get(0);
        }

        if (TextUtils.isEmpty(initialCategoryTitle) && parent != null) {
            initialCategoryTitle = parent.getName();
        }

        List<CategoryNodeBean> children = resolveChildren(parent);

        // 检查新数据是否与预加载数据相同，避免重复提交
        boolean needsUpdate = true;
        int adapterCount = pagerAdapter.getItemCount();
        if (adapterCount == children.size()) {
            needsUpdate = false;
            for (int i = 0; i < children.size(); i++) {
                CategoryNodeBean newChild = children.get(i);
                CategoryNodeBean oldChild = pagerAdapter.getItem(i);
                if (newChild == null || oldChild == null || newChild.getId() != oldChild.getId()) {
                    needsUpdate = true;
                    break;
                }
            }
        }

        // 只在数据确实改变时才更新
        if (needsUpdate) {
            pagerAdapter.submitChildren(children);
        }

        boolean hasChildren = !children.isEmpty();
        currentBinding.viewPager.setVisibility(hasChildren ? View.VISIBLE : View.GONE);
        stateController.setEmpty(!hasChildren);

        if (hasChildren && shouldReapplyChildSelection) {
            // 第一次加载完整数据，需要重新应用子项选择
            applyChildSelection(children.size());
            shouldReapplyChildSelection = false;
        } else if (hasChildren && needsUpdate) {
            // 数据发生了变化（如刷新），需要验证当前位置是否还有效
            int currentItem = currentBinding.viewPager.getCurrentItem();
            if (currentItem >= children.size()) {
                pendingChildIndex = children.size() - 1;
                applyChildSelection(children.size());
            } else {
                // 当前位置有效，只需确保 Tab 同步
                selectTab(currentItem);
            }
        }
        // 如果数据没有变化且不需要重新选择，不做任何操作，保持当前状态

        updateToolbarTitle(parent);
    }

    private void applyChildSelection(int childCount) {
        ActivitySystemCategoryDetailBinding currentBinding = this.binding;
        if (childCount <= 0 || currentBinding == null || isFinishing() || isDestroyed()) {
            return;
        }
        pendingChildIndex = Math.max(0, Math.min(pendingChildIndex, childCount - 1));
        final int target = pendingChildIndex;
        ViewPager2 viewPager = currentBinding.viewPager;
        viewPager.post(() -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            viewPager.setCurrentItem(target, false);
        });
        selectTab(target);
    }

    private void selectTab(int position) {
        ActivitySystemCategoryDetailBinding currentBinding = this.binding;
        if (currentBinding == null || isFinishing() || isDestroyed()) {
            return;
        }
        TabLayout tabLayout = currentBinding.tabLayout;
        tabLayout.post(() -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            TabLayout.Tab tab = tabLayout.getTabAt(position);
            if (tab != null && !tab.isSelected()) {
                tab.select();
            }
        });
    }

    private CategoryNodeBean findParentCategory() {
        if (requestedCategoryId <= 0) {
            return categories.isEmpty() ? null : categories.get(0);
        }
        for (CategoryNodeBean category : categories) {
            if (category != null && category.getId() == requestedCategoryId) {
                return category;
            }
        }
        return categories.isEmpty() ? null : categories.get(0);
    }

    private SystemCategoryChildFragment getCurrentChildFragment() {
        ActivitySystemCategoryDetailBinding currentBinding = this.binding;
        if (currentBinding == null) {
            return null;
        }
        int position = currentBinding.viewPager.getCurrentItem();
        long itemId = pagerAdapter.getItemId(position);
        String tag = SystemCategoryChildPagerAdapter.createFragmentTag(currentBinding.viewPager.getId(), itemId);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment instanceof SystemCategoryChildFragment) {
            return (SystemCategoryChildFragment) fragment;
        }
        return null;
    }

    private void updateToolbarTitle(@Nullable CategoryNodeBean parent) {
        if (getSupportActionBar() == null) {
            return;
        }
        String title = !TextUtils.isEmpty(initialCategoryTitle)
                ? initialCategoryTitle
                : parent != null ? parent.getName() : null;
        if (TextUtils.isEmpty(title)) {
            title = getString(R.string.discover_tab_system);
        }
        getSupportActionBar().setTitle(title);
    }

    private List<CategoryNodeBean> buildChildrenFromExtras(List<Integer> childIds, List<String> childNames) {
        if (childIds == null || childNames == null || childIds.isEmpty()) {
            return Collections.emptyList();
        }
        int size = Math.min(childIds.size(), childNames.size());
        List<CategoryNodeBean> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            CategoryNodeBean child = new CategoryNodeBean();
            child.setId(childIds.get(i));
            child.setName(childNames.get(i));
            result.add(child);
        }
        return result;
    }

    private List<CategoryNodeBean> resolveChildren(CategoryNodeBean parent) {
        if (parent == null) {
            return Collections.emptyList();
        }
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
        return Collections.singletonList(single);
    }

    @Override
    protected void onDestroy() {
        if (tabMediator != null) {
            tabMediator.detach();
            tabMediator = null;
        }
        binding.viewPager.unregisterOnPageChangeCallback(pageChangeCallback);
        super.onDestroy();
    }
}
