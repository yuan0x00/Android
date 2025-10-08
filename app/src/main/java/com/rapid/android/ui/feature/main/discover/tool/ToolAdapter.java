package com.rapid.android.ui.feature.main.discover.tool;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.rapid.android.core.domain.model.ToolItemBean;
import com.rapid.android.databinding.ItemToolBinding;
import com.rapid.android.ui.feature.web.ArticleWebViewActivity;

import java.util.ArrayList;
import java.util.List;

public class ToolAdapter extends RecyclerView.Adapter<ToolAdapter.ToolViewHolder> {

    private final List<ToolItemBean> items = new ArrayList<>();

    public void submitList(List<ToolItemBean> data) {
        List<ToolItemBean> newItems = data != null ? new ArrayList<>(data) : new ArrayList<>();
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ToolDiffCallback(items, newItems));
        items.clear();
        items.addAll(newItems);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ToolViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemToolBinding binding = ItemToolBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ToolViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ToolViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ToolViewHolder extends RecyclerView.ViewHolder {

        private final ItemToolBinding binding;

        ToolViewHolder(@NonNull ItemToolBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ToolItemBean bean) {
            binding.toolName.setText(bean.getName());
            binding.toolLink.setText(bean.getLink());
            binding.getRoot().setOnClickListener(v -> {
                if (!TextUtils.isEmpty(bean.getLink())) {
                    ArticleWebViewActivity.start(v.getContext(), bean.getLink(), bean.getName());
                }
            });
        }
    }

    private static class ToolDiffCallback extends DiffUtil.Callback {
        private final List<ToolItemBean> oldList;
        private final List<ToolItemBean> newList;

        ToolDiffCallback(List<ToolItemBean> oldList, List<ToolItemBean> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            ToolItemBean oldItem = oldList.get(oldItemPosition);
            ToolItemBean newItem = newList.get(newItemPosition);
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            ToolItemBean oldItem = oldList.get(oldItemPosition);
            ToolItemBean newItem = newList.get(newItemPosition);
            return TextUtils.equals(oldItem.getName(), newItem.getName())
                    && TextUtils.equals(oldItem.getLink(), newItem.getLink())
                    && TextUtils.equals(oldItem.getIcon(), newItem.getIcon());
        }
    }
}
