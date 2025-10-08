package com.rapid.android.ui.feature.main.discover.system;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.core.domain.model.CategoryNodeBean;
import com.rapid.android.databinding.ItemSystemTabPageBinding;
import com.rapid.android.ui.feature.main.discover.system.list.SystemArticleListActivity;

import java.util.*;

final class SystemTabPagerAdapter extends RecyclerView.Adapter<SystemTabPagerAdapter.TabPageViewHolder> {

    private final List<CategoryNodeBean> items = new ArrayList<>();
    private final Map<Integer, PageState> states = new HashMap<>();

    void submitList(List<CategoryNodeBean> data) {
        items.clear();
        states.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    CategoryNodeBean getItem(int position) {
        if (position < 0 || position >= items.size()) {
            return null;
        }
        return items.get(position);
    }

    boolean canScrollUp(int position) {
        CategoryNodeBean item = getItem(position);
        if (item == null) {
            return false;
        }
        PageState state = states.get(item.getId());
        if (state == null || state.scrollView == null) {
            return false;
        }
        return state.scrollView.canScrollVertically(-1);
    }

    @NonNull
    @Override
    public TabPageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSystemTabPageBinding binding = ItemSystemTabPageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TabPageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TabPageViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private PageState obtainState(CategoryNodeBean category) {
        PageState state = states.get(category.getId());
        if (state == null) {
            state = new PageState();
            states.put(category.getId(), state);
        }
        return state;
    }

    private static class PageState {
        androidx.core.widget.NestedScrollView scrollView;
    }

    class TabPageViewHolder extends RecyclerView.ViewHolder {

        private final ItemSystemTabPageBinding binding;

        TabPageViewHolder(@NonNull ItemSystemTabPageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CategoryNodeBean category) {
            PageState state = obtainState(category);
            state.scrollView = binding.getRoot();
            binding.chipGroup.removeAllViews();
            if (category == null) {
                binding.chipGroup.setVisibility(View.GONE);
                return;
            }

            List<CategoryNodeBean> children = resolveChildren(category);
            if (children.isEmpty()) {
                binding.chipGroup.setVisibility(View.GONE);
                return;
            }

            binding.chipGroup.setVisibility(View.VISIBLE);
            for (CategoryNodeBean child : children) {
                if (child == null || TextUtils.isEmpty(child.getName())) {
                    continue;
                }
                Chip chip = createChip(child);
                binding.chipGroup.addView(chip);
            }
        }

        private Chip createChip(CategoryNodeBean child) {
            Chip chip = new Chip(binding.chipGroup.getContext());
            chip.setText(child.getName());
            chip.setCheckable(false);
            chip.setClickable(true);
            chip.setEnsureMinTouchTargetSize(false);
            chip.setMaxLines(1);
            chip.setEllipsize(TextUtils.TruncateAt.END);
            chip.setOnClickListener(v -> SystemArticleListActivity.start(
                    v.getContext(), child.getId(), child.getName()));
            return chip;
        }

        private List<CategoryNodeBean> resolveChildren(CategoryNodeBean parent) {
            List<CategoryNodeBean> children = parent.getChildren();
            if (children != null && !children.isEmpty()) {
                return children;
            }

            CategoryNodeBean single = new CategoryNodeBean();
            single.setId(parent.getId());
            single.setName(parent.getName());
            single.setDesc(parent.getDesc());
            single.setLink(parent.getLink());
            single.setLisenseLink(parent.getLisenseLink());
            single.setAuthor(parent.getAuthor());
            single.setCover(parent.getCover());
            List<ArticleListBean.Data> articleList = parent.getArticleList();
            if (articleList != null) {
                single.setArticleList(new ArrayList<>(articleList));
            }
            return Collections.singletonList(single);
        }
    }
}
