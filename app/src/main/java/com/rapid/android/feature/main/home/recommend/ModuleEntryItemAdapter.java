package com.rapid.android.feature.main.home.recommend;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.rapid.android.databinding.ItemModuleEntryItemBinding;

import java.util.ArrayList;
import java.util.List;

public class ModuleEntryItemAdapter extends RecyclerView.Adapter<ModuleEntryItemAdapter.ModuleEntryViewHolder> {

    private List<String> titles = new ArrayList<>();
    private List<Integer> drawableIds = new ArrayList<>();
    private List<Fragment> fragments = new ArrayList<>();
    private OnItemClickListener onItemClickListener;

    public void setData(List<String> titles, List<Integer> drawablesIds, List<Fragment> fragments) {
        this.titles = titles != null ? titles : new ArrayList<>();
        this.drawableIds = drawablesIds != null ? drawablesIds : new ArrayList<>();
        this.fragments = fragments != null ? fragments : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ModuleEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemModuleEntryItemBinding binding = ItemModuleEntryItemBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ModuleEntryItemAdapter.ModuleEntryViewHolder(binding, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ModuleEntryItemAdapter.ModuleEntryViewHolder holder, int position) {
        holder.bind(titles.get(position), drawableIds.get(position), fragments.get(position), position);
    }

    @Override
    public int getItemCount() {
        return Math.min(titles.size(), fragments.size());
    }

    public interface OnItemClickListener {
        void onItemClick(int position, Fragment fragment, String title);
    }

    public static class ModuleEntryViewHolder extends RecyclerView.ViewHolder {
        private final OnItemClickListener onItemClickListener;
        private final ItemModuleEntryItemBinding binding;

        public ModuleEntryViewHolder(@NonNull ItemModuleEntryItemBinding binding, OnItemClickListener onItemClickListener) {
            super(binding.getRoot());
            this.binding = binding;
            this.onItemClickListener = onItemClickListener;
        }

        public void bind(String title, Integer drawableId, Fragment fragment, int position) {
            binding.textView.setText(title);
            binding.imageView.setImageDrawable(binding.getRoot().getContext().getDrawable(drawableId));

            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(position, fragment, title);
                }
            });
        }
    }
}
