package com.example.reandroid.ui.activity.main.fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.example.core.base.BaseFragment;
import com.example.reandroid.databinding.FragmentHomeBinding;
import com.example.reandroid.ui.activity.main.TabNavigator;

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
        binding.tvTitle.setText("首页");
        binding.tvTitle.setOnClickListener((view) -> navigator.navigateTo(2));
    }
}