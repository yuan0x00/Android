package com.rapid.android.core.ui.components.popup;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.rapid.android.core.ui.R;

/**
 * A custom PopupWindow styled according to Material Design 3 guidelines.
 * Features: card-like background, dynamic theming, elevation, smooth animations,
 * and touch-position display with edge avoidance.
 * Uses Builder pattern for flexible configuration.
 */
public class BasePopupWindow extends PopupWindow {

    private final Context context;
    private View contentView;

    private BasePopupWindow(Builder builder) {
        super(builder.contentView, builder.width, builder.height, builder.focusable);
        this.context = builder.context;
        this.contentView = builder.contentView;

        // Apply styling
        setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.core_ui_base_popup_background));
        setElevation(6f); // Level 2 elevation for cards
        setOutsideTouchable(true);
        setTouchable(true);

        // Apply animation
        setAnimationStyle(R.style.CoreUiBasePopupAnimation);
    }

    @Override
    public void showAsDropDown(View anchor) {
        super.showAsDropDown(anchor);
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
    }

    /**
     * Shows the PopupWindow with its bottom edge aligned to the specified touch position's y-coordinate
     * and right edge aligned to the touch position's x-coordinate, avoiding screen edges.
     *
     * @param anchor The anchor view relative to which the popup is positioned.
     * @param touchX The x-coordinate of the touch event in screen coordinates.
     * @param touchY The y-coordinate of the touch event in screen coordinates.
     */
    public void showAtTouchPosition(View anchor, float touchX, float touchY) {
        // Measure content view to get its size
        contentView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        int popupWidth = contentView.getMeasuredWidth();
        int popupHeight = contentView.getMeasuredHeight();

        // Get screen dimensions
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        // Convert touch coordinates to anchor-relative coordinates, aligning bottom edge to touchY and right edge to touchX
        int[] anchorLocation = new int[2];
        anchor.getLocationOnScreen(anchorLocation);
        int x = (int) touchX - anchorLocation[0] - (popupWidth / 2); // Align center to touchX
        int y = (int) touchY - anchorLocation[1] - anchor.getHeight() - popupHeight; // Align bottom edge to touchY relative to anchor bottom

        // Adjust position to avoid anchor edges (x-axis only)
        if (x + popupWidth > anchor.getWidth()) {
            x = anchor.getWidth() - popupWidth; // Shift left if exceeding anchor right
        }
        if (x < 0) {
            x = 0; // Prevent exceeding anchor left
        }

        // Further adjust for screen boundaries
        int[] windowLocation = new int[2];
        anchor.getRootView().getLocationOnScreen(windowLocation);
        int absoluteX = anchorLocation[0] + x;
        int absoluteY = anchorLocation[1] + y + anchor.getHeight(); // Convert y back to screen coordinates

        if (absoluteX + popupWidth > screenWidth) {
            x -= (absoluteX + popupWidth - screenWidth); // Shift left to fit screen
        }
        if (absoluteY + popupHeight > screenHeight) {
            y -= (absoluteY + popupHeight - screenHeight); // Shift up to fit screen
        }
        if (absoluteX < 0) {
            x -= absoluteX; // Shift right to fit screen
        }
        if (absoluteY < 0) {
            y -= absoluteY; // Shift down to fit screen
        }

        // Show popup at calculated position
        showAsDropDown(anchor, x, y);
    }

    /**
     * Builder class for configuring PopupWindow.
     */
    public static class Builder {
        private final Context context;
        private View contentView;
        private int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        private int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        private boolean focusable = true;

        public Builder(@NonNull Context context) {
            this.context = context;
            this.contentView = LayoutInflater.from(context).inflate(R.layout.core_ui_base_popup_layout, null);
        }

        public Builder setContentView(@NonNull View contentView) {
            this.contentView = contentView;
            return this;
        }

        public Builder setContentView(int layoutResId) {
            this.contentView = LayoutInflater.from(context).inflate(layoutResId, null);
            return this;
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setFocusable(boolean focusable) {
            this.focusable = focusable;
            return this;
        }

        public BasePopupWindow build() {
            if (contentView == null) {
                throw new IllegalStateException("ContentView must be set.");
            }
            return new BasePopupWindow(this);
        }
    }
}
