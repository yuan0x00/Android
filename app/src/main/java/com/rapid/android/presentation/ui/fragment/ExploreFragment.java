package com.rapid.android.presentation.ui.fragment;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;

import com.core.base.ui.BaseFragment;
import com.core.webview.WebViewFragment;
import com.rapid.android.databinding.FragmentExploreBinding;
import com.rapid.android.presentation.viewmodel.ExploreViewModel;

public class ExploreFragment extends BaseFragment<ExploreViewModel, FragmentExploreBinding> {

    @Override
    protected ExploreViewModel createViewModel() {
        return new ViewModelProvider(this).get(ExploreViewModel.class);
    }

    @Override
    protected FragmentExploreBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentExploreBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initializeViews() {
        setupWebViewFragment();
    }

    private void setupWebViewFragment() {
        // 创建WebViewFragment实例，加载百度
        WebViewFragment webViewFragment = WebViewFragment.newInstance("https://www.wanandroid.com");

        // 将WebViewFragment添加到容器中
        getChildFragmentManager()
                .beginTransaction()
                .replace(binding.webviewContainer.getId(), webViewFragment)
                .commit();
    }

}