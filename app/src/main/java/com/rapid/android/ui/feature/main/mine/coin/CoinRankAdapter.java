package com.rapid.android.ui.feature.main.mine.coin;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.core.domain.model.CoinRankBean;
import com.rapid.android.R;

import java.util.ArrayList;
import java.util.List;

class CoinRankAdapter extends RecyclerView.Adapter<CoinRankAdapter.RankViewHolder> {

    private final List<CoinRankBean> items = new ArrayList<>();

    void submitNewList(List<CoinRankBean> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RankViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_coin_rank, parent, false);
        return new RankViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RankViewHolder holder, int position) {
        CoinRankBean item = items.get(position);

        String rankValue = !TextUtils.isEmpty(item.getRank()) ? item.getRank() : String.valueOf(position + 1);
        String rankDisplay = rankValue.startsWith("#") ? rankValue : "#" + rankValue;
        holder.rank.setText(rankDisplay);

        String nickname = !TextUtils.isEmpty(item.getNickname()) ? item.getNickname() : item.getUsername();
        if (TextUtils.isEmpty(nickname)) {
            nickname = holder.userName.getContext().getString(R.string.mine_placeholder_dash);
        }
        holder.userName.setText(nickname);

        holder.level.setText(holder.level.getContext()
                .getString(R.string.mine_membership_level_format, item.getLevel()));
        holder.coinCount.setText(String.valueOf(item.getCoinCount()));

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class RankViewHolder extends RecyclerView.ViewHolder {
        final TextView rank;
        final TextView userName;
        final TextView level;
        final TextView coinCount;

        RankViewHolder(@NonNull View itemView) {
            super(itemView);
            rank = itemView.findViewById(R.id.tvRank);
            userName = itemView.findViewById(R.id.tvUserName);
            level = itemView.findViewById(R.id.tvLevel);
            coinCount = itemView.findViewById(R.id.tvCoinCount);
        }
    }
}
