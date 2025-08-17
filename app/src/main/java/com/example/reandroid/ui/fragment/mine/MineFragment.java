package com.example.reandroid.ui.fragment.mine;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;

import com.example.core.base.BaseFragment;
import com.example.reandroid.databinding.FragmentMineBinding;

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
        binding.tvTitle.setText("我的");
    }
}