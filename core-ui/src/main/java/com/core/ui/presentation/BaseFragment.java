package com.core.ui.presentation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.viewbinding.ViewBinding;

public abstract class BaseFragment<VM extends ViewModel, VB extends ViewBinding> extends Fragment {

    protected VM viewModel;
    protected VB binding;
    private boolean isDataLoaded = false;
    private boolean isFirstVisible = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = createViewModel();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = createViewBinding(inflater, container);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews();
        setupObservers();
        if (!isLazyLoad()) {
            loadData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        onVisible();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isDataLoaded = false;
        isFirstVisible = true;
    }

    protected abstract VM createViewModel();

    protected abstract VB createViewBinding(LayoutInflater inflater, ViewGroup container);

    protected void initializeViews() {
    }

    protected void setupObservers() {
    }

    protected void loadData() {
    }

    protected boolean isLazyLoad() {
        return false;
    }

    private void onVisible() {
        if (isLazyLoad() && !isDataLoaded && isVisible()) {
            if (isFirstVisible) {
                loadData();
                isFirstVisible = false;
            }
            isDataLoaded = true;
        }
    }
}