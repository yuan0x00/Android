package com.example.reandroid.ui.fragment.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.core.base.BaseFragment;
import com.example.core.utils.SafeAreaUtils;
import com.example.reandroid.databinding.FragmentHomeBinding;
import com.example.reandroid.ui.activity.main.TabNavigator;
import com.example.reandroid.ui.fragment.home.recycleView.adapter.BannerAdapter;
import com.example.reandroid.ui.fragment.home.recycleView.adapter.EntriesAdapter;
import com.example.reandroid.ui.fragment.home.recycleView.adapter.FeedAdapter;
import com.example.reandroid.ui.fragment.home.recycleView.item.BannerItem;
import com.example.reandroid.ui.fragment.home.recycleView.item.EntryItem;
import com.example.reandroid.ui.fragment.home.recycleView.item.FeedItem;

import java.util.Arrays;
import java.util.List;

public class HomeFragment extends BaseFragment<HomeViewModel, FragmentHomeBinding> {
    private TabNavigator navigator;

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
                new EntryItem("商城", android.R.drawable.ic_menu_agenda),
                new EntryItem("订单", android.R.drawable.ic_menu_camera),
                new EntryItem("客服", android.R.drawable.ic_menu_call),
                new EntryItem("我的", android.R.drawable.ic_menu_my_calendar)
        );

        List<FeedItem> feeds = Arrays.asList(
                new FeedItem("欢迎来到首页，这里是你最新的动态..."),
                new FeedItem("今日推荐商品已更新，请查看。"),
                new FeedItem("系统通知：版本已升级至 v2.0")
        );

        // 创建各模块 Adapter
        BannerAdapter bannerAdapter = new BannerAdapter(bannerUrls);
        EntriesAdapter entriesAdapter = new EntriesAdapter(entries);
        FeedAdapter feedAdapter = new FeedAdapter(feeds);

        // 使用 ConcatAdapter 拼接
        ConcatAdapter concatAdapter = new ConcatAdapter(bannerAdapter, entriesAdapter, feedAdapter);
        binding.recyclerView.setAdapter(concatAdapter);
    }

}