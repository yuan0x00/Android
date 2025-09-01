package com.example.android.ui.activity.main;

public interface ITabNavigator {
    void navigateTo(int position);

    void disableTab(int position);

    void enableTab(int position);
}