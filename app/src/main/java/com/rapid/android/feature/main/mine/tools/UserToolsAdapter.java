package com.rapid.android.feature.main.mine.tools;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rapid.android.core.domain.model.UserToolBean;
import com.rapid.android.databinding.ItemUserToolBinding;

import java.util.ArrayList;
import java.util.List;

class UserToolsAdapter extends RecyclerView.Adapter<UserToolsAdapter.ToolViewHolder> {

    private final List<UserToolBean> items = new ArrayList<>();
    private final Callback callback;

    UserToolsAdapter(Callback callback) {
        this.callback = callback;
    }

    void submitList(List<UserToolBean> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ToolViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserToolBinding binding = ItemUserToolBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ToolViewHolder(binding, callback);
    }

    @Override
    public void onBindViewHolder(@NonNull ToolViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    interface Callback {
        void onEdit(UserToolBean tool);

        void onDelete(UserToolBean tool);

        void onOpen(UserToolBean tool);
    }

    static class ToolViewHolder extends RecyclerView.ViewHolder {

        private final ItemUserToolBinding binding;
        private final Callback callback;

        ToolViewHolder(@NonNull ItemUserToolBinding binding, Callback callback) {
            super(binding.getRoot());
            this.binding = binding;
            this.callback = callback;
        }

        void bind(UserToolBean tool) {
            binding.toolName.setText(tool.getName());
            binding.toolLink.setText(tool.getLink());

            binding.getRoot().setOnClickListener(v -> {
                if (callback != null) {
                    callback.onOpen(tool);
                }
            });

            binding.btnEdit.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onEdit(tool);
                }
            });

            binding.btnDelete.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onDelete(tool);
                }
            });
        }
    }
}
