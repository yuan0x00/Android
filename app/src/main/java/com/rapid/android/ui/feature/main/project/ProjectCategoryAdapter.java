package com.rapid.android.ui.feature.main.project;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.lib.domain.model.CategoryNodeBean;
import com.rapid.android.R;

import java.util.ArrayList;
import java.util.List;

public class ProjectCategoryAdapter extends RecyclerView.Adapter<ProjectCategoryAdapter.CategoryViewHolder> {

    private static final int[] ACCENT_COLORS = {
            R.color.project_category_accent1,
            R.color.project_category_accent2,
            R.color.project_category_accent3,
            R.color.project_category_accent4
    };

    private final List<CategoryNodeBean> items = new ArrayList<>();
    private final OnCategoryClickListener clickListener;

    public ProjectCategoryAdapter(OnCategoryClickListener clickListener) {
        this.clickListener = clickListener;
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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.project_item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryNodeBean item = items.get(position);
        holder.title.setText(item.getName());
        holder.bindAccentColor(ContextCompat.getColor(holder.itemView.getContext(),
                ACCENT_COLORS[position % ACCENT_COLORS.length]));

        StringBuilder metaBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(item.getAuthor())) {
            metaBuilder.append(item.getAuthor());
        }
        int childCount = item.getChildren() != null ? item.getChildren().size() : 0;
        if (childCount > 0) {
            if (metaBuilder.length() > 0) {
                metaBuilder.append(" · ");
            }
            metaBuilder.append(holder.itemView.getContext()
                    .getString(R.string.project_category_sub_count, childCount));
        }

        if (metaBuilder.length() > 0) {
            holder.meta.setVisibility(View.VISIBLE);
            holder.meta.setText(metaBuilder.toString());
        } else {
            holder.meta.setVisibility(View.GONE);
            holder.meta.setText("");
        }

        if (!TextUtils.isEmpty(item.getDesc())) {
            holder.desc.setVisibility(View.VISIBLE);
            holder.desc.setText(item.getDesc());
        } else {
            holder.desc.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onCategoryClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnCategoryClickListener {
        void onCategoryClick(@NonNull CategoryNodeBean category);
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView meta;
        final TextView desc;
        final View accent;
        final ImageView arrow;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvCategoryName);
            meta = itemView.findViewById(R.id.tvCategoryMeta);
            desc = itemView.findViewById(R.id.tvCategoryDesc);
            accent = itemView.findViewById(R.id.viewAccent);
            arrow = itemView.findViewById(R.id.ivArrow);
        }

        void bindAccentColor(int color) {
            if (accent != null) {
                Drawable background = accent.getBackground();
                if (background != null) {
                    Drawable wrapped = DrawableCompat.wrap(background.mutate());
                    DrawableCompat.setTint(wrapped, color);
                    accent.setBackground(wrapped);
                } else {
                    accent.setBackgroundColor(color);
                }
            }

            if (arrow != null) {
                Drawable arrowDrawable = arrow.getDrawable();
                if (arrowDrawable != null) {
                    Drawable wrappedArrow = DrawableCompat.wrap(arrowDrawable.mutate());
                    DrawableCompat.setTint(wrappedArrow, color);
                    arrow.setImageDrawable(wrappedArrow);
                }
            }
        }
    }
}
