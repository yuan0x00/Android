package com.rapid.android.ui.feature.main.mine.coin;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rapid.android.R;
import com.rapid.android.core.domain.model.CoinRecordBean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

class CoinRecordAdapter extends RecyclerView.Adapter<CoinRecordAdapter.RecordViewHolder> {

    private final List<CoinRecordBean> items = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    void submitNewList(List<CoinRecordBean> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_coin_record, parent, false);
        return new RecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        CoinRecordBean item = items.get(position);

        String reason = !TextUtils.isEmpty(item.getReason()) ? item.getReason() : holder.reason.getContext().getString(R.string.mine_placeholder_dash);
        holder.reason.setText(reason);

        if (!TextUtils.isEmpty(item.getDesc())) {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(item.getDesc());
        } else {
            holder.description.setVisibility(View.GONE);
            holder.description.setText("");
        }

        int change = item.getCoinCount();
        String changeDisplay = change > 0 ? "+" + change : String.valueOf(change);
        holder.coinChange.setText(changeDisplay);

        long dateValue = item.getDate();
        if (dateValue > 0L) {
            holder.date.setText(dateFormat.format(new Date(dateValue)));
        } else {
            holder.date.setText(R.string.mine_placeholder_dash);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class RecordViewHolder extends RecyclerView.ViewHolder {
        final TextView reason;
        final TextView description;
        final TextView coinChange;
        final TextView date;

        RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            reason = itemView.findViewById(R.id.tvReason);
            description = itemView.findViewById(R.id.tvDescription);
            coinChange = itemView.findViewById(R.id.tvCoinChange);
            date = itemView.findViewById(R.id.tvDate);
        }
    }
}
