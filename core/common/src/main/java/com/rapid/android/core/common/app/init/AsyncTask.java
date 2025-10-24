package com.rapid.android.core.common.app.init;

import java.util.Collections;
import java.util.List;

public abstract class AsyncTask implements InitTask {
    @Override
    public List<String> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public boolean isMainThread() {
        return false;
    }

    @Override
    public boolean isCritical() {
        return false;
    }
}
