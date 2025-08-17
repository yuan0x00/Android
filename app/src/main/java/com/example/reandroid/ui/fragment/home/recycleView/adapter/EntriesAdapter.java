package com.example.reandroid.ui.fragment.home.recycleView.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reandroid.R;
import com.example.reandroid.ui.fragment.home.recycleView.item.EntryItem;
import com.example.reandroid.ui.fragment.home.recycleView.viewHolder.EntriesViewHolder;

import java.util.List;

public class EntriesAdapter extends RecyclerView.Adapter<EntriesViewHolder> {
    private final List<EntryItem> entries;

    public EntriesAdapter(List<EntryItem> entries) {
        this.entries = entries;
    }

    @NonNull
    @Override
    public EntriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_entries, parent, false);
        return new EntriesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntriesViewHolder holder, int position) {
        holder.bind(entries);
    }

    @Override
    public int getItemCount() {
        return 1;
    }
}