package com.rapid.android.core.ui.presentation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import com.rapid.android.core.ui.components.dialog.DialogController;
import com.rapid.android.core.ui.components.dialog.DialogHost;
import com.rapid.android.core.ui.components.dialog.ScopedDialogHost;

import org.jetbrains.annotations.NotNull;

public abstract class BaseFragment<VM extends BaseViewModel, VB extends ViewBinding> extends Fragment implements DialogHost, ScopedDialogHost {

    protected VM viewModel;
    protected VB binding;
    private DialogController dialogController;
    private DialogController scopedDialogController;

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
        dialogController = DialogController.from(requireActivity(), binding.getRoot());
        scopedDialogController = resolveScopedDialogController();
        initializeViews();
        setupObservers();
        loadData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        dialogController = null;
        scopedDialogController = null;
    }

    protected abstract VM createViewModel();

    protected abstract VB createViewBinding(LayoutInflater inflater, ViewGroup container);

    protected void initializeViews() {
    }

    protected void setupObservers() {
    }

    protected void loadData() {
    }

    @Override
    public @NotNull DialogController getDialogController() {
        if (dialogController == null) {
            throw new IllegalStateException("DialogController is not available after view destruction.");
        }
        return dialogController;
    }

    @Override
    public @NotNull DialogController provideDialogController() {
        if (scopedDialogController == null) {
            throw new IllegalStateException("DialogController is not available after view destruction.");
        }
        return scopedDialogController;
    }

    private DialogController resolveScopedDialogController() {
        Fragment parent = getParentFragment();
        while (parent != null) {
            if (parent instanceof ScopedDialogHost) {
                return ((ScopedDialogHost) parent).provideDialogController();
            }
            parent = parent.getParentFragment();
        }

        if (requireActivity() instanceof ScopedDialogHost) {
            return ((ScopedDialogHost) requireActivity()).provideDialogController();
        }

        return dialogController;
    }
}
