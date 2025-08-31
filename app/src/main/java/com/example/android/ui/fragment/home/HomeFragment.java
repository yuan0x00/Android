package com.example.android.ui.fragment.home;

import android.animation.*;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.R;
import com.example.android.databinding.FragmentHomeBinding;
import com.example.android.ui.activity.main.TabNavigator;
import com.example.android.ui.fragment.home.adapter.BannerAdapter;
import com.example.android.ui.fragment.home.adapter.EntriesAdapter;
import com.example.android.ui.fragment.home.adapter.FeedAdapter;
import com.example.android.ui.fragment.home.item.BannerItem;
import com.example.android.ui.fragment.home.item.EntryItem;
import com.example.android.ui.fragment.home.item.FeedItem;
import com.example.core.base.BaseFragment;
import com.example.core.utils.SafeAreaUtils;
import com.example.core.utils.ToastUtils;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;

import java.util.Arrays;
import java.util.List;

public class HomeFragment extends BaseFragment<HomeViewModel, FragmentHomeBinding> {
    private TabNavigator navigator;
    private BannerAdapter bannerAdapter;
    private boolean isCollapsed = false;
    private int originalHeight = 0;
    private int compactHeight = 0;
    private int scrollThreshold = 0; // 滚动阈值
    private AnimatorSet currentAnimatorSet = null; // 跟踪当前动画
    private final RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        private int accumulatedDy = 0; // 累计滑动距离

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            // 累计滑动距离
            accumulatedDy += dy;

