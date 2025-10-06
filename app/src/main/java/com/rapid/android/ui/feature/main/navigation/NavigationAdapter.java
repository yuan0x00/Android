package com.rapid.android.ui.feature.main.navigation;

import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.core.domain.model.ArticleListBean;
import com.core.domain.model.NavigationBean;
import com.core.webview.WebViewActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.rapid.android.R;

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

    private static Chip createChip(@NonNull ChipGroup parent, @NonNull String rawTitle) {
        Chip chip = new Chip(parent.getContext());
        chip.setCheckable(false);
        chip.setClickable(true);
        chip.setEnsureMinTouchTargetSize(false);
        chip.setMaxLines(1);
        chip.setEllipsize(TextUtils.TruncateAt.END);
        if (TextUtils.indexOf(rawTitle, '<') >= 0) {
            chip.setText(Html.fromHtml(rawTitle, Html.FROM_HTML_MODE_LEGACY));
        } else {
            chip.setText(rawTitle);
        }
        return chip;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onBindViewHolder(@NonNull NavigationViewHolder holder, int position) {
        NavigationBean item = items.get(position);
        holder.titleText.setText(item.getName());

        List<ArticleListBean.Data> articles = item.getArticles();
        holder.articlesGroup.removeAllViews();
        if (articles != null && !articles.isEmpty()) {
            for (ArticleListBean.Data article : articles) {
                if (article == null || TextUtils.isEmpty(article.getTitle())) {
                    continue;
                }
                Chip chip = createChip(holder.articlesGroup, article.getTitle());
                String link = article.getLink();
                if (!TextUtils.isEmpty(link)) {
                    chip.setOnClickListener(v -> WebViewActivity.start(v.getContext(), link, article.getTitle()));
                } else {
                    chip.setEnabled(false);
                    chip.setClickable(false);
                    chip.setAlpha(0.6f);
                }
                holder.articlesGroup.addView(chip);
            }
            holder.articlesGroup.setVisibility(holder.articlesGroup.getChildCount() > 0 ? View.VISIBLE : View.GONE);
        } else {
            holder.articlesGroup.setVisibility(View.GONE);
        }
    }

    static class NavigationViewHolder extends RecyclerView.ViewHolder {
        final TextView titleText;
        final ChipGroup articlesGroup;

        NavigationViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            articlesGroup = itemView.findViewById(R.id.articlesGroup);
        }
    }
}
