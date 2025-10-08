package com.rapid.android.core.ui.components.dialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.atomic.AtomicLong;

public abstract class DialogEffect {

    private static final AtomicLong TAG_COUNTER = new AtomicLong();

    private final String tag;

    protected DialogEffect(@NonNull String tag) {
        this.tag = tag;
    }

    @NonNull
    private static String nextTag(@NonNull String prefix) {
        return prefix + "_" + TAG_COUNTER.incrementAndGet();
    }

    @NonNull
    public String getTag() {
        return tag;
    }

    public static final class Confirm extends DialogEffect {
        private final CharSequence title;
        private final CharSequence message;
        @Nullable private final CharSequence positiveText;
        @Nullable private final CharSequence negativeText;
        @Nullable private final Runnable onPositive;
        @Nullable private final Runnable onNegative;

        private Confirm(@NonNull String tag,
                        @Nullable CharSequence title,
                        @Nullable CharSequence message,
                        @Nullable CharSequence positiveText,
                        @Nullable CharSequence negativeText,
                        @Nullable Runnable onPositive,
                        @Nullable Runnable onNegative) {
            super(tag);
            this.title = title;
            this.message = message;
            this.positiveText = positiveText;
            this.negativeText = negativeText;
            this.onPositive = onPositive;
            this.onNegative = onNegative;
        }

        public Confirm(@Nullable CharSequence title,
                       @Nullable CharSequence message,
                       @Nullable CharSequence positiveText,
                       @Nullable CharSequence negativeText,
                       @Nullable Runnable onPositive,
                       @Nullable Runnable onNegative) {
            this(nextTag("confirm"), title, message, positiveText, negativeText, onPositive, onNegative);
        }

        @Nullable
        public CharSequence getTitle() {
            return title;
        }

        @Nullable
        public CharSequence getMessage() {
            return message;
        }

        @Nullable
        public CharSequence getPositiveText() {
            return positiveText;
        }

        @Nullable
        public CharSequence getNegativeText() {
            return negativeText;
        }

        @Nullable
        public Runnable getOnPositive() {
            return onPositive;
        }

        @Nullable
        public Runnable getOnNegative() {
            return onNegative;
        }
    }

    public static final class Loading extends DialogEffect {
        private final boolean cancelable;

        public Loading(boolean cancelable) {
            super(nextTag("loading"));
            this.cancelable = cancelable;
        }

        public boolean isCancelable() {
            return cancelable;
        }
    }

    public static final class Custom extends DialogEffect {
        private final BaseDialogView dialogView;
        @Nullable private final Boolean cancelable;
        @Nullable private final Boolean dimEnabled;
        @Nullable private final Float dimAmount;

        private Custom(@NonNull String tag,
                       @NonNull BaseDialogView dialogView,
                       @Nullable Boolean cancelable,
                       @Nullable Boolean dimEnabled,
                       @Nullable Float dimAmount) {
            super(tag);
            this.dialogView = dialogView;
            this.cancelable = cancelable;
            this.dimEnabled = dimEnabled;
            this.dimAmount = dimAmount;
        }

        public Custom(@NonNull BaseDialogView dialogView) {
            this(nextTag("custom"), dialogView, null, null, null);
        }

        public Custom(@NonNull BaseDialogView dialogView,
                      @Nullable Boolean cancelable,
                      @Nullable Boolean dimEnabled,
                      @Nullable Float dimAmount) {
            this(nextTag("custom"), dialogView, cancelable, dimEnabled, dimAmount);
        }

        @NonNull
        public BaseDialogView getDialogView() {
            return dialogView;
        }

        @Nullable
        public Boolean getCancelableOverride() {
            return cancelable;
        }

        @Nullable
        public Boolean getDimEnabledOverride() {
            return dimEnabled;
        }

        @Nullable
        public Float getDimAmountOverride() {
            return dimAmount;
        }
    }

    public static final class Toast extends DialogEffect {
        private static final long DEFAULT_DURATION = 2000L;
        private static final int DEFAULT_MAX_COUNT = 3;

        private final CharSequence message;
        private final long durationMillis;
        private final int maxShowingCount;

        public Toast(@NonNull CharSequence message) {
            this(message, DEFAULT_DURATION, DEFAULT_MAX_COUNT);
        }

        public Toast(@NonNull CharSequence message,
                     long durationMillis,
                     int maxShowingCount) {
            super(nextTag("toast"));
            this.message = message;
            this.durationMillis = durationMillis <= 0L ? DEFAULT_DURATION : durationMillis;
            this.maxShowingCount = maxShowingCount <= 0 ? DEFAULT_MAX_COUNT : maxShowingCount;
        }

        @NonNull
        public CharSequence getMessage() {
            return message;
        }

        public long getDurationMillis() {
            return durationMillis;
        }

        public int getMaxShowingCount() {
            return maxShowingCount;
        }

    }
}
