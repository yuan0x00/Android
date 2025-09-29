package com.rapid.android.presentation.ui.activity;

import androidx.lifecycle.ViewModelProvider;

import com.core.base.ui.BaseActivity;
import com.rapid.android.databinding.ActivitySearchBinding;
import com.rapid.android.presentation.viewmodel.SearchViewModel;

public class SearchActivity extends BaseActivity<SearchViewModel, ActivitySearchBinding> {

    @Override
    protected SearchViewModel createViewModel() {
        return new ViewModelProvider(this).get(SearchViewModel.class);
    }

    @Override
    protected ActivitySearchBinding createViewBinding() {
        return ActivitySearchBinding.inflate(getLayoutInflater());

    }

}