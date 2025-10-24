package com.rapid.android.feature.main.message;

import com.rapid.android.R;

enum MessageCategory {

    UNREAD(0, R.string.message_tab_unread),
    READ(1, R.string.message_tab_read);

    private final int position;
    private final int titleRes;

    MessageCategory(int position, int titleRes) {
        this.position = position;
        this.titleRes = titleRes;
    }

    static MessageCategory fromPosition(int value) {
        for (MessageCategory category : values()) {
            if (category.position == value) {
                return category;
            }
        }
        return UNREAD;
    }

    int getPosition() {
        return position;
    }

    int getTitleRes() {
        return titleRes;
    }
}
