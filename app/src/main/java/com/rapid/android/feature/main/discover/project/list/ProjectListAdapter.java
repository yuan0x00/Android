package com.rapid.android.feature.main.discover.project.list;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rapid.android.R;
import com.rapid.android.core.domain.model.ProjectPageBean;
import com.rapid.android.feature.web.ArticleWebViewActivity;
import com.rapid.android.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class ProjectListAdapter extends RecyclerView.Adapter<ProjectListAdapter.ProjectViewHolder> {

    private final List<ProjectPageBean.ProjectItemBean> items = new ArrayList<>();

    public void submitNewList(List<ProjectPageBean.ProjectItemBean> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    public void appendList(List<ProjectPageBean.ProjectItemBean> more) {
        if (more == null || more.isEmpty()) {
            return;
        }
        int start = items.size();
        items.addAll(more);
        notifyItemRangeInserted(start, more.size());
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        ProjectPageBean.ProjectItemBean item = items.get(position);
        holder.title.setText(item.getTitle());
        if (!TextUtils.isEmpty(item.getDesc())) {
            holder.desc.setVisibility(View.VISIBLE);
            holder.desc.setText(item.getDesc());
        } else {
            holder.desc.setVisibility(View.GONE);
        }

        StringBuilder metaBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(item.getAuthor())) {
            metaBuilder.append(item.getAuthor());
        } else if (!TextUtils.isEmpty(item.getShareUser())) {
            metaBuilder.append(item.getShareUser());
        }
        if (!TextUtils.isEmpty(item.getNiceDate())) {
            if (metaBuilder.length() > 0) {
                metaBuilder.append(" · ");
            }
            metaBuilder.append(item.getNiceDate());
        }
        if (!TextUtils.isEmpty(item.getSuperChapterName())) {
            if (metaBuilder.length() > 0) {
                metaBuilder.append(" · ");
            }
            metaBuilder.append(item.getSuperChapterName());
        }
        holder.meta.setText(metaBuilder.toString());

        ImageLoader.loadOrHide(holder.cover, item.getEnvelopePic());

        String targetUrl = !TextUtils.isEmpty(item.getLink()) ? item.getLink() : item.getProjectLink();
        holder.itemView.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(targetUrl)) {
                ArticleWebViewActivity.start(v.getContext(), targetUrl, item.getTitle());
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        final ImageView cover;
        final TextView title;
        final TextView desc;
        final TextView meta;

        ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.ivCover);
            title = itemView.findViewById(R.id.tvTitle);
            desc = itemView.findViewById(R.id.tvDesc);
            meta = itemView.findViewById(R.id.tvMeta);
        }
    }
}
