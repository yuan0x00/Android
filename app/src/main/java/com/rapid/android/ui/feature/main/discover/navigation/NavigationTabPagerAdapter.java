package com.rapid.android.ui.feature.main.discover.navigation;

import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.core.domain.model.NavigationBean;
import com.rapid.android.databinding.ItemNavigationTabPageBinding;
import com.rapid.android.ui.feature.web.ArticleWebViewActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class NavigationTabPagerAdapter extends RecyclerView.Adapter<NavigationTabPagerAdapter.NavigationPageViewHolder> {

    private final List<NavigationBean> items = new ArrayList<>();
    private final Map<Integer, PageState> states = new HashMap<>();

    void submitList(List<NavigationBean> data) {
        items.clear();
        states.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    NavigationBean getItem(int position) {
        if (position < 0 || position >= items.size()) {
            return null;
        }
        return items.get(position);
    }

    boolean canScrollUp(int position) {
        NavigationBean item = getItem(position);
        if (item == null) {
            return false;
        }
        PageState state = states.get(item.getCid());
        if (state == null || state.scrollView == null) {
            return false;
        }
        return state.scrollView.canScrollVertically(-1);
    }

    @NonNull
    @Override
    public NavigationPageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNavigationTabPageBinding binding = ItemNavigationTabPageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new NavigationPageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NavigationPageViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private PageState obtainState(NavigationBean bean) {
        PageState state = states.get(bean.getCid());
        if (state == null) {
            state = new PageState();
            states.put(bean.getCid(), state);
        }
        return state;
    }

    private static class PageState {
        androidx.core.widget.NestedScrollView scrollView;
    }

    class NavigationPageViewHolder extends RecyclerView.ViewHolder {

        private final ItemNavigationTabPageBinding binding;

        NavigationPageViewHolder(@NonNull ItemNavigationTabPageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(NavigationBean item) {
            PageState state = obtainState(item);
            state.scrollView = binding.getRoot();
            binding.chipGroup.removeAllViews();
            if (item == null) {
                binding.chipGroup.setVisibility(View.GONE);
                return;
            }

            List<ArticleListBean.Data> articles = item.getArticles();
            if (articles == null) {
                articles = new ArrayList<>();
            }

            int added = 0;
            for (ArticleListBean.Data article : articles) {
                if (article == null || TextUtils.isEmpty(article.getTitle())) {
                    continue;
                }
                Chip chip = createArticleChip(article);
                binding.chipGroup.addView(chip);
                added++;
            }

            binding.chipGroup.setVisibility(added > 0 ? View.VISIBLE : View.GONE);
        }

        private Chip createArticleChip(ArticleListBean.Data article) {
            Chip chip = new Chip(binding.chipGroup.getContext());
            if (TextUtils.indexOf(article.getTitle(), '<') >= 0) {
                chip.setText(Html.fromHtml(article.getTitle(), Html.FROM_HTML_MODE_LEGACY));
            } else {
                chip.setText(article.getTitle());
            }
            chip.setCheckable(false);
            chip.setClickable(true);
            chip.setEnsureMinTouchTargetSize(false);
            chip.setMaxLines(1);
            chip.setEllipsize(TextUtils.TruncateAt.END);
            chip.setOnClickListener(v -> {
                if (!TextUtils.isEmpty(article.getLink())) {
                    ArticleWebViewActivity.start(v.getContext(), article);
                }
            });
            return chip;
        }
    }
}
