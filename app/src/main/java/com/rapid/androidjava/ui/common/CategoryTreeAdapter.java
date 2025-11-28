package com.rapid.android.ui.common;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.rapid.android.R;
import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.core.domain.model.CategoryNodeBean;
import com.rapid.android.feature.web.ArticleWebViewUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用的体系/项目分类树适配器，使用 Chip 展示二级节点。
 */
public class CategoryTreeAdapter extends RecyclerView.Adapter<CategoryTreeAdapter.CategoryViewHolder> {

    private final OnChildClickListener childClickListener;

    private final List<CategoryNodeBean> items = new ArrayList<>();

    public CategoryTreeAdapter() {
        this(null);
    }

    public CategoryTreeAdapter(OnChildClickListener listener) {
        this.childClickListener = listener;
    }

    private static Chip createChip(@NonNull ChipGroup parent, @NonNull String text) {
        Chip chip = new Chip(parent.getContext());
        chip.setText(text);
        chip.setCheckable(false);
        chip.setClickable(true);
        chip.setEnsureMinTouchTargetSize(false);
        chip.setMaxLines(1);
        chip.setEllipsize(TextUtils.TruncateAt.END);
        return chip;
    }

    private static String resolveTargetUrl(@NonNull CategoryNodeBean node) {
        if (isValidUrl(node.getLink())) {
            return node.getLink();
        }
        if (isValidUrl(node.getLisenseLink())) {
            return node.getLisenseLink();
        }

        List<ArticleListBean.Data> articles = node.getArticleList();
        if (articles != null) {
            for (ArticleListBean.Data article : articles) {
                if (article != null && isValidUrl(article.getLink())) {
                    return article.getLink();
                }
            }
        }

        String desc = node.getDesc();
        if (isValidUrl(desc)) {
            return desc;
        }

        return null;
    }

    private static boolean isValidUrl(String value) {
        return !TextUtils.isEmpty(value) && (value.startsWith("http://") || value.startsWith("https://"));
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryNodeBean item = items.get(position);
        holder.titleText.setText(item.getName());

        List<CategoryNodeBean> children = item.getChildren();
        bindChildren(holder, item, children);
    }

    public void submitList(List<CategoryNodeBean> data) {
        List<CategoryNodeBean> newItems = data != null ? new ArrayList<>(data) : new ArrayList<>();
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new CategoryDiffCallback(items, newItems));
        items.clear();
        items.addAll(newItems);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_node, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void bindChildren(@NonNull CategoryViewHolder holder,
                              @NonNull CategoryNodeBean parent,
                              @Nullable List<CategoryNodeBean> children) {
        ChipGroup group = holder.childrenGroup;
        int index = 0;
        if (children != null) {
            for (CategoryNodeBean child : children) {
                if (child == null || TextUtils.isEmpty(child.getName())) {
                    continue;
                }
                Chip chip = obtainChip(group, index, child.getName());
                bindChip(chip, child, parent);
                if (chip.getParent() == null) {
                    group.addView(chip, index);
                }
                index++;
            }
        }

        while (group.getChildCount() > index) {
            group.removeViewAt(group.getChildCount() - 1);
        }

        group.setVisibility(index > 0 ? View.VISIBLE : View.GONE);
    }

    private Chip obtainChip(@NonNull ChipGroup parent, int position, @NonNull String text) {
        if (position < parent.getChildCount()) {
            View existing = parent.getChildAt(position);
            if (existing instanceof Chip) {
                Chip chip = (Chip) existing;
                chip.setText(text);
                return chip;
            } else {
                parent.removeViewAt(position);
            }
        }
        Chip chip = createChip(parent, text);
        return chip;
    }

    private void bindChip(@NonNull Chip chip,
                          @NonNull CategoryNodeBean child,
                          @NonNull CategoryNodeBean parent) {
        chip.setEnabled(true);
        chip.setAlpha(1f);
        chip.setClickable(true);
        if (childClickListener != null) {
            chip.setOnClickListener(v -> childClickListener.onChildClick(child, parent));
        } else {
            String targetUrl = resolveTargetUrl(child);
            if (!TextUtils.isEmpty(targetUrl)) {
                chip.setOnClickListener(v -> ArticleWebViewUtil.start(v.getContext(), targetUrl, child.getName()));
            } else {
                chip.setOnClickListener(null);
                chip.setEnabled(false);
                chip.setClickable(false);
                chip.setAlpha(0.6f);
            }
        }
    }

    public interface OnChildClickListener {
        void onChildClick(CategoryNodeBean child, CategoryNodeBean parent);
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        final TextView titleText;
        final ChipGroup childrenGroup;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            childrenGroup = itemView.findViewById(R.id.childrenGroup);
        }
    }

    private static class CategoryDiffCallback extends DiffUtil.Callback {
        private final List<CategoryNodeBean> oldList;
        private final List<CategoryNodeBean> newList;

        CategoryDiffCallback(List<CategoryNodeBean> oldList, List<CategoryNodeBean> newList) {
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
            return oldList.get(oldItemPosition).getId() == newList.get(newItemPosition).getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            CategoryNodeBean oldItem = oldList.get(oldItemPosition);
            CategoryNodeBean newItem = newList.get(newItemPosition);
            return TextUtils.equals(oldItem.getName(), newItem.getName())
                    && TextUtils.equals(oldItem.getDesc(), newItem.getDesc())
                    && TextUtils.equals(oldItem.getLink(), newItem.getLink())
                    && TextUtils.equals(oldItem.getLisenseLink(), newItem.getLisenseLink())
                    && areChildrenEquivalent(oldItem.getChildren(), newItem.getChildren());
        }

        private boolean areChildrenEquivalent(@Nullable List<CategoryNodeBean> oldChildren,
                                              @Nullable List<CategoryNodeBean> newChildren) {
            if (oldChildren == null || newChildren == null) {
                return oldChildren == null && newChildren == null;
            }
            if (oldChildren.size() != newChildren.size()) {
                return false;
            }
            for (int i = 0; i < oldChildren.size(); i++) {
                CategoryNodeBean oldChild = oldChildren.get(i);
                CategoryNodeBean newChild = newChildren.get(i);
                int oldId = oldChild != null ? oldChild.getId() : -1;
                int newId = newChild != null ? newChild.getId() : -1;
                if (oldId != newId) {
                    return false;
                }
                String oldName = oldChild != null ? oldChild.getName() : null;
                String newName = newChild != null ? newChild.getName() : null;
                if (!TextUtils.equals(oldName, newName)) {
                    return false;
                }
            }
            return true;
        }
    }
}
