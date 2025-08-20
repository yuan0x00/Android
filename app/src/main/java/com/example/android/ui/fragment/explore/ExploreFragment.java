package com.example.android.ui.fragment.explore;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;

import com.example.android.databinding.FragmentExploreBinding;
import com.example.android.ui.dialog.TipDialogFragment;
import com.example.core.base.BaseFragment;

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
        binding.tvTitle.setOnClickListener((v -> new TipDialogFragment()
                .size(300, 200)
                .gravity(Gravity.CENTER)
                .cancelable(true)
                .show(getParentFragmentManager())));
    }
}