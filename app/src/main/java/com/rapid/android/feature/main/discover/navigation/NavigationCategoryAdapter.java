package com.rapid.android.feature.main.discover.navigation;

import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.core.domain.model.NavigationBean;
import com.rapid.android.databinding.ItemNavigationCategoryBinding;

import java.util.ArrayList;
import java.util.List;

final class NavigationCategoryAdapter extends RecyclerView.Adapter<NavigationCategoryAdapter.NavigationCategoryViewHolder> {

    private final List<NavigationBean> items = new ArrayList<>();
    private final Callbacks callbacks;

    NavigationCategoryAdapter(@NonNull Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    void submitList(List<NavigationBean> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NavigationCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNavigationCategoryBinding binding = ItemNavigationCategoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new NavigationCategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NavigationCategoryViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    interface Callbacks {
        void onCategoryClick(@NonNull NavigationBean category);

        void onArticleClick(@NonNull ArticleListBean.Data article);
    }

    class NavigationCategoryViewHolder extends RecyclerView.ViewHolder {

        private final ItemNavigationCategoryBinding binding;

        NavigationCategoryViewHolder(@NonNull ItemNavigationCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(NavigationBean category) {
            binding.textTitle.setText(category.getName());
            binding.getRoot().setOnClickListener(v -> callbacks.onCategoryClick(category));

            List<ArticleListBean.Data> articles = category.getArticles();
            ChipGroup chipGroup = binding.chipGroup;
            chipGroup.removeAllViews();
            if (articles == null || articles.isEmpty()) {
                chipGroup.setVisibility(View.GONE);
                return;
            }
            chipGroup.setVisibility(View.VISIBLE);
            for (ArticleListBean.Data article : articles) {
                if (article == null || TextUtils.isEmpty(article.getTitle())) {
                    continue;
                }
                Chip chip = createChip(article);
                chipGroup.addView(chip);
            }
        }

        private Chip createChip(ArticleListBean.Data article) {
            Chip chip = new Chip(binding.chipGroup.getContext());
            CharSequence title = article.getTitle();
            if (!TextUtils.isEmpty(title) && TextUtils.indexOf(title, '<') >= 0) {
                chip.setText(Html.fromHtml(title.toString(), Html.FROM_HTML_MODE_LEGACY));
            } else {
                chip.setText(title);
            }
            chip.setCheckable(false);
            chip.setClickable(true);
            chip.setEnsureMinTouchTargetSize(false);
            chip.setMaxLines(1);
            chip.setEllipsize(TextUtils.TruncateAt.END);
            chip.setOnClickListener(v -> callbacks.onArticleClick(article));
            return chip;
        }
    }
}
