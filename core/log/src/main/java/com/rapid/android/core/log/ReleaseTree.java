package com.rapid.android.core.log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import timber.log.Timber;

final class ReleaseTree extends Timber.Tree {

    @Override
    protected void log(int priority, @Nullable String tag, @NonNull String message, @Nullable Throwable t) {
        // Release 环境可根据需要上报或写文件，这里默认不输出。
    }
}
