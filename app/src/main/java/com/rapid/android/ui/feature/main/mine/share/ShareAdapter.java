package com.rapid.android.ui.feature.main.mine.share;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.core.domain.model.ArticleListBean;
import com.core.webview.WebViewActivity;
import com.rapid.android.R;

import java.util.ArrayList;
import java.util.List;

final class ShareAdapter extends RecyclerView.Adapter<ShareAdapter.ShareViewHolder> {

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
    public ShareViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite_article, parent, false);
        return new ShareViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShareViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ShareViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView desc;
        private final TextView author;
        private final TextView meta;

        ShareViewHolder(@NonNull View itemView) {
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

            String authorName = !TextUtils.isEmpty(data.getShareUser())
                    ? data.getShareUser()
                    : data.getAuthor();
            if (TextUtils.isEmpty(authorName)) {
                authorName = itemView.getContext().getString(R.string.mine_placeholder_dash);
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
