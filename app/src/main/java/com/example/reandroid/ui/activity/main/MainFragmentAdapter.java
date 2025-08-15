package com.example.reandroid.ui.activity.main;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.reandroid.ui.activity.main.fragment.DiscoverFragment;
import com.example.reandroid.ui.activity.main.fragment.HomeFragment;
import com.example.reandroid.ui.activity.main.fragment.MineFragment;

public class MainFragmentAdapter extends FragmentStateAdapter {

    public MainFragmentAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return new DiscoverFragment();
            case 2:
                return new MineFragment();
            case 0:
            default:
                return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}