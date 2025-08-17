package com.example.reandroid.ui.fragment.explore;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;

import com.example.core.base.BaseFragment;
import com.example.reandroid.databinding.FragmentExploreBinding;

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
        binding.tvTitle.setText("发现");
    }
}