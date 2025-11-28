package com.rapid.android.feature.main;

public interface TabNavigator {
    void navigateTo(int position);

    void disableTab(int position);

    void enableTab(int position);

    void hideBottomBar(boolean animated);

    void showBottomBar(boolean animated);

    boolean isBottomBarVisible();

    void onHomeNavigationClick();
}
