package com.rapid.android.ui.common;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.core.domain.model.ArticleListBean;
import com.core.domain.model.CategoryNodeBean;
import com.core.webview.WebViewActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.rapid.android.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用的体系/项目分类树适配器，使用 Chip 展示二级节点。
 */
public class CategoryTreeAdapter extends RecyclerView.Adapter<CategoryTreeAdapter.CategoryViewHolder> {

    private final List<CategoryNodeBean> items = new ArrayList<>();

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

    public void submitList(List<CategoryNodeBean> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_node, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryNodeBean item = items.get(position);
        holder.titleText.setText(item.getName());

        List<CategoryNodeBean> children = item.getChildren();
        holder.childrenGroup.removeAllViews();
        if (children != null && !children.isEmpty()) {
            for (CategoryNodeBean child : children) {
                if (child == null || TextUtils.isEmpty(child.getName())) {
                    continue;
                }
                Chip chip = createChip(holder.childrenGroup, child.getName());
                String targetUrl = resolveTargetUrl(child);
                if (!TextUtils.isEmpty(targetUrl)) {
                    chip.setOnClickListener(v -> WebViewActivity.start(v.getContext(), targetUrl, child.getName()));
                    chip.setEnabled(true);
                    chip.setAlpha(1f);
                } else {
                    chip.setEnabled(false);
                    chip.setClickable(false);
                    chip.setAlpha(0.6f);
                }
                holder.childrenGroup.addView(chip);
            }
            holder.childrenGroup.setVisibility(holder.childrenGroup.getChildCount() > 0 ? View.VISIBLE : View.GONE);
        } else {
            holder.childrenGroup.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
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
}
