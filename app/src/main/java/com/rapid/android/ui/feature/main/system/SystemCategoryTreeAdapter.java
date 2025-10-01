package com.rapid.android.ui.feature.main.system;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rapid.android.R;
import com.rapid.android.domain.model.CategoryNodeBean;

import java.util.ArrayList;
import java.util.List;

public class SystemCategoryTreeAdapter extends RecyclerView.Adapter<SystemCategoryTreeAdapter.CategoryViewHolder> {

    private final List<CategoryNodeBean> items = new ArrayList<>();

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
        if (children != null && !children.isEmpty()) {
            List<String> names = new ArrayList<>();
            for (CategoryNodeBean child : children) {
                if (!TextUtils.isEmpty(child.getName())) {
                    names.add(child.getName());
                }
            }
            if (!names.isEmpty()) {
                holder.childrenText.setText(TextUtils.join("  |  ", names));
                holder.childrenText.setVisibility(View.VISIBLE);
            } else {
                holder.childrenText.setVisibility(View.GONE);
            }
        } else {
            holder.childrenText.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        final TextView titleText;
        final TextView childrenText;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            childrenText = itemView.findViewById(R.id.childrenText);
        }
    }
}
