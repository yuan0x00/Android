package com.rapid.android.feature.main.discover.system;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.rapid.android.core.domain.model.CategoryNodeBean;

import java.util.ArrayList;
import java.util.List;

final class SystemCategoryChildPagerAdapter extends FragmentStateAdapter {

    private final List<CategoryNodeBean> children = new ArrayList<>();

    SystemCategoryChildPagerAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    static String createFragmentTag(int viewPagerId, long itemId) {
        return "f" + viewPagerId + ":" + itemId;
    }

    void submitChildren(List<CategoryNodeBean> data) {
        children.clear();
        if (data != null) {
            children.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        CategoryNodeBean child = getItem(position);
        int categoryId = child != null ? child.getId() : -1;
        String name = child != null ? child.getName() : "";
        return SystemCategoryChildFragment.newInstance(categoryId, name);
    }

    @Override
    public int getItemCount() {
        return children.size();
    }

    @Override
    public long getItemId(int position) {
        CategoryNodeBean child = getItem(position);
        if (child == null) {
            return RecyclerView.NO_ID;
        }
        return child.getId();
    }

    @Override
    public boolean containsItem(long itemId) {
        for (CategoryNodeBean child : children) {
            if (child != null && child.getId() == itemId) {
                return true;
            }
        }
        return false;
    }

    CategoryNodeBean getItem(int position) {
        if (position < 0 || position >= children.size()) {
            return null;
        }
        return children.get(position);
    }
}
