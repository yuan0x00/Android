package com.rapid.android.ui.fragment;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;

import com.rapid.android.databinding.FragmentExploreBinding;
import com.rapid.android.viewmodel.ExploreViewModel;
import com.rapid.core.base.ui.BaseFragment;

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

    }

}