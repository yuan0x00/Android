package com.rapid.android.feature.main.home.recommend;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.rapid.android.core.domain.model.CategoryNodeBean;
import com.rapid.android.databinding.ItemHomePopularSectionBinding;
import com.rapid.android.feature.web.ArticleWebViewActivity;

import java.util.ArrayList;
import java.util.List;

class HomePopularSectionCardAdapter extends RecyclerView.Adapter<HomePopularSectionCardAdapter.SectionViewHolder> {

    private final List<HomePopularSection> sections = new ArrayList<>();

    void submitList(List<HomePopularSection> data) {
        List<HomePopularSection> newData = data != null ? new ArrayList<>(data) : new ArrayList<>();
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new SectionDiffCallback(sections, newData));
        sections.clear();
        sections.addAll(newData);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHomePopularSectionBinding binding = ItemHomePopularSectionBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new SectionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder holder, int position) {
        holder.bind(sections.get(position));
    }

    @Override
    public int getItemCount() {
        return sections.size();
    }

    static class SectionViewHolder extends RecyclerView.ViewHolder {

        private final ItemHomePopularSectionBinding binding;

        SectionViewHolder(@NonNull ItemHomePopularSectionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(HomePopularSection section) {
            if (section == null) {
                binding.sectionTitle.setText("");
                binding.chipGroup.removeAllViews();
                return;
            }
            binding.sectionTitle.setText(section.getTitle());
            binding.chipGroup.removeAllViews();

            List<CategoryNodeBean> chapters = section.getChapters();
            if (chapters == null || chapters.isEmpty()) {
                return;
            }

            LayoutInflater inflater = LayoutInflater.from(binding.getRoot().getContext());
            int count = Math.min(chapters.size(), 8);
            for (int i = 0; i < count; i++) {
                CategoryNodeBean item = chapters.get(i);
                if (item == null || TextUtils.isEmpty(item.getName())) {
                    continue;
                }
                Chip chip = (Chip) inflater.inflate(com.rapid.android.R.layout.item_home_popular_chip, binding.chipGroup, false);
                chip.setText(item.getName());
                String link = resolveLink(item);
                if (!TextUtils.isEmpty(link)) {
                    chip.setOnClickListener(v -> ArticleWebViewActivity.start(v.getContext(), link, item.getName()));
                } else {
                    chip.setOnClickListener(null);
                }
                binding.chipGroup.addView(chip);
            }
        }

        private String resolveLink(CategoryNodeBean bean) {
            if (bean == null) {
                return "";
            }
            if (!TextUtils.isEmpty(bean.getLink())) {
                return bean.getLink();
            }
            List<CategoryNodeBean> children = bean.getChildren();
            if (children == null) {
                return "";
            }
            for (CategoryNodeBean child : children) {
                if (child != null && !TextUtils.isEmpty(child.getLink())) {
                    return child.getLink();
                }
            }
            return "";
        }
    }

    private static class SectionDiffCallback extends DiffUtil.Callback {
        private final List<HomePopularSection> oldList;
        private final List<HomePopularSection> newList;

        SectionDiffCallback(List<HomePopularSection> oldList, List<HomePopularSection> newList) {
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
            HomePopularSection oldItem = oldList.get(oldItemPosition);
            HomePopularSection newItem = newList.get(newItemPosition);
            if (oldItem == null || newItem == null) {
                return oldItem == newItem;
            }
            return TextUtils.equals(oldItem.getTitle(), newItem.getTitle());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            HomePopularSection oldItem = oldList.get(oldItemPosition);
            HomePopularSection newItem = newList.get(newItemPosition);
            if (oldItem == null || newItem == null) {
                return oldItem == newItem;
            }
            List<CategoryNodeBean> oldChapters = oldItem.getChapters();
            List<CategoryNodeBean> newChapters = newItem.getChapters();
            if (oldChapters.size() != newChapters.size()) {
                return false;
            }
            for (int i = 0; i < oldChapters.size(); i++) {
                CategoryNodeBean oldBean = oldChapters.get(i);
                CategoryNodeBean newBean = newChapters.get(i);
                String oldName = oldBean != null ? oldBean.getName() : null;
                String newName = newBean != null ? newBean.getName() : null;
                if (!TextUtils.equals(oldName, newName)) {
                    return false;
                }
                String oldLink = oldBean != null ? oldBean.getLink() : null;
                String newLink = newBean != null ? newBean.getLink() : null;
                if (!TextUtils.equals(oldLink, newLink)) {
                    return false;
                }
            }
            return true;
        }
    }
}
