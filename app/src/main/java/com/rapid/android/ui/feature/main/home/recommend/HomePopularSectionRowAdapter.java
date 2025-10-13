package com.rapid.android.ui.feature.main.home.recommend;

import android.view.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.rapid.android.databinding.ItemHomePopularRowBinding;

import java.util.Collections;
import java.util.List;

class HomePopularSectionRowAdapter extends RecyclerView.Adapter<HomePopularSectionRowAdapter.RowViewHolder> {

    private List<HomePopularSection> sections = Collections.emptyList();

    void submitList(List<HomePopularSection> data) {
        sections = data != null ? data : Collections.emptyList();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHomePopularRowBinding binding = ItemHomePopularRowBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new RowViewHolder(binding);
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

        private final HomePopularSectionCardAdapter cardAdapter;

        RowViewHolder(@NonNull ItemHomePopularRowBinding binding) {
            super(binding.getRoot());
            cardAdapter = new HomePopularSectionCardAdapter();
            binding.popularRecyclerView.setLayoutManager(
                    new LinearLayoutManager(binding.getRoot().getContext(), LinearLayoutManager.HORIZONTAL, false));
            binding.popularRecyclerView.setAdapter(cardAdapter);
            binding.popularRecyclerView.setClipToPadding(false);
            binding.popularRecyclerView.setClipChildren(false);
            binding.popularRecyclerView.setNestedScrollingEnabled(false);
            binding.popularRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
            setupTouchConflictHandler(binding.popularRecyclerView);
            PagerSnapHelper snapHelper = new PagerSnapHelper();
            snapHelper.attachToRecyclerView(binding.popularRecyclerView);
        }

        void bind(List<HomePopularSection> data) {
            cardAdapter.submitList(data);
        }

        private void setupTouchConflictHandler(RecyclerView recyclerView) {
            final int touchSlop = ViewConfiguration.get(recyclerView.getContext()).getScaledTouchSlop();
            recyclerView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                            requestParentsDisallowIntercept(recyclerView, true);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            requestParentsDisallowIntercept(recyclerView, false);
                            break;
                        default:
                            break;
                    }
                    return false;
                }
            });
        }

        private void requestParentsDisallowIntercept(View child, boolean disallow) {
            ViewParent parent = child.getParent();
            while (parent != null) {
                parent.requestDisallowInterceptTouchEvent(disallow);
                parent = parent.getParent();
            }
        }
    }
}
