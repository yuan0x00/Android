package com.example.android.ui.fragment.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;

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
    private ConcatAdapter concatAdapter;

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
                new FeedItem("系统通知：版本已升级至 v2.0。")
        );

        // 创建各模块 Adapter
        bannerAdapter = new BannerAdapter(bannerUrls);
        EntriesAdapter entriesAdapter = new EntriesAdapter(entries);
        FeedAdapter feedAdapter = new FeedAdapter(feeds);

        // 使用 ConcatAdapter 拼接
        concatAdapter = new ConcatAdapter(bannerAdapter, entriesAdapter, feedAdapter);
        binding.recyclerView.setAdapter(concatAdapter);

        //设置 Header
        binding.refreshLayout.setRefreshHeader(new ClassicsHeader(requireContext()));
        //设置 Footer
        binding.refreshLayout.setRefreshFooter(new ClassicsFooter(requireContext()));

        binding.refreshLayout.setOnRefreshListener((refreshlayout) -> {
            refreshlayout.finishRefresh(2000);
        });
        binding.refreshLayout.setOnLoadMoreListener(refreshlayout -> {
            refreshlayout.finishLoadMore(2000);
        });

    }

}