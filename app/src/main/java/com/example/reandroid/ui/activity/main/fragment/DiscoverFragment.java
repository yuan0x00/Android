package com.example.reandroid.ui.activity.main.fragment;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;

import com.example.core.base.BaseFragment;
import com.example.reandroid.databinding.FragmentDiscoverBinding;

public class DiscoverFragment extends BaseFragment<DiscoverViewModel, FragmentDiscoverBinding> {
    @Override
    protected DiscoverViewModel createViewModel() {
        return new ViewModelProvider(this).get(DiscoverViewModel.class);
    }

    @Override
    protected FragmentDiscoverBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentDiscoverBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initializeViews() {
        binding.tvTitle.setText("发现");
    }
}