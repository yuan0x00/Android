package com.rapid.android.ui.base;

public interface ITabNavigator {
    void navigateTo(int position);

    void disableTab(int position);

    void enableTab(int position);
}