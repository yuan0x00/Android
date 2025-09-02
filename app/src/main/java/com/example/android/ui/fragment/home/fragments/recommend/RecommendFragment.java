package com.example.android.ui.fragment.home.fragments.recommend;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.bean.ArticleListBean;
import com.example.android.bean.BannerItemBean;
import com.example.android.databinding.FragmentRecommandBinding;
import com.example.android.ui.fragment.home.ITopLayoutHandler;
import com.example.android.ui.fragment.home.fragments.recommend.adapter.BannerAdapter;
import com.example.android.ui.fragment.home.fragments.recommend.adapter.FeedAdapter;
import com.example.core.base.BaseFragment;
import com.example.core.utils.DisplayUtils;
import com.example.core.utils.ToastUtils;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;

import java.util.Arrays;
import java.util.List;


public class RecommendFragment extends BaseFragment<RecommendViewModel, FragmentRecommandBinding> {

    private BannerAdapter bannerAdapter;
    private ITopLayoutHandler topLayoutHandler;
    private int scrollThreshold = 0;
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
                if (topLayoutHandler != null) {
                    topLayoutHandler.setTopLayoutCollapsed(false);
                }
                accumulatedDy = 0; // 重置累计距离
                return;
            }

            // 只有当累计距离超过阈值且无动画运行时触发
            // 滚动阈值
            if (Math.abs(accumulatedDy) >= scrollThreshold) {
                if (accumulatedDy > 0) {
                    // 向下滑动 - 折叠
                    if (topLayoutHandler != null) {
                        topLayoutHandler.setTopLayoutCollapsed(true);
                    }
                    accumulatedDy = 0; // 重置
                } else if (accumulatedDy < 0) {
                    // 向上滑动 - 展开
                    if (topLayoutHandler != null) {
                        topLayoutHandler.setTopLayoutCollapsed(false);
                    }
                    accumulatedDy = 0; // 重置
                }
            }
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
    private FeedAdapter feedAdapter;

    public RecommendFragment setTopLayoutHandler(ITopLayoutHandler topLayoutHandler) {
        this.topLayoutHandler = topLayoutHandler;
        return this;
    }

    @Override
    protected RecommendViewModel createViewModel() {
        return new ViewModelProvider(this).get(RecommendViewModel.class);
    }

    @Override
    protected FragmentRecommandBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentRecommandBinding.inflate(inflater, container, false);
    }

    @Override
    protected void loadData() {
        super.loadData();
        viewModel.banner();
        viewModel.articleList();
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
        viewModel.getArticleListBean().observe(this, bean -> {
            if (bean != null) {
                feedAdapter.setData(bean);
            }
        });
    }

    @Override
    protected void initializeViews() {

        // 初始化 RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 模拟数据
        List<BannerItemBean> bannerUrls = Arrays.asList(
                new BannerItemBean("https://public.ysjf.com/content/title-image/Y7IoA2mGtho.jpg"),
                new BannerItemBean("https://public.ysjf.com/product/preview/Y9s2dgnGrU.jpg"),
                new BannerItemBean("https://public.ysjf.com/product/preview/cT9mqPVWS6.jpg")
        );

        ArticleListBean feeds = new ArticleListBean();

        // 创建各模块 Adapter
        bannerAdapter = new BannerAdapter(bannerUrls);
        feedAdapter = new FeedAdapter(feeds);

        scrollThreshold = DisplayUtils.dp2px(60);
        // 使用 ConcatAdapter 拼接
        ConcatAdapter concatAdapter = new ConcatAdapter(bannerAdapter, feedAdapter);
        binding.recyclerView.setAdapter(concatAdapter);
        binding.recyclerView.addOnScrollListener(scrollListener);

        //设置 Header
        binding.refreshLayout.setRefreshHeader(new ClassicsHeader(requireContext()));
        //设置 Footer
        binding.refreshLayout.setRefreshFooter(new ClassicsFooter(requireContext()));

        binding.refreshLayout.setOnRefreshListener((refreshlayout) -> refreshlayout.finishRefresh(2000));
        binding.refreshLayout.setOnLoadMoreListener(refreshlayout -> refreshlayout.finishLoadMore(2000));

    }
}