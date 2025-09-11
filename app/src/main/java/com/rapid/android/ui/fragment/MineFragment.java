package com.rapid.android.ui.fragment;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;

import com.core.base.ui.BaseFragment;
import com.rapid.android.databinding.FragmentMineBinding;
import com.rapid.android.viewmodel.MineViewModel;

public class MineFragment extends BaseFragment<MineViewModel, FragmentMineBinding> {
    @Override
    protected MineViewModel createViewModel() {
        return new ViewModelProvider(this).get(MineViewModel.class);
    }

    @Override
    protected FragmentMineBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentMineBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initializeViews() {
    }
}