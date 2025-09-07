package com.rapid.android.ui.dialog;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rapid.android.R;
import com.rapid.android.databinding.DialogTipBinding;
import com.rapid.core.base.ui.BaseDialogFragment;

import org.jetbrains.annotations.NotNull;

public class TipDialogFragment extends BaseDialogFragment {

    private String contentText;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_tip;
    }

    public TipDialogFragment setContentText(String text) {
        this.contentText = text;
        return this;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DialogTipBinding binding = DialogTipBinding.bind(view);
        binding.tvContent.setText(this.contentText);
    }
}