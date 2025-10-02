package com.rapid.android.ui.feature.main.project;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lib.domain.model.CategoryNodeBean;
import com.rapid.android.R;

import java.util.ArrayList;
import java.util.List;

public class ProjectCategoryAdapter extends RecyclerView.Adapter<ProjectCategoryAdapter.CategoryViewHolder> {

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
        final TextView desc;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvCategoryName);
            desc = itemView.findViewById(R.id.tvCategoryDesc);
        }
    }
}
