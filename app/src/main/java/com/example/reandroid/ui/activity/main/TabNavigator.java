package com.example.reandroid.ui.activity.main;

public interface TabNavigator {
    void navigateTo(int position);

    void disableTab(int position);

    void enableTab(int position);
}