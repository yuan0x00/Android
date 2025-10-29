package com.rapid.android.feature.main.discover.system;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.core.domain.model.CategoryNodeBean;
import com.rapid.android.databinding.ItemSystemCategoryBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class SystemCategoryAdapter extends RecyclerView.Adapter<SystemCategoryAdapter.SystemCategoryViewHolder> {

    private final List<CategoryNodeBean> items = new ArrayList<>();
    private final OnCategoryClickListener listener;

    SystemCategoryAdapter(@NonNull OnCategoryClickListener listener) {
        this.listener = listener;
    }

    void submitList(List<CategoryNodeBean> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SystemCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSystemCategoryBinding binding = ItemSystemCategoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SystemCategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SystemCategoryViewHolder holder, int position) {
        holder.bind(items.get(position), position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    interface OnCategoryClickListener {
        void onCategoryClick(@NonNull CategoryNodeBean category, int position);

        void onChildClick(@NonNull CategoryNodeBean parent,
                          @NonNull CategoryNodeBean child,
                          int parentPosition,
                          int childPosition);
    }

    class SystemCategoryViewHolder extends RecyclerView.ViewHolder {

        private final ItemSystemCategoryBinding binding;

        SystemCategoryViewHolder(@NonNull ItemSystemCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CategoryNodeBean category, int position) {
            binding.textTitle.setText(category.getName());
            if (TextUtils.isEmpty(category.getDesc())) {
                binding.textDesc.setVisibility(View.GONE);
            } else {
                binding.textDesc.setVisibility(View.VISIBLE);
                binding.textDesc.setText(category.getDesc());
            }

            binding.getRoot().setOnClickListener(v -> listener.onCategoryClick(category, position));

            List<CategoryNodeBean> children = resolveChildren(category);
            ChipGroup chipGroup = binding.chipGroup;
            chipGroup.removeAllViews();
            if (children.isEmpty()) {
                chipGroup.setVisibility(View.GONE);
                return;
            }
            chipGroup.setVisibility(View.VISIBLE);
            for (int index = 0; index < children.size(); index++) {
                CategoryNodeBean child = children.get(index);
                if (child == null || TextUtils.isEmpty(child.getName())) {
                    continue;
                }
                Chip chip = createChip(child);
                final int childPosition = index;
                chip.setOnClickListener(v ->
                        listener.onChildClick(category, child, position, childPosition));
                chipGroup.addView(chip);
            }
        }

        private Chip createChip(CategoryNodeBean child) {
            Chip chip = new Chip(binding.chipGroup.getContext());
            chip.setText(child.getName());
            chip.setCheckable(false);
            chip.setClickable(true);
            chip.setEnsureMinTouchTargetSize(false);
            chip.setMaxLines(1);
            chip.setEllipsize(TextUtils.TruncateAt.END);
            return chip;
        }

        private List<CategoryNodeBean> resolveChildren(CategoryNodeBean parent) {
            List<CategoryNodeBean> children = parent.getChildren();
            if (children != null && !children.isEmpty()) {
                return children;
            }

            CategoryNodeBean single = new CategoryNodeBean();
            single.setId(parent.getId());
            single.setName(parent.getName());
            single.setDesc(parent.getDesc());
            single.setLink(parent.getLink());
            single.setLisenseLink(parent.getLisenseLink());
            single.setAuthor(parent.getAuthor());
            single.setCover(parent.getCover());
            single.setOrder(parent.getOrder());
            single.setParentChapterId(parent.getParentChapterId());
            single.setCourseId(parent.getCourseId());
            single.setType(parent.getType());
            List<ArticleListBean.Data> articleList = parent.getArticleList();
            if (articleList != null) {
                single.setArticleList(new ArrayList<>(articleList));
            }
            return Collections.singletonList(single);
        }
    }
}
