package com.example.reandroid.ui.activity.main.fragment;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;

import com.example.core.base.BaseFragment;
import com.example.reandroid.databinding.FragmentHomeBinding;

public class HomeFragment extends BaseFragment<HomeViewModel, FragmentHomeBinding> {
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
        binding.tvTitle.setText("首页");
    }
}