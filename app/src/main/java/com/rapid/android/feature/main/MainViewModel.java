package com.rapid.android.feature.main;

import androidx.lifecycle.MutableLiveData;

import com.rapid.android.core.ui.presentation.BaseViewModel;

public class MainViewModel extends BaseViewModel {

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public MainViewModel() {
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

}
