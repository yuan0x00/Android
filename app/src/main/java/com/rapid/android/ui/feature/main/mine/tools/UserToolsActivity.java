package com.rapid.android.ui.feature.main.mine.tools;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rapid.android.R;
import com.rapid.android.core.domain.model.UserToolBean;
import com.rapid.android.core.ui.presentation.BaseActivity;
import com.rapid.android.core.ui.utils.ToastUtils;
import com.rapid.android.databinding.ActivityUserToolsBinding;
import com.rapid.android.databinding.DialogUserToolBinding;
import com.rapid.android.ui.common.RecyclerViewDecorations;
import com.rapid.android.ui.feature.web.ArticleWebViewActivity;

public class UserToolsActivity extends BaseActivity<UserToolsViewModel, ActivityUserToolsBinding>
        implements UserToolsAdapter.Callback {

    private UserToolsAdapter adapter;

    public static void start(@NonNull Context context) {
        context.startActivity(new Intent(context, UserToolsActivity.class));
    }

    @Override
    protected UserToolsViewModel createViewModel() {
        return new ViewModelProvider(this).get(UserToolsViewModel.class);
    }

    @Override
    protected ActivityUserToolsBinding createViewBinding() {
        return ActivityUserToolsBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initializeViews() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new UserToolsAdapter(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        RecyclerViewDecorations.addTopSpacing(binding.recyclerView);
        binding.swipeRefresh.setOnRefreshListener(viewModel::refresh);
        binding.fabAdd.setOnClickListener(v -> showEditDialog(null));
    }

    @Override
    protected void setupObservers() {
        viewModel.getTools().observe(this, list -> {
            adapter.submitList(list);
            binding.emptyView.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
        });
        viewModel.getLoading().observe(this, loading -> {
            boolean isLoading = Boolean.TRUE.equals(loading);
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.swipeRefresh.setRefreshing(isLoading);
        });
        viewModel.getMessage().observe(this, this::showShortToast);
    }

    @Override
    protected void loadData() {
        viewModel.refresh();
    }

    @Override
    public void onEdit(UserToolBean tool) {
        showEditDialog(tool);
    }

    @Override
    public void onDelete(UserToolBean tool) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.user_tools_delete)
                .setMessage(R.string.user_tools_delete_confirm)
                .setNegativeButton(R.string.user_tools_cancel, null)
                .setPositiveButton(R.string.user_tools_delete, (dialog, which) -> viewModel.deleteTool(tool.getId()))
                .show();
    }

    @Override
    public void onOpen(UserToolBean tool) {
        ArticleWebViewActivity.start(this, tool.getLink(), tool.getName());
    }

    private void showEditDialog(UserToolBean tool) {
        DialogUserToolBinding dialogBinding = DialogUserToolBinding.inflate(LayoutInflater.from(this));
        if (tool != null) {
            dialogBinding.inputName.setText(tool.getName());
            dialogBinding.inputLink.setText(tool.getLink());
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(tool == null ? R.string.user_tools_add : R.string.user_tools_edit)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.user_tools_save, (dialog, which) -> {
                    String name = dialogBinding.inputName.getText() != null ? dialogBinding.inputName.getText().toString() : "";
                    String link = dialogBinding.inputLink.getText() != null ? dialogBinding.inputLink.getText().toString() : "";
                    if (tool == null) {
                        viewModel.addTool(name, link);
                    } else {
                        viewModel.updateTool(tool.getId(), name, link);
                    }
                })
                .setNegativeButton(R.string.user_tools_cancel, null)
                .show();
    }

    private void showShortToast(String message) {
        ToastUtils.showShortToast(getDialogController(), message);
    }
}
