package com.core.ui.dialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.core.ui.presentation.BaseDialogFragment;

public abstract class DialogEffect {

    private final String tag;

    protected DialogEffect(@NonNull String tag) {
        this.tag = tag;
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

        public Confirm(@NonNull String tag,
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

        public Loading(@NonNull String tag, boolean cancelable) {
            super(tag);
            this.cancelable = cancelable;
        }

        public boolean isCancelable() {
            return cancelable;
        }
    }

    public static final class Snackbar extends DialogEffect {
        private final CharSequence message;
        private final int duration;

        public Snackbar(@NonNull String tag, @NonNull CharSequence message, int duration) {
            super(tag);
            this.message = message;
            this.duration = duration;
        }

        @NonNull
        public CharSequence getMessage() {
            return message;
        }

        public int getDuration() {
            return duration;
        }
    }

    public static final class Custom extends DialogEffect {
        private final BaseDialogFragment fragment;
        @Nullable private final Boolean cancelable;
        @Nullable private final Boolean dimEnabled;
        @Nullable private final Float dimAmount;

        public Custom(@NonNull String tag, @NonNull BaseDialogFragment fragment) {
            this(tag, fragment, null, null, null);
        }

        public Custom(@NonNull String tag,
                      @NonNull BaseDialogFragment fragment,
                      @Nullable Boolean cancelable,
                      @Nullable Boolean dimEnabled,
                      @Nullable Float dimAmount) {
            super(tag);
            this.fragment = fragment;
            this.cancelable = cancelable;
            this.dimEnabled = dimEnabled;
            this.dimAmount = dimAmount;
        }

        @NonNull
        public BaseDialogFragment getFragment() {
            return fragment;
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
}
