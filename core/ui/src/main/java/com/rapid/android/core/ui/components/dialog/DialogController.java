package com.rapid.android.core.ui.components.dialog;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import java.lang.ref.WeakReference;
import java.util.*;

public final class DialogController {

    @Nullable
    private final WeakReference<View> anchorRef;
    private final Map<String, DialogHandle> handles = new HashMap<>();
    private final LinkedHashMap<String, ToastEntry> toastEntries = new LinkedHashMap<>();
    @Nullable
    private WeakReference<Context> contextRef;
    @Nullable
    private BaseOverlayLayout dialogOverlayLayout;
    @Nullable
    private BaseOverlayLayout toastOverlayLayout;
    @Nullable
    private LifecycleOwner attachedLifecycleOwner;
    @Nullable
    private LifecycleEventObserver lifecycleObserver;

    private DialogController(@NonNull Context context,
                             @Nullable View anchor) {
        this.contextRef = new WeakReference<>(context);
        this.anchorRef = anchor != null ? new WeakReference<>(anchor) : null;
    }

    public static DialogController from(@NonNull FragmentActivity activity, @Nullable View anchor) {
        View resolvedAnchor = anchor != null ? anchor : activity.findViewById(android.R.id.content);
        DialogController controller = new DialogController(activity, resolvedAnchor);
        controller.registerLifecycle(activity);
        return controller;
    }

    public static DialogController from(@NonNull Fragment fragment, @Nullable View anchor) {
        View resolvedAnchor = anchor;
        if (resolvedAnchor == null && fragment.getView() != null) {
            resolvedAnchor = fragment.getView();
        }
        if (resolvedAnchor == null) {
            resolvedAnchor = fragment.requireActivity().findViewById(android.R.id.content);
        }
        DialogController controller = new DialogController(fragment.requireContext(), resolvedAnchor);
        controller.registerLifecycle(fragment);
        return controller;
    }

    @Nullable
    private Context getContextOrNull() {
        Context context = contextRef != null ? contextRef.get() : null;
        if (context != null) {
            return context;
        }
        View anchor = getAnchor();
        if (anchor != null) {
            Context anchorContext = anchor.getContext();
            contextRef = new WeakReference<>(anchorContext);
            return anchorContext;
        }
        return null;
    }

    @NonNull
    private Context requireContext() {
        Context context = getContextOrNull();
        if (context == null) {
            throw new IllegalStateException("DialogController context is no longer available.");
        }
        return context;
    }

    @Nullable
    private View getAnchor() {
        return anchorRef != null ? anchorRef.get() : null;
    }

