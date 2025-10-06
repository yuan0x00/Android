package com.core.ui.components.dialog;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.core.ui.R;

final class ConfirmDialogView extends BaseDialogView {

    private TextView titleView;
    private TextView messageView;
    private Button positiveButton;
    private Button negativeButton;

    @Nullable
    private Runnable positiveAction;
    @Nullable
    private Runnable negativeAction;

    ConfirmDialogView(@NonNull Context context) {
        super(context);
        init();
    }

    ConfirmDialogView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    ConfirmDialogView(@NonNull Context context,
                      @Nullable AttributeSet attrs,
                      int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setDialogCancelable(true);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.core_ui_dialog_confirm;
    }

    @Override
    protected void onDialogContentCreated(@NonNull View view) {
        super.onDialogContentCreated(view);
        titleView = view.findViewById(R.id.core_ui_confirm_title);
        messageView = view.findViewById(R.id.core_ui_confirm_message);
        positiveButton = view.findViewById(R.id.core_ui_confirm_positive);
        negativeButton = view.findViewById(R.id.core_ui_confirm_negative);
    }

    @Override
    protected void initListener() {
        super.initListener();
        positiveButton.setOnClickListener(v -> {
            if (positiveAction != null) {
                positiveAction.run();
            }
        });
        negativeButton.setOnClickListener(v -> {
            if (negativeAction != null) {
                negativeAction.run();
            }
        });
    }

    void setTitle(@Nullable CharSequence title) {
        if (TextUtils.isEmpty(title)) {
            titleView.setVisibility(View.GONE);
        } else {
            titleView.setVisibility(View.VISIBLE);
            titleView.setText(title);
        }
    }

    void setMessage(@Nullable CharSequence message) {
        if (TextUtils.isEmpty(message)) {
            messageView.setVisibility(View.GONE);
        } else {
            messageView.setVisibility(View.VISIBLE);
            messageView.setText(message);
        }
    }

    void setPositiveButton(@Nullable CharSequence text, @NonNull Runnable action) {
        CharSequence displayText = TextUtils.isEmpty(text)
                ? getContext().getString(android.R.string.ok)
                : text;
        positiveButton.setText(displayText);
        positiveButton.setVisibility(View.VISIBLE);
        positiveAction = action;
    }

    void setNegativeButton(@Nullable CharSequence text, @Nullable Runnable action) {
        if (TextUtils.isEmpty(text) && action == null) {
            negativeButton.setVisibility(View.GONE);
            negativeAction = null;
            return;
        }
        CharSequence displayText = TextUtils.isEmpty(text)
                ? getContext().getString(android.R.string.cancel)
                : text;
        negativeButton.setText(displayText);
        negativeButton.setVisibility(View.VISIBLE);
        negativeAction = action;
    }
}
