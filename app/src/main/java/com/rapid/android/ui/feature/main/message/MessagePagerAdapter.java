package com.rapid.android.ui.feature.main.message;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

class MessagePagerAdapter extends FragmentStateAdapter {

    MessagePagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        MessageCategory category = MessageCategory.fromPosition(position);
        return MessageListFragment.newInstance(category);
    }

    @Override
    public int getItemCount() {
        return MessageCategory.values().length;
    }

    CharSequence getPageTitle(@NonNull Context context, int position) {
        MessageCategory category = MessageCategory.fromPosition(position);
        return context.getString(category.getTitleRes());
    }
}
