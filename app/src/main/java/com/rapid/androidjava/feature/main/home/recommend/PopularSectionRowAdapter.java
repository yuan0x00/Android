package com.rapid.android.feature.main.home.recommend;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;
import com.rapid.android.databinding.ItemHomePopularTablayoutBinding;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class PopularSectionRowAdapter extends RecyclerView.Adapter<PopularSectionRowAdapter.RowViewHolder> {

    private final Fragment parentFragment;
    private List<PopularSection> sections = Collections.emptyList();

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
        private final Handler handler = new Handler(Looper.getMainLooper());
        private final Map<Integer, Integer> heightCache = new HashMap<>(); // 缓存高度
        private TabLayoutMediator tabLayoutMediator;
        private int currentHeight = 0; // 当前高度
        private int pageWidth; // 页面宽度

        RowViewHolder(@NonNull ItemHomePopularTablayoutBinding binding, Fragment parentFragment) {
            super(binding.getRoot());
            this.binding = binding;
            pagerAdapter = new PopularSectionPagerAdapter(parentFragment);
            binding.viewPager.setAdapter(pagerAdapter);
            binding.viewPager.setUserInputEnabled(true);

            // 获取页面宽度
            pageWidth = binding.viewPager.getWidth();
            if (pageWidth == 0) {
                binding.viewPager.post(() -> {
                    this.pageWidth = binding.viewPager.getWidth();
                });
            }

            // 滑动过程中监听
            binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if (pageWidth > 0) {
                        adjustViewPagerHeightWithAnimation(position, position + 1, positionOffsetPixels);
                    }
                }

                @Override
                public void onPageSelected(int position) {
                    updateCurrentHeight(position);
                }
            });
        }

        void bind(List<PopularSection> data) {
            pagerAdapter.submitList(data);

            if (tabLayoutMediator != null) {
                tabLayoutMediator.detach();
            }

            tabLayoutMediator = new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                    (tab, position) -> tab.setText(pagerAdapter.getTitle(position))
            );
            tabLayoutMediator.attach();

            // 初始高度调整
            handler.postDelayed(() -> adjustViewPagerHeightWithAnimation(
                    binding.viewPager.getCurrentItem(), binding.viewPager.getCurrentItem(), 0), 100);
        }

        private void adjustViewPagerHeightWithAnimation(int currentPosition, int targetPosition, int positionOffsetPixels) {
            if (pageWidth <= 0) return; // 确保宽度有效

            Fragment currentFragment = pagerAdapter.getFragmentAt(currentPosition);
            Fragment targetFragment = (targetPosition < pagerAdapter.getItemCount()) ?
                    pagerAdapter.getFragmentAt(targetPosition) : null;

            if (currentFragment != null && currentFragment.getView() != null) {
                int currentHeight = getFragmentHeight(currentFragment, currentPosition);
                int targetHeight = (targetFragment != null && targetFragment.getView() != null) ?
                        getFragmentHeight(targetFragment, targetPosition) : currentHeight;

                // 计算插值因子（基于滑动距离）
                float progress = Math.min(1.0f, (float) positionOffsetPixels / pageWidth);
                int animatedHeight = (int) (currentHeight + (targetHeight - currentHeight) * progress);

                // 实时更新高度
                ViewGroup.LayoutParams layoutParams = binding.viewPager.getLayoutParams();
                if (layoutParams.height != animatedHeight) {
                    layoutParams.height = animatedHeight;
                    binding.viewPager.setLayoutParams(layoutParams);
                    binding.viewPager.requestLayout();
                    itemView.requestLayout();
                }
            }
        }

        private int getFragmentHeight(Fragment fragment, int position) {
            Integer cachedHeight = heightCache.get(position);
            if (cachedHeight != null) {
                return cachedHeight;
            }

            final View fragmentView = fragment.getView();
            if (fragmentView != null) {
                fragmentView.measure(
                        View.MeasureSpec.makeMeasureSpec(pageWidth, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                );
                int height = fragmentView.getMeasuredHeight();
                if (height > 0) {
                    heightCache.put(position, height);
                    return height;
                }
            }
            return currentHeight; // 默认使用当前高度
        }

        private void updateCurrentHeight(int position) {
            Fragment fragment = pagerAdapter.getFragmentAt(position);
            if (fragment != null && fragment.getView() != null) {
                int height = getFragmentHeight(fragment, position);
                if (height != currentHeight) {
                    currentHeight = height;
                }
            }
        }
    }
}