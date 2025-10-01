package com.rapid.android.ui.feature.main.home.search;

import androidx.lifecycle.ViewModelProvider;

import com.core.ui.presentation.BaseActivity;
import com.rapid.android.databinding.ActivitySearchBinding;

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