package com.example.reandroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import com.example.reandroid.base.BaseActivity;
import com.example.reandroid.databinding.ActivityMainBinding;

public class MainActivity<ActivityMainBinding, MainViewModel> extends BaseActivity {
    @Override
    protected ActivityMainBinding inflateViewBinding(@NonNull LayoutInflater inflater) {
        return ActivityMainBinding.inflate(inflater);
    }
    @Override
    protected Class<MainViewModel> getViewModelClass() {
        return MainViewModel.class;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}