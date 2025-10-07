package com.rapid.android.ui.feature.share;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.rapid.android.R;
import com.rapid.android.core.common.utils.ToastUtils;
import com.rapid.android.core.ui.presentation.BaseActivity;
import com.rapid.android.databinding.ActivityShareArticleBinding;
import com.rapid.android.ui.common.UiFeedback;

public class ShareArticleActivity extends BaseActivity<ShareArticleViewModel, ActivityShareArticleBinding> {

    private TextWatcher formWatcher;

    public static void start(@NonNull Context context) {
        context.startActivity(new Intent(context, ShareArticleActivity.class));
    }

    @Override
    protected ShareArticleViewModel createViewModel() {
        return new ViewModelProvider(this).get(ShareArticleViewModel.class);
    }

    @Override
    protected ActivityShareArticleBinding createViewBinding() {
        return ActivityShareArticleBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initializeViews() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        formWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.updateForm(binding.titleInput.getText() != null ? binding.titleInput.getText().toString() : "",
                        binding.linkInput.getText() != null ? binding.linkInput.getText().toString() : "");
            }
        };

        binding.titleInput.addTextChangedListener(formWatcher);
        binding.linkInput.addTextChangedListener(formWatcher);
        binding.linkInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptSubmit();
                return true;
            }
            return false;
        });

        binding.submitButton.setOnClickListener(v -> attemptSubmit());
    }

    @Override
    protected void setupObservers() {
        viewModel.getSubmitEnabled().observe(this, enabled -> binding.submitButton.setEnabled(Boolean.TRUE.equals(enabled)));
        viewModel.getLoading().observe(this, loading -> binding.progressBar.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE));
        viewModel.getSubmitSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                ToastUtils.showShortToast(getString(R.string.share_article_success));
                finish();
            }
        });
        UiFeedback.observeError(this, viewModel.getErrorMessage());
    }

    @Override
    protected void onDestroy() {
        if (formWatcher != null) {
            binding.titleInput.removeTextChangedListener(formWatcher);
            binding.linkInput.removeTextChangedListener(formWatcher);
            formWatcher = null;
        }
        super.onDestroy();
    }

    private void attemptSubmit() {
        viewModel.submit();
    }
}
