package com.rapid.android.ui.base;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.media3.exoplayer.ExoPlayer;

import org.jetbrains.annotations.NotNull;

public class SplashLifecycleObserver implements LifecycleEventObserver {

    private final ExoPlayer exoPlayer;

    public SplashLifecycleObserver(ExoPlayer exoPlayer) {
        this.exoPlayer = exoPlayer;
    }

    @Override
    public void onStateChanged(@NotNull LifecycleOwner source, Lifecycle.@NotNull Event event) {
        if (event == Lifecycle.Event.ON_RESUME) {
            if (exoPlayer != null) {
                exoPlayer.play();
            }
        } else if (event == Lifecycle.Event.ON_PAUSE) {
            if (exoPlayer != null) {
                exoPlayer.pause();
            }
        } else if (event == Lifecycle.Event.ON_DESTROY) {
            if (exoPlayer != null) {
                exoPlayer.release();
            }
        }
    }
}