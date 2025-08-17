package com.example.reandroid.ui.fragment.home.recycleView.viewHolder;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.gridlayout.widget.GridLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reandroid.R;
import com.example.reandroid.ui.fragment.home.recycleView.item.EntryItem;

import java.util.List;

public class EntriesViewHolder extends RecyclerView.ViewHolder {
    private final GridLayout gridLayout;

    public EntriesViewHolder(@NonNull View itemView) {
        super(itemView);
        gridLayout = itemView.findViewById(R.id.grid_layout);
    }

    public void bind(List<EntryItem> entries) {
        // 先清空
        gridLayout.removeAllViews();

        for (EntryItem entry : entries) {
            // 创建一个项
            View entryView = LayoutInflater.from(gridLayout.getContext())
                    .inflate(R.layout.item_entry_single, gridLayout, false);

            ImageView ivIcon = entryView.findViewById(R.id.iv_icon);
            TextView tvName = entryView.findViewById(R.id.tv_name);

            ivIcon.setImageResource(entry.iconRes);
            tvName.setText(entry.name);

            // 设置 layout params
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); // 平均分配
            params.setMargins(8, 8, 8, 8);
            entryView.setLayoutParams(params);

            gridLayout.addView(entryView);
        }
    }
}