    private void assertMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("DialogController methods must be called on the main thread.");
        }
    }

    public void show(@NonNull DialogEffect effect) {
        assertMainThread();
        if (effect instanceof DialogEffect.Confirm) {
            showConfirm((DialogEffect.Confirm) effect);
        } else if (effect instanceof DialogEffect.Loading) {
            showLoading((DialogEffect.Loading) effect);
        } else if (effect instanceof DialogEffect.Toast) {
            showToast((DialogEffect.Toast) effect);
        } else if (effect instanceof DialogEffect.Custom) {
            showCustom((DialogEffect.Custom) effect);
        }
    }

    public void showToast(@NonNull CharSequence message) {
        assertMainThread();
        show(new DialogEffect.Toast(message));
    }

    public void dismiss(@NonNull String tag) {
        assertMainThread();
        DialogHandle handle = handles.remove(tag);
        if (handle != null) {
            handle.dismiss();
        }
    }

    public void dismissAll() {
        assertMainThread();
        List<DialogHandle> snapshot = new ArrayList<>(handles.values());
        handles.clear();
        for (DialogHandle handle : snapshot) {
            handle.dismiss();
        }
    }

    private void registerLifecycle(@NonNull LifecycleOwner owner) {
        if (attachedLifecycleOwner == owner) {
            return;
        }
        detachLifecycle();
        lifecycleObserver = (source, event) -> {
            if (event == Lifecycle.Event.ON_DESTROY) {
                handleLifecycleDestroy();
            }
        };
        attachedLifecycleOwner = owner;
        owner.getLifecycle().addObserver(lifecycleObserver);
    }

    private void detachLifecycle() {
        if (attachedLifecycleOwner != null && lifecycleObserver != null) {
            attachedLifecycleOwner.getLifecycle().removeObserver(lifecycleObserver);
        }
        lifecycleObserver = null;
        attachedLifecycleOwner = null;
    }

    private void handleLifecycleDestroy() {
        dismissAll();
        clearToastEntries();
        clearOverlays();
        contextRef = null;
        detachLifecycle();
    }

    private void showConfirm(DialogEffect.Confirm effect) {
        dismiss(effect.getTag());

        Context context = requireContext();
        ConfirmDialogView dialogView = new ConfirmDialogView(context);
        dialogView.setTitle(effect.getTitle());
        dialogView.setMessage(effect.getMessage());
        dialogView.setPositiveButton(effect.getPositiveText(), () -> {
            dismiss(effect.getTag());
            if (effect.getOnPositive() != null) {
                effect.getOnPositive().run();
            }
        });
        if (!TextUtils.isEmpty(effect.getNegativeText()) || effect.getOnNegative() != null) {
            dialogView.setNegativeButton(effect.getNegativeText(), () -> {
                dismiss(effect.getTag());
                if (effect.getOnNegative() != null) {
                    effect.getOnNegative().run();
                }
            });
        } else {
            dialogView.setNegativeButton(null, null);
        }
        showDialogView(effect.getTag(), dialogView, null, null, null, null);
    }

    private void showLoading(DialogEffect.Loading effect) {
        dismiss(effect.getTag());

        Context context = requireContext();
        LoadingDialogView dialogView = new LoadingDialogView(context);
        dialogView.setDialogCancelable(effect.isCancelable());
        showDialogView(effect.getTag(), dialogView, null, null, null, null);
    }

    private void showToast(DialogEffect.Toast effect) {
        int maxCount = effect.getMaxShowingCount();
        if (maxCount <= 0) {
            maxCount = 2;
        }
        while (toastEntries.size() >= maxCount) {
            Map.Entry<String, ToastEntry> oldest = toastEntries.entrySet().iterator().next();
            toastEntries.remove(oldest.getKey());
            ToastEntry oldestEntry = oldest.getValue();
            oldestEntry.cancelAutoDismiss();
            dismiss(oldest.getKey());
        }
        repositionToasts();

        String tag = effect.getTag();
        dismiss(tag);
        toastEntries.remove(tag);

        Context context = requireContext();
        ToastDialogView toastView = new ToastDialogView(context);
        toastView.setMessage(effect.getMessage());
        toastView.setOnDismissListener(() -> removeToastEntry(tag));
        ViewCompat.setTranslationZ(toastView, 10f);

        ToastEntry entry = new ToastEntry(tag, toastView);
        toastEntries.put(tag, entry);

        BaseOverlayLayout toastOverlay = ensureToastOverlay();
        removeFromParent(toastView);
        toastView.setOnDismissRequestListener(view -> dismiss(tag));
        toastOverlay.setVisibility(View.VISIBLE);
        toastOverlay.addView(toastView);
        toastView.playEnterAnimation();
        handles.put(tag, new DialogHandle.Overlay(toastOverlay, toastView, () -> removeToastEntry(tag)));

        Runnable autoDismiss = () -> dismiss(tag);
        entry.setAutoDismiss(autoDismiss, effect.getDurationMillis());
        repositionToasts();
    }

    private void showCustom(DialogEffect.Custom effect) {
        dismiss(effect.getTag());
        showDialogView(effect.getTag(),
                effect.getDialogView(),
                effect.getCancelableOverride(),
                effect.getDimEnabledOverride(),
                effect.getDimAmountOverride(),
                null);
    }

    private void showDialogView(@NonNull String tag,
                                @NonNull BaseDialogView dialogView,
                                @Nullable Boolean cancelableOverride,
                                @Nullable Boolean dimEnabledOverride,
                                @Nullable Float dimAmountOverride,
                                @Nullable Runnable onDismissed) {
        if (cancelableOverride != null) {
            dialogView.setDialogCancelable(cancelableOverride);
        }
        if (dimEnabledOverride != null) {
            dialogView.setDimEnabled(dimEnabledOverride);
        }
        if (dimAmountOverride != null) {
            dialogView.setDimAmount(dimAmountOverride);
        }

        BaseOverlayLayout overlay = ensureDialogOverlay();
        removeFromParent(dialogView);
        dialogView.setOnDismissRequestListener(view -> dismiss(tag));
        overlay.setVisibility(View.VISIBLE);
        overlay.addView(dialogView);
        dialogView.playEnterAnimation();
        handles.put(tag, new DialogHandle.Overlay(overlay, dialogView, onDismissed));
    }

    private BaseOverlayLayout ensureDialogOverlay() {
        ViewGroup parent = resolveOverlayParent();
        if (dialogOverlayLayout == null) {
            dialogOverlayLayout = new DialogOverlayLayout(parent.getContext());
        }
        attachOverlayToParent(dialogOverlayLayout, parent);
        return dialogOverlayLayout;
    }

    private BaseOverlayLayout ensureToastOverlay() {
        ViewGroup parent = resolveOverlayParent();
        if (toastOverlayLayout == null) {
            toastOverlayLayout = new ToastOverlayLayout(parent.getContext());
        }
        attachOverlayToParent(toastOverlayLayout, parent);
        return toastOverlayLayout;
    }

    private void attachOverlayToParent(@NonNull BaseOverlayLayout overlay, @NonNull ViewGroup targetParent) {
        ViewParent currentParent = overlay.getParent();
        if (currentParent == targetParent) {
            return;
        }
        if (currentParent instanceof ViewGroup) {
            ((ViewGroup) currentParent).removeView(overlay);
        }
        targetParent.addView(overlay);
    }

    private ViewGroup resolveOverlayParent() {
        View candidate = getAnchor();
        Context context = getContextOrNull();
        if (candidate == null && context instanceof Activity) {
            candidate = ((Activity) context).findViewById(android.R.id.content);
        }
        if (candidate == null) {
            throw new IllegalStateException("DialogController requires an anchor view attached to a ViewGroup.");
        }
        ViewGroup parent = findSuitableParent(candidate);
        if (parent != null) {
            return parent;
        }
        if (candidate instanceof ViewGroup) {
            return (ViewGroup) candidate;
        }
        View root = candidate.getRootView();
        if (root instanceof ViewGroup) {
            return (ViewGroup) root;
        }
        if (context instanceof Activity) {
            View content = ((Activity) context).findViewById(android.R.id.content);
            if (content instanceof ViewGroup) {
                return (ViewGroup) content;
            }
        }
        throw new IllegalStateException("Unable to locate a ViewGroup to host dialog overlays.");
    }

    @Nullable
    private ViewGroup findSuitableParent(@NonNull View start) {
        View current = start;
        ViewGroup fallback = null;
        while (current != null) {
            if (current instanceof FrameLayout && current.getId() == android.R.id.content) {
                return (ViewGroup) current;
            }
            if (current instanceof ViewGroup) {
                fallback = (ViewGroup) current;
            }
            ViewParent parent = current.getParent();
            if (!(parent instanceof View)) {
                break;
            }
            current = (View) parent;
        }
        return fallback;
    }

    private void removeFromParent(@NonNull BaseDialogView dialogView) {
        ViewParent parent = dialogView.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(dialogView);
        }
    }

    private void removeToastEntry(@NonNull String tag) {
        ToastEntry entry = toastEntries.remove(tag);
        if (entry != null) {
            entry.cancelAutoDismiss();
        }
        repositionToasts();
        if (toastEntries.isEmpty() && toastOverlayLayout != null) {
            toastOverlayLayout.setVisibility(View.GONE);
        }
    }

    private void clearToastEntries() {
        for (ToastEntry entry : toastEntries.values()) {
            entry.cancelAutoDismiss();
        }
        toastEntries.clear();
        if (toastOverlayLayout != null) {
            toastOverlayLayout.removeAllViews();
            toastOverlayLayout.setVisibility(View.GONE);
        }
    }

    private void clearOverlays() {
        if (dialogOverlayLayout != null) {
            dialogOverlayLayout.removeAllViews();
            dialogOverlayLayout.setVisibility(View.GONE);
            ViewParent parent = dialogOverlayLayout.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(dialogOverlayLayout);
            }
        }
        if (toastOverlayLayout != null) {
            toastOverlayLayout.removeAllViews();
            toastOverlayLayout.setVisibility(View.GONE);
            ViewParent parent = toastOverlayLayout.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(toastOverlayLayout);
            }
        }
        dialogOverlayLayout = null;
        toastOverlayLayout = null;
    }

    private void repositionToasts() {
        int total = toastEntries.size();
        int index = 0;
        for (ToastEntry entry : toastEntries.values()) {
            entry.applyPosition(index, total);
            index++;
        }
    }

    public interface DialogHandle {
        void dismiss();

        final class Overlay implements DialogHandle {
            private final BaseOverlayLayout overlay;
            private final BaseDialogView dialogView;
            @Nullable
            private final Runnable onDismissed;
            private boolean dismissed;

            Overlay(BaseOverlayLayout overlay, BaseDialogView dialogView, @Nullable Runnable onDismissed) {
                this.overlay = overlay;
                this.dialogView = dialogView;
                this.onDismissed = onDismissed;
            }

            @Override
            public void dismiss() {
                if (dismissed) {
                    return;
                }
                dismissed = true;
                if (!ViewCompat.isAttachedToWindow(dialogView)) {
                    overlay.removeDialog(dialogView);
                    dialogView.notifyDismissed();
                    if (onDismissed != null) {
                        onDismissed.run();
                    }
                    return;
                }
                dialogView.playExitAnimation(() -> {
                    overlay.removeDialog(dialogView);
                    dialogView.notifyDismissed();
                    if (onDismissed != null) {
                        onDismissed.run();
                    }
                });
            }
        }

    }

    private abstract static class BaseOverlayLayout extends FrameLayout {

        BaseOverlayLayout(@NonNull Context context) {
            super(context);
            setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            setClickable(false);
            setFocusable(false);
            setVisibility(GONE);
            setClipToPadding(false);
            setClipChildren(false);
        }

        void removeDialog(@NonNull BaseDialogView dialogView) {
            Runnable action = () -> {
                dialogView.clearAnimation();
                if (dialogView.getParent() == this) {
                    removeView(dialogView);
                }
                if (getChildCount() == 0) {
                    setVisibility(GONE);
                }
            };
            if (ViewCompat.isAttachedToWindow(this)) {
                post(action);
            } else {
                action.run();
            }
        }
    }

    private static final class DialogOverlayLayout extends BaseOverlayLayout {
        DialogOverlayLayout(@NonNull Context context) {
            super(context);
        }
    }

    private static final class ToastOverlayLayout extends BaseOverlayLayout {
        ToastOverlayLayout(@NonNull Context context) {
            super(context);
            ViewCompat.setTranslationZ(this, 50f);
        }
    }

    private static final class ToastEntry {
        private final String tag;
        private final ToastDialogView dialogView;
        @Nullable
        private Runnable autoDismissRunnable;

        ToastEntry(@NonNull String tag,
                   @NonNull ToastDialogView dialogView) {
            this.tag = tag;
            this.dialogView = dialogView;
        }

        void setAutoDismiss(@NonNull Runnable runnable, long delayMillis) {
            cancelAutoDismiss();
            autoDismissRunnable = runnable;
            dialogView.postDelayed(autoDismissRunnable, delayMillis);
        }

        void cancelAutoDismiss() {
            if (autoDismissRunnable != null) {
                dialogView.removeCallbacks(autoDismissRunnable);
                autoDismissRunnable = null;
            }
        }

        void applyPosition(int index, int total) {
            dialogView.applyPosition(index, total);
        }
    }
}