            // 检查是否到达顶部
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager != null && layoutManager.findFirstVisibleItemPosition() == 0) {
                // 在顶部时确保展开，且重置累计距离
                if (isCollapsed && (currentAnimatorSet == null || !currentAnimatorSet.isRunning())) {
                    expandSearchArea();
                }
                accumulatedDy = 0; // 重置累计距离
                return;
            }

            // 只有当累计距离超过阈值且无动画运行时触发
            if (Math.abs(accumulatedDy) >= scrollThreshold && (currentAnimatorSet == null || !currentAnimatorSet.isRunning())) {
                if (accumulatedDy > 0 && !isCollapsed) {
                    // 向下滑动 - 折叠
                    collapseSearchArea();
                    accumulatedDy = 0; // 重置
                } else if (accumulatedDy < 0 && isCollapsed) {
                    // 向上滑动 - 展开
                    expandSearchArea();
                    accumulatedDy = 0; // 重置
                }
            }

            Log.d("HomeFragment-dy", String.valueOf(dy));
            Log.d("HomeFragment-accumulatedDy", String.valueOf(accumulatedDy));
        }

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            // 滑动停止时重置累计距离
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                accumulatedDy = 0;
            }
        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof TabNavigator) {
            this.navigator = (TabNavigator) context;
        } else {
            throw new IllegalStateException(context + " must implement TabNavigator");
        }
    }

    @Override
    protected HomeViewModel createViewModel() {
        return new ViewModelProvider(this).get(HomeViewModel.class);
    }

    @Override
    protected FragmentHomeBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentHomeBinding.inflate(inflater, container, false);
    }

    @Override
    protected void loadData() {
        super.loadData();
        viewModel.banner();
    }

    @Override
    protected void setupObservers() {
        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                ToastUtils.showLongToast(msg);
            }
        });
        viewModel.getBannerList().observe(this, bannerList -> {
            if (bannerList != null && !bannerList.isEmpty()) {
                bannerAdapter.setData(bannerList);
            }
        });
    }

    @Override
    protected void initializeViews() {
        SafeAreaUtils.applyTop(binding.getRoot());

        // 初始化 RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 模拟数据
        List<BannerItem> bannerUrls = Arrays.asList(
                new BannerItem("https://public.ysjf.com/content/title-image/Y7IoA2mGtho.jpg"),
                new BannerItem("https://public.ysjf.com/product/preview/Y9s2dgnGrU.jpg"),
                new BannerItem("https://public.ysjf.com/product/preview/cT9mqPVWS6.jpg")
        );

        List<EntryItem> entries = Arrays.asList(
                new EntryItem("商城", R.drawable.explore_24px),
                new EntryItem("订单", R.drawable.explore_24px),
                new EntryItem("客服", R.drawable.explore_24px),
                new EntryItem("我的", R.drawable.explore_24px)
        );

        List<FeedItem> feeds = Arrays.asList(
                new FeedItem("欢迎来到首页，这里是你最新的动态..."),
                new FeedItem("今日推荐商品1已更新，请查看。"),
                new FeedItem("今日推荐商品2已更新，请查看。"),
                new FeedItem("今日推荐商品3已更新，请查看。"),
                new FeedItem("今日推荐商品4已更新，请查看。"),
                new FeedItem("今日推荐商品5已更新，请查看。"),
                new FeedItem("今日推荐商品6已更新，请查看。"),
                new FeedItem("今日推荐商品7已更新，请查看。"),
                new FeedItem("今日推荐商品7已更新，请查看。"),
                new FeedItem("今日推荐商品7已更新，请查看。"),
                new FeedItem("今日推荐商品7已更新，请查看。"),
                new FeedItem("今日推荐商品7已更新，请查看。"),
                new FeedItem("今日推荐商品7已更新，请查看。"),
                new FeedItem("今日推荐商品7已更新，请查看。"),
                new FeedItem("今日推荐商品7已更新，请查看。"),
                new FeedItem("今日推荐商品7已更新，请查看。"),
                new FeedItem("今日推荐商品7已更新，请查看。"),
                new FeedItem("今日推荐商品7已更新，请查看。"),
                new FeedItem("今日推荐商品7已更新，请查看。"),
                new FeedItem("今日推荐商品7已更新，请查看。"),
                new FeedItem("今日推荐商品7已更新，请查看。"),
                new FeedItem("今日推荐商品7已更新，请查看。"),
                new FeedItem("今日推荐商品7已更新，请查看。"),
                new FeedItem("系统通知：版本已升级至 v2.0。")
        );

        // 创建各模块 Adapter
        bannerAdapter = new BannerAdapter(bannerUrls);
        EntriesAdapter entriesAdapter = new EntriesAdapter(entries);
        FeedAdapter feedAdapter = new FeedAdapter(feeds);

        // 使用 ConcatAdapter 拼接
        ConcatAdapter concatAdapter = new ConcatAdapter(bannerAdapter, entriesAdapter, feedAdapter);
        binding.recyclerView.setAdapter(concatAdapter);
        binding.recyclerView.addOnScrollListener(scrollListener);

        originalHeight = dpToPx(120);
        compactHeight = dpToPx(60);
        scrollThreshold = dpToPx(60);

        //设置 Header
        binding.refreshLayout.setRefreshHeader(new ClassicsHeader(requireContext()));
        //设置 Footer
        binding.refreshLayout.setRefreshFooter(new ClassicsFooter(requireContext()));

        binding.refreshLayout.setOnRefreshListener((refreshlayout) -> refreshlayout.finishRefresh(2000));
        binding.refreshLayout.setOnLoadMoreListener(refreshlayout -> refreshlayout.finishLoadMore(2000));

        binding.searchBar.setOnClickListener(v -> ToastUtils.showShortToast("搜索栏"));

        binding.tabBar.setOnClickListener(v -> ToastUtils.showShortToast("选项栏"));

    }

    private void collapseSearchArea() {
        if (isCollapsed || (currentAnimatorSet != null && currentAnimatorSet.isRunning())) {
            return; // 如果已折叠或动画正在运行，忽略
        }

        TextView searchBar = binding.searchBar;
        TextView tabBar = binding.tabBar;

        // 防止裁剪
        binding.searchArea.setClipChildren(false);

        // 动态获取搜索栏高度
        float searchHeight = searchBar.getHeight() > 0 ? searchBar.getHeight() : 60f;

        // 搜索栏动画：上移并淡出
        ObjectAnimator searchMoveUp = ObjectAnimator.ofFloat(searchBar, "translationY", 0f, -searchHeight);
        ObjectAnimator searchFadeOut = ObjectAnimator.ofFloat(searchBar, "alpha", 1f, 0.5f);

        // 标签栏动画：上移到搜索栏位置
        ObjectAnimator tabMoveUp = ObjectAnimator.ofFloat(tabBar, "translationY", 0f, -searchHeight);

        // 父布局高度动画
        ValueAnimator heightAnimator = ValueAnimator.ofInt(originalHeight, compactHeight);
        heightAnimator.addUpdateListener(animation -> {
            int height = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams params = binding.searchArea.getLayoutParams();
            params.height = height;
            binding.searchArea.setLayoutParams(params);
        });

        // 组合动画
        currentAnimatorSet = new AnimatorSet();
        currentAnimatorSet.playTogether(searchMoveUp, searchFadeOut, tabMoveUp, heightAnimator);
        currentAnimatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        currentAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isCollapsed = true;
                currentAnimatorSet = null; // 动画结束，清空引用
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                currentAnimatorSet = null; // 取消时清空
            }
        });
        currentAnimatorSet.setDuration(300);
        currentAnimatorSet.start();
    }

    private void expandSearchArea() {
        if (!isCollapsed || (currentAnimatorSet != null && currentAnimatorSet.isRunning())) {
            return; // 如果已展开或动画正在运行，忽略
        }

        TextView searchBar = binding.searchBar;
        TextView tabBar = binding.tabBar;

        // 防止裁剪
        binding.searchArea.setClipChildren(false);

        // 动态获取搜索栏高度
        float searchHeight = searchBar.getHeight() > 0 ? searchBar.getHeight() : 60f;

        // 初始化状态
        searchBar.setTranslationY(-searchHeight);
        searchBar.setAlpha(0.5f);
        tabBar.setTranslationY(-searchHeight);

        // 搜索栏动画：下移并淡入
        ObjectAnimator searchMoveDown = ObjectAnimator.ofFloat(searchBar, "translationY", -searchHeight, 0f);
        ObjectAnimator searchFadeIn = ObjectAnimator.ofFloat(searchBar, "alpha", 0.5f, 1f);

        // 标签栏动画：下移到原始位置
        ObjectAnimator tabMoveDown = ObjectAnimator.ofFloat(tabBar, "translationY", -searchHeight, 0f);

        // 父布局高度动画
        ValueAnimator heightAnimator = ValueAnimator.ofInt(compactHeight, originalHeight);
        heightAnimator.addUpdateListener(animation -> {
            int height = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams params = binding.searchArea.getLayoutParams();
            params.height = height;
            binding.searchArea.setLayoutParams(params);
        });

        // 组合动画
        currentAnimatorSet = new AnimatorSet();
        currentAnimatorSet.playTogether(searchMoveDown, searchFadeIn, tabMoveDown, heightAnimator);
        currentAnimatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        currentAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isCollapsed = false;
                currentAnimatorSet = null; // 动画结束，清空引用
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                currentAnimatorSet = null; // 取消时清空
            }
        });
        currentAnimatorSet.setDuration(300);
        currentAnimatorSet.start();
    }

    // dp转px工具方法
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

}