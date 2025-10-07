package com.rapid.android.ui.feature.main.message;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rapid.android.R;
import com.rapid.android.core.domain.model.MessageBean;
import com.rapid.android.databinding.ItemMessageBinding;
import com.rapid.android.ui.feature.web.ArticleWebViewActivity;

import java.util.ArrayList;
import java.util.List;

class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final List<MessageBean> items = new ArrayList<>();

    void submitList(List<MessageBean> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMessageBinding binding = ItemMessageBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new MessageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemMessageBinding binding;

        MessageViewHolder(@NonNull ItemMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(MessageBean bean) {
            binding.messageTitle.setText(TextUtils.isEmpty(bean.getTitle()) ? itemView.getContext().getString(R.string.message_title) : bean.getTitle());
            binding.messageContent.setText(TextUtils.isEmpty(bean.getMessage()) ? "" : bean.getMessage());

            String time = !TextUtils.isEmpty(bean.getNiceDate()) ? bean.getNiceDate() : bean.getDate();
            if (time == null) {
                time = "";
            }
            String fromUser = !TextUtils.isEmpty(bean.getFromUserNick()) ? bean.getFromUserNick() : bean.getFromUser();
            if (fromUser == null) {
                fromUser = "";
            }
            String meta;
            if (TextUtils.isEmpty(fromUser)) {
                meta = time;
            } else if (TextUtils.isEmpty(time)) {
                meta = fromUser;
            } else {
                meta = itemView.getContext().getString(R.string.message_item_time_format, time, fromUser);
            }
            binding.messageMeta.setText(meta);

            boolean isRead = bean.isReadState();
            binding.getRoot().setAlpha(isRead ? 0.7f : 1f);

            View root = binding.getRoot();
            String link = bean.getLink();
            if (!TextUtils.isEmpty(link)) {
                root.setOnClickListener(v -> ArticleWebViewActivity.start(v.getContext(), link, bean.getTitle()));
            } else {
                root.setOnClickListener(null);
            }
        }
    }
}
