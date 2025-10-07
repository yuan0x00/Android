package com.rapid.android.ui.feature.main.system.list;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rapid.android.R;
import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.core.webview.WebViewActivity;

import java.util.ArrayList;
import java.util.List;

final class SystemArticleListAdapter extends RecyclerView.Adapter<SystemArticleListAdapter.SystemArticleViewHolder> {

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
    public SystemArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_system_article, parent, false);
        return new SystemArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SystemArticleViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SystemArticleViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView desc;
        private final TextView author;
        private final TextView meta;

        SystemArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            desc = itemView.findViewById(R.id.tvDesc);
            author = itemView.findViewById(R.id.tvAuthor);
            meta = itemView.findViewById(R.id.tvMeta);
        }

        void bind(ArticleListBean.Data data) {
            title.setText(data.getTitle());

            if (!TextUtils.isEmpty(data.getDesc())) {
                desc.setVisibility(View.VISIBLE);
                desc.setText(data.getDesc());
            } else {
                desc.setVisibility(View.GONE);
                desc.setText("");
            }

            String authorName = !TextUtils.isEmpty(data.getAuthor()) ? data.getAuthor() : data.getShareUser();
            if (TextUtils.isEmpty(authorName)) {
                authorName = itemView.getContext().getString(R.string.system_placeholder_author);
            }
            author.setText(authorName);

            StringBuilder metaBuilder = new StringBuilder();
            if (!TextUtils.isEmpty(data.getSuperChapterName())) {
                metaBuilder.append(data.getSuperChapterName());
            }
            if (!TextUtils.isEmpty(data.getNiceShareDate())) {
                if (metaBuilder.length() > 0) {
                    metaBuilder.append(" · ");
                }
                metaBuilder.append(data.getNiceShareDate());
            } else if (!TextUtils.isEmpty(data.getNiceDate())) {
                if (metaBuilder.length() > 0) {
                    metaBuilder.append(" · ");
                }
                metaBuilder.append(data.getNiceDate());
            }
            meta.setText(metaBuilder.toString());

            itemView.setOnClickListener(v -> {
                if (!TextUtils.isEmpty(data.getLink())) {
                    WebViewActivity.start(v.getContext(), data.getLink(), data.getTitle());
                }
            });
        }
    }
}
