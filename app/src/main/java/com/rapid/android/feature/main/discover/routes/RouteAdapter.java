package com.rapid.android.feature.main.discover.routes;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.rapid.android.core.domain.model.CategoryNodeBean;
import com.rapid.android.databinding.ItemRouteBinding;
import com.rapid.android.feature.web.ArticleWebViewUtil;

import java.util.ArrayList;
import java.util.List;

class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.RouteViewHolder> {

    private final List<CategoryNodeBean> items = new ArrayList<>();

    void submitList(List<CategoryNodeBean> data) {
        List<CategoryNodeBean> newItems = data != null ? new ArrayList<>(data) : new ArrayList<>();
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new RouteDiffCallback(items, newItems));
        items.clear();
        items.addAll(newItems);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRouteBinding binding = ItemRouteBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new RouteViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class RouteViewHolder extends RecyclerView.ViewHolder {

        private final ItemRouteBinding binding;

        RouteViewHolder(@NonNull ItemRouteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CategoryNodeBean bean) {
            if (bean == null) {
                binding.routeTitle.setText("");
                binding.routeSubtitle.setVisibility(View.GONE);
                binding.getRoot().setOnClickListener(null);
                binding.getRoot().setClickable(false);
                binding.getRoot().setFocusable(false);
                return;
            }

            binding.routeTitle.setText(nonNull(bean.getName()));

            String subtitle = buildSubtitle(bean);
            if (TextUtils.isEmpty(subtitle)) {
                binding.routeSubtitle.setVisibility(View.GONE);
            } else {
                binding.routeSubtitle.setVisibility(View.VISIBLE);
                binding.routeSubtitle.setText(subtitle);
            }

            String link = resolveLink(bean);
            if (TextUtils.isEmpty(link)) {
                binding.getRoot().setOnClickListener(null);
                binding.getRoot().setClickable(false);
                binding.getRoot().setFocusable(false);
            } else {
                binding.getRoot().setClickable(true);
                binding.getRoot().setFocusable(true);
                binding.getRoot().setOnClickListener(v ->
                        ArticleWebViewUtil.start(v.getContext(), link, bean.getName()));
            }
        }

        private String buildSubtitle(CategoryNodeBean bean) {
            List<CategoryNodeBean> children = bean.getChildren();
            if (children == null || children.isEmpty()) {
                return nonNull(bean.getDesc());
            }
            StringBuilder builder = new StringBuilder();
            int count = Math.min(children.size(), 3);
            for (int i = 0; i < count; i++) {
                CategoryNodeBean child = children.get(i);
                if (child == null || TextUtils.isEmpty(child.getName())) {
                    continue;
                }
                if (builder.length() > 0) {
                    builder.append(" · ");
                }
                builder.append(child.getName());
            }
            return builder.toString();
        }

        private String resolveLink(CategoryNodeBean bean) {
            if (!TextUtils.isEmpty(bean.getLink())) {
                return bean.getLink();
            }
            List<CategoryNodeBean> children = bean.getChildren();
            if (children == null || children.isEmpty()) {
                return null;
            }
            for (CategoryNodeBean child : children) {
                if (child != null && !TextUtils.isEmpty(child.getLink())) {
                    return child.getLink();
                }
            }
            return null;
        }

        private String nonNull(String value) {
            return value != null ? value : "";
        }
    }

    private static class RouteDiffCallback extends DiffUtil.Callback {
        private final List<CategoryNodeBean> oldList;
        private final List<CategoryNodeBean> newList;

        RouteDiffCallback(List<CategoryNodeBean> oldList, List<CategoryNodeBean> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            CategoryNodeBean oldItem = oldList.get(oldItemPosition);
            CategoryNodeBean newItem = newList.get(newItemPosition);
            return oldItem != null && newItem != null && oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            CategoryNodeBean oldItem = oldList.get(oldItemPosition);
            CategoryNodeBean newItem = newList.get(newItemPosition);
            if (oldItem == null || newItem == null) {
                return oldItem == newItem;
            }
            return TextUtils.equals(oldItem.getName(), newItem.getName())
                    && TextUtils.equals(oldItem.getDesc(), newItem.getDesc())
                    && TextUtils.equals(oldItem.getLink(), newItem.getLink())
                    && hasSameChildrenSignature(oldItem, newItem);
        }

        private boolean hasSameChildrenSignature(CategoryNodeBean oldItem, CategoryNodeBean newItem) {
            List<CategoryNodeBean> oldChildren = oldItem.getChildren();
            List<CategoryNodeBean> newChildren = newItem.getChildren();
            if (oldChildren == null || newChildren == null) {
                return oldChildren == newChildren;
            }
            if (oldChildren.size() != newChildren.size()) {
                return false;
            }
            for (int i = 0; i < oldChildren.size(); i++) {
                CategoryNodeBean oldChild = oldChildren.get(i);
                CategoryNodeBean newChild = newChildren.get(i);
                int oldId = oldChild != null ? oldChild.getId() : -1;
                int newId = newChild != null ? newChild.getId() : -1;
                if (oldId != newId) {
                    return false;
                }
            }
            return true;
        }
    }
}
