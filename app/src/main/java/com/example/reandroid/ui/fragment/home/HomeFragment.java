package com.example.reandroid.ui.fragment.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.example.core.base.BaseFragment;
import com.example.core.utils.SafeAreaUtils;
import com.example.reandroid.databinding.FragmentHomeBinding;
import com.example.reandroid.ui.activity.main.TabNavigator;
import com.example.reandroid.ui.view.ImageBanner;

import java.util.Arrays;
import java.util.List;

public class HomeFragment extends BaseFragment<HomeViewModel, FragmentHomeBinding> {
    private TabNavigator navigator;
    private ImageBanner banner;

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
        binding.tvTitle.setText("首页");
        binding.tvTitle.setOnClickListener((view) -> navigator.navigateTo(2));
        banner = binding.banner;
        List<String> urls = Arrays.asList(
                "https://public.ysjf.com/content/title-image/Y7IoA2mGtho.jpg",
                "https://public.ysjf.com/product/preview/Y9s2dgnGrU.jpg",
                "https://public.ysjf.com/product/preview/cT9mqPVWS6.jpg"
        );
        banner.setData(urls);
    }

    @Override
    public void onPause() {
        super.onPause();
        banner.stopAutoScroll();
    }

    @Override
    public void onResume() {
        super.onResume();
        banner.startAutoScroll();
    }
}