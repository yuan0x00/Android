package com.rapid.android.ui.feature.main.navigation;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rapid.android.R;
import com.rapid.android.domain.model.ArticleListBean;
import com.rapid.android.domain.model.NavigationBean;

import java.util.ArrayList;
import java.util.List;

public class NavigationAdapter extends RecyclerView.Adapter<NavigationAdapter.NavigationViewHolder> {

    private final List<NavigationBean> items = new ArrayList<>();

    public void submitList(List<NavigationBean> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NavigationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_navigation, parent, false);
        return new NavigationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NavigationViewHolder holder, int position) {
        NavigationBean item = items.get(position);
        holder.titleText.setText(item.getName());

        List<ArticleListBean.Data> articles = item.getArticles();
        if (articles != null && !articles.isEmpty()) {
            List<String> titles = new ArrayList<>();
            for (ArticleListBean.Data article : articles) {
                if (!TextUtils.isEmpty(article.getTitle())) {
                    titles.add(article.getTitle());
                }
            }
            holder.articlesText.setText(TextUtils.join("  |  ", titles));
        } else {
            holder.articlesText.setText(R.string.no_navigation_articles);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class NavigationViewHolder extends RecyclerView.ViewHolder {
        final TextView titleText;
        final TextView articlesText;

        NavigationViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            articlesText = itemView.findViewById(R.id.articlesText);
        }
    }
}
