package com.rapid.android.core.ui.components.dialog;

import androidx.annotation.NonNull;

public interface ScopedDialogHost {

    @NonNull
    DialogController provideDialogController();
}
