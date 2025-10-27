package com.rapid.android.feature.main.home.recommend;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.rapid.android.databinding.ItemHomePopularTablayoutBinding;

import java.util.Collections;
import java.util.List;

class PopularSectionRowAdapter extends RecyclerView.Adapter<PopularSectionRowAdapter.RowViewHolder> {

    private List<PopularSection> sections = Collections.emptyList();
    private final Fragment parentFragment;

    PopularSectionRowAdapter(Fragment parentFragment) {
        this.parentFragment = parentFragment;
    }

    void submitList(List<PopularSection> data) {
        sections = data != null ? data : Collections.emptyList();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHomePopularTablayoutBinding binding = ItemHomePopularTablayoutBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new RowViewHolder(binding, parentFragment);
    }

    @Override
    public void onBindViewHolder(@NonNull RowViewHolder holder, int position) {
        holder.bind(sections);
    }

    @Override
    public int getItemCount() {
        return sections == null || sections.isEmpty() ? 0 : 1;
    }

    static class RowViewHolder extends RecyclerView.ViewHolder {

        private final ItemHomePopularTablayoutBinding binding;
        private final PopularSectionPagerAdapter pagerAdapter;
        private TabLayoutMediator tabLayoutMediator;

        RowViewHolder(@NonNull ItemHomePopularTablayoutBinding binding, Fragment parentFragment) {
            super(binding.getRoot());
            this.binding = binding;
            pagerAdapter = new PopularSectionPagerAdapter(parentFragment);
            binding.viewPager.setAdapter(pagerAdapter);
            binding.viewPager.setUserInputEnabled(true);
        }

        void bind(List<PopularSection> data) {
            pagerAdapter.submitList(data);

            // Detach old mediator if exists
            if (tabLayoutMediator != null) {
                tabLayoutMediator.detach();
            }

            // Setup new TabLayoutMediator
            tabLayoutMediator = new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                    new TabLayoutMediator.TabConfigurationStrategy() {
                        @Override
                        public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                            tab.setText(pagerAdapter.getTitle(position));
                        }
                    }
            );
            tabLayoutMediator.attach();
        }
    }
}
