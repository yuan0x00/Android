package com.rapid.android.ui.feature.main.home;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.core.common.text.StringUtils;
import com.core.common.utils.ResUtils;
import com.core.domain.model.ArticleListBean;
import com.core.ui.components.popup.BasePopupWindow;
import com.core.webview.WebViewActivity;
import com.rapid.android.R;
import com.rapid.android.databinding.ItemFeedBinding;

import java.util.ArrayList;
import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {
    private final List<ArticleListBean.Data> items = new ArrayList<>();


    public FeedAdapter(ArticleListBean feeds) {
        setData(feeds);
    }

    public void setData(ArticleListBean newData) {
        submitList(newData != null ? newData.getDatas() : null);
    }

    public void submitList(List<ArticleListBean.Data> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    public void appendList(List<ArticleListBean.Data> more) {
        if (more == null || more.isEmpty()) {
            return;
        }
        int start = items.size();
        items.addAll(more);
        notifyItemRangeInserted(start, more.size());
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFeedBinding binding = ItemFeedBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new FeedViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class FeedViewHolder extends RecyclerView.ViewHolder {
        private final ItemFeedBinding binding;

        public FeedViewHolder(@NonNull ItemFeedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ArticleListBean.Data feedItem) {
            String author;
            if (StringUtils.isEmpty(feedItem.getAuthor())) {
                author = feedItem.getShareUser();
            } else {
                author = feedItem.getAuthor();
            }
            binding.tvAuthor.setText(author);
            binding.tvTitle.setText(feedItem.getTitle());
            binding.tvTime.setText(" · " + feedItem.getNiceShareDate());
            binding.tvClass.setText(feedItem.getSuperChapterName());

            GestureDetector gestureDetector = new GestureDetector(itemView.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public void onLongPress(MotionEvent e) {
                    // Get touch coordinates from MotionEvent
                    float touchX = e.getRawX();
                    float touchY = e.getRawY();

                    // Create and configure BasePopupWindow
                    BasePopupWindow popup = new BasePopupWindow.Builder(itemView.getContext())
                            .setFocusable(true)
                            .build();

                    // Show popup at touch position
                    popup.showAtTouchPosition(binding.getRoot(), touchX, touchY);
                }
            });

            // Set OnTouchListener to pass events to GestureDetector
            binding.getRoot().setOnTouchListener((v, event) -> {
                gestureDetector.onTouchEvent(event);
                return false;
            });

            binding.getRoot().setOnClickListener(v -> WebViewActivity.start(binding.getRoot().getContext(), feedItem.getLink(), feedItem.getTitle()));

            binding.ivFavorite.setOnClickListener(v -> {
                Drawable drawable = ResUtils.getDrawable(R.drawable.favorite_fill_24px);
                if (drawable != null) {
                    drawable.setTint(Color.RED);
                }
                binding.ivFavorite.setImageDrawable(drawable);
            });
        }
    }
}
