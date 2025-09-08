package com.rapid.android.viewmodel;

import androidx.lifecycle.MutableLiveData;

import com.rapid.core.base.vm.BaseViewModel;


public class MainViewModel extends BaseViewModel {
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

}
