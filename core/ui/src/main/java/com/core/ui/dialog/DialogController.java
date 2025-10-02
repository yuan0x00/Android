package com.core.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;
import java.util.Map;

public final class DialogController {

    private final Context context;
    private final FragmentManager fragmentManager;
    private final View snackbarAnchor;
    private final Map<String, DialogHandle> handles = new HashMap<>();

    private DialogController(@NonNull Context context,
                             @NonNull FragmentManager fragmentManager,
                             @Nullable View snackbarAnchor) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.snackbarAnchor = snackbarAnchor;
    }

    public static DialogController from(@NonNull FragmentActivity activity, @Nullable View anchor) {
        return new DialogController(activity, activity.getSupportFragmentManager(), anchor);
    }

    public static DialogController from(@NonNull Fragment fragment, @Nullable View anchor) {
        return new DialogController(fragment.requireContext(), fragment.getChildFragmentManager(), anchor);
    }

    public void show(@NonNull DialogEffect effect) {
        if (effect instanceof DialogEffect.Confirm) {
            showConfirm((DialogEffect.Confirm) effect);
        } else if (effect instanceof DialogEffect.Loading) {
            showLoading((DialogEffect.Loading) effect);
        } else if (effect instanceof DialogEffect.Snackbar) {
            showSnackbar((DialogEffect.Snackbar) effect);
        }
    }

    public void dismiss(@NonNull String tag) {
        DialogHandle handle = handles.remove(tag);
        if (handle != null) {
            handle.dismiss();
        }
    }

    public void dismissAll() {
        for (DialogHandle handle : handles.values()) {
            handle.dismiss();
        }
        handles.clear();
    }

    private void showConfirm(DialogEffect.Confirm effect) {
        dismiss(effect.getTag());

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(effect.getTitle())
                .setMessage(effect.getMessage())
                .setPositiveButton(effect.getPositiveText(), (d, which) -> {
                    if (effect.getOnPositive() != null) {
                        effect.getOnPositive().run();
                    }
                })
                .setNegativeButton(effect.getNegativeText(), (d, which) -> {
                    if (effect.getOnNegative() != null) {
                        effect.getOnNegative().run();
                    }
                })
                .create();
        dialog.show();
        handles.put(effect.getTag(), new DialogHandle.Dialog(dialog));
    }

    private void showLoading(DialogEffect.Loading effect) {
        dismiss(effect.getTag());

        LoadingDialogFragment fragment = LoadingDialogFragment.newInstance(effect.isCancelable());
        fragment.show(fragmentManager, effect.getTag());
        handles.put(effect.getTag(), new DialogHandle.Fragment(fragment));
    }

    private void showSnackbar(DialogEffect.Snackbar effect) {
        if (snackbarAnchor == null) {
            return;
        }
        Snackbar snackbar = Snackbar.make(snackbarAnchor, effect.getMessage(), effect.getDuration());
        snackbar.show();
        handles.put(effect.getTag(), new DialogHandle.Snack(snackbar));
    }

    public interface DialogHandle {
        void dismiss();

        final class Dialog implements DialogHandle {
            private final AlertDialog dialog;

            Dialog(AlertDialog dialog) {
                this.dialog = dialog;
            }

            @Override
            public void dismiss() {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }

        final class Fragment implements DialogHandle {
            private final DialogFragment fragment;

            Fragment(DialogFragment fragment) {
                this.fragment = fragment;
            }

            @Override
            public void dismiss() {
                fragment.dismissAllowingStateLoss();
            }
        }

        final class Snack implements DialogHandle {
            private final Snackbar snackbar;

            Snack(Snackbar snackbar) {
                this.snackbar = snackbar;
            }

            @Override
            public void dismiss() {
                snackbar.dismiss();
            }
        }
    }

    public static class LoadingDialogFragment extends DialogFragment {
        private static final String ARG_CANCELABLE = "arg_cancelable";

        public static LoadingDialogFragment newInstance(boolean cancelable) {
            LoadingDialogFragment fragment = new LoadingDialogFragment();
            Bundle args = new Bundle();
            args.putBoolean(ARG_CANCELABLE, cancelable);
            fragment.setArguments(args);
            fragment.setCancelable(cancelable);
            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = new View(requireContext());
            view.setBackgroundColor(0x66000000);
            return view;
        }
    }
}
