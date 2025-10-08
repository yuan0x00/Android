package com.rapid.android.ui.feature.main.discover.tutorial;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.rapid.android.R;
import com.rapid.android.core.domain.model.CategoryNodeBean;

public final class TutorialChapterAdapter extends ListAdapter<CategoryNodeBean, TutorialChapterAdapter.ViewHolder> {

    private static final DiffUtil.ItemCallback<CategoryNodeBean> DIFF_CALLBACK = new DiffUtil.ItemCallback<CategoryNodeBean>() {
        @Override
        public boolean areItemsTheSame(@NonNull CategoryNodeBean oldItem, @NonNull CategoryNodeBean newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull CategoryNodeBean oldItem, @NonNull CategoryNodeBean newItem) {
            return oldItem.getId() == newItem.getId()
                    && equals(oldItem.getName(), newItem.getName())
                    && equals(oldItem.getDesc(), newItem.getDesc());
        }

        private boolean equals(String a, String b) {
            return a == null ? b == null : a.equals(b);
        }
    };
    private OnItemClickListener listener;

    public TutorialChapterAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tutorial_chapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    interface OnItemClickListener {
        void onItemClick(@NonNull CategoryNodeBean item);
    }

    class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView description;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            description = itemView.findViewById(R.id.tvDescription);
        }

        void bind(CategoryNodeBean item) {
            title.setText(item.getName());
            if (item.getDesc() == null || item.getDesc().isEmpty()) {
                description.setVisibility(View.GONE);
            } else {
                description.setVisibility(View.VISIBLE);
                description.setText(item.getDesc());
            }
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}
