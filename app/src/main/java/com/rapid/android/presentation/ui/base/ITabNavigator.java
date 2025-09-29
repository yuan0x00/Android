package com.rapid.android.presentation.ui.base;

public interface ITabNavigator {
    void navigateTo(int position);

    void disableTab(int position);

    void enableTab(int position);
}