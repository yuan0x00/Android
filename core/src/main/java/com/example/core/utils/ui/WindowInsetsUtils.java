package com.example.core.utils.ui;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsCompat.Type;

/**
 * WindowInsets 工具类（适配状态栏、导航栏、键盘、刘海屏等）
 * 兼容 API 21+，使用 androidx.core.view.WindowInsetsCompat
 */
public final class WindowInsetsUtils {

    private WindowInsetsUtils() {
        throw new UnsupportedOperationException("WindowInsetsUtils cannot be instantiated");
    }

    // —————— 自动应用系统栏 padding（最常用） ——————

    /**
     * 为 View 自动设置系统栏 padding（状态栏 + 导航栏）
     * 适用于：根布局、CoordinatorLayout、ConstraintLayout 等
     * 使用后，内容不会被系统栏遮挡
     */
    public static void applySystemWindowInsets(@NonNull View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            v.setPadding(
                    insets.getInsets(Type.systemBars()).left,
                    insets.getInsets(Type.systemBars()).top,
                    insets.getInsets(Type.systemBars()).right,
                    insets.getInsets(Type.systemBars()).bottom
            );
            return insets; // 不消费，允许子 View 继续处理
        });
    }

    /**
     * 为 View 设置仅顶部（状态栏）padding
     */
    public static void applyTopSystemWindowInsets(@NonNull View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            v.setPadding(
                    v.getPaddingLeft(),
                    insets.getInsets(Type.statusBars()).top,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });
    }

    /**
     * 为 View 设置仅底部（导航栏）padding
     */
    public static void applyBottomSystemWindowInsets(@NonNull View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    insets.getInsets(Type.navigationBars()).bottom
            );
            return insets;
        });
    }

    // —————— 消费（consume）特定 Insets ——————

    /**
     * 消费所有系统栏 insets（子 View 不再收到）
     * 适用于：DrawerLayout、BottomSheet、全屏视频等
     */
    public static void consumeSystemWindowInsets(@NonNull View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            // 构建消费后的 insets（系统栏设为 0）
            WindowInsetsCompat.Builder builder = new WindowInsetsCompat.Builder(insets);
            builder.setInsets(Type.systemBars(), Insets.of(0, 0, 0, 0));
            WindowInsetsCompat consumed = builder.build();

            // 设置 padding（可选）
            Insets systemBars = insets.getInsets(Type.systemBars());
            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );

            return consumed; // 返回消费后的 insets，子 View 不会再收到系统栏 inset
        });
    }

    /**
     * 仅消费底部导航栏 insets
     */
    public static void consumeBottomInsets(@NonNull View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            WindowInsetsCompat.Builder builder = new WindowInsetsCompat.Builder(insets);
            builder.setInsets(Type.navigationBars(), Insets.of(0, 0, 0, 0)); // 消费底部导航栏
            WindowInsetsCompat consumed = builder.build();

            Insets navBars = insets.getInsets(Type.navigationBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    navBars.bottom
            );

            return consumed;
        });
    }

    /**
     * 仅消费顶部状态栏 insets
     */
    public static void consumeTopInsets(@NonNull View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            WindowInsetsCompat.Builder builder = new WindowInsetsCompat.Builder(insets);
            builder.setInsets(Type.statusBars(), Insets.of(0, 0, 0, 0)); // 消费顶部状态栏
            WindowInsetsCompat consumed = builder.build();

            Insets statusBars = insets.getInsets(Type.statusBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    statusBars.top,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );

            return consumed;
        });
    }

    // —————— 获取当前 Insets 值（静态获取，非监听） ——————

    /**
     * 获取当前 View 的系统栏顶部 inset（状态栏高度）
     */
    public static int getTopSystemInset(@NonNull View view) {
        WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(view);
        if (insets != null) {
            return insets.getInsets(Type.statusBars()).top;
        }
        return 0;
    }

    /**
     * 获取当前 View 的系统栏底部 inset（导航栏高度）
     */
    public static int getBottomSystemInset(@NonNull View view) {
        WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(view);
        if (insets != null) {
            return insets.getInsets(Type.navigationBars()).bottom;
        }
        return 0;
    }

    // —————— 工具方法：移除监听器 ——————

    /**
     * 移除 View 的 WindowInsets 监听器（恢复默认行为）
     */
    public static void removeOnApplyWindowInsetsListener(@NonNull View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, null);
    }

    // —————— 高级：监听 Insets 变化（如键盘弹出） ——————

    /**
     * 监听 IME（键盘）弹出/隐藏
     *
     * @param onImeVisibilityChangeListener 回调：isVisible=true 表示键盘弹出
     */
    public static void addImeVisibilityListener(
            @NonNull View view,
            @NonNull OnImeVisibilityChangeListener onImeVisibilityChangeListener) {

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets imeInsets = insets.getInsets(Type.ime());
            boolean imeVisible = imeInsets.bottom > 0 || imeInsets.top > 0;
            onImeVisibilityChangeListener.onImeVisibilityChanged(imeVisible);
            return insets;
        });
    }

    public interface OnImeVisibilityChangeListener {
        void onImeVisibilityChanged(boolean isVisible);
    }
}