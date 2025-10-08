package com.rapid.android.ui.feature.main.discover.tutorial.list;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rapid.android.R;
import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.ui.feature.web.ArticleWebViewActivity;

import java.util.ArrayList;
import java.util.List;

final class TutorialArticleListAdapter extends RecyclerView.Adapter<TutorialArticleListAdapter.ViewHolder> {

    private final List<ArticleListBean.Data> items = new ArrayList<>();

    void submitNewList(List<ArticleListBean.Data> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tutorial_article, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView subtitle;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            subtitle = itemView.findViewById(R.id.tvSubtitle);
        }

        void bind(ArticleListBean.Data data) {
            title.setText(data.getTitle());

            StringBuilder sb = new StringBuilder();
            if (!TextUtils.isEmpty(data.getAuthor())) {
                sb.append(data.getAuthor());
            } else if (!TextUtils.isEmpty(data.getShareUser())) {
                sb.append(data.getShareUser());
            }
            if (!TextUtils.isEmpty(data.getNiceShareDate())) {
                if (sb.length() > 0) {
                    sb.append(" · ");
                }
                sb.append(data.getNiceShareDate());
            } else if (!TextUtils.isEmpty(data.getNiceDate())) {
                if (sb.length() > 0) {
                    sb.append(" · ");
                }
                sb.append(data.getNiceDate());
            }
            subtitle.setText(sb);

            itemView.setOnClickListener(v -> {
                if (!TextUtils.isEmpty(data.getLink())) {
                    ArticleWebViewActivity.start(v.getContext(), data);
                }
            });
        }
    }
}
