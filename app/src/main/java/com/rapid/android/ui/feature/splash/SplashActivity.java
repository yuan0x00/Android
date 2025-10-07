package com.rapid.android.ui.feature.splash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import androidx.annotation.OptIn;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;

import com.rapid.android.R;
import com.rapid.android.core.common.data.StorageManager;
import com.rapid.android.core.ui.presentation.BaseActivity;
import com.rapid.android.databinding.ActivitySplashBinding;
import com.rapid.android.ui.feature.main.MainActivity;

import java.util.concurrent.atomic.AtomicBoolean;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity<SplashViewModel, ActivitySplashBinding> {

    private static final String NEED_PLAY_VIDEO = "need_play_video";
    private final AtomicBoolean isSkipClicked = new AtomicBoolean(false);
    private ExoPlayer exoPlayer;
    private boolean isInitializationFinished = false;
    private boolean needPlayVideo = true;

    @Override
    protected SplashViewModel createViewModel() {
        return new ViewModelProvider(this).get(SplashViewModel.class);
    }

    @Override
    protected ActivitySplashBinding createViewBinding() {
        return ActivitySplashBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initializeViews() {
        needPlayVideo = StorageManager.getBoolean(NEED_PLAY_VIDEO, true);

        if (needPlayVideo) {
            binding.loadingIndicator.setVisibility(View.GONE);
            initVideoPlayer();
            playVideo();
        } else {
            binding.loadingIndicator.setVisibility(View.VISIBLE);
        }

        // 模拟初始化，完成后显示跳过按钮
        simulateInitialization();
    }

    @OptIn(markerClass = UnstableApi.class)
    private void initVideoPlayer() {

        binding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
        binding.playerView.setUseController(false);

        exoPlayer = new ExoPlayer.Builder(this).build();
        binding.playerView.setPlayer(exoPlayer);

        // 使用 LifecycleObserver 管理播放器生命周期
        SplashLifecycleObserver observer = new SplashLifecycleObserver(exoPlayer);
        getLifecycle().addObserver(observer);

        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onRenderedFirstFrame() {
                Player.Listener.super.onRenderedFirstFrame();
                binding.playerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_ENDED) {
                    maybeSkipToMain();
                }
            }
        });
    }

    private void playVideo() {
        try {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.t1_ui;
            Uri uri = Uri.parse(videoPath);
            MediaItem mediaItem = MediaItem.fromUri(uri);

            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.prepare();
            exoPlayer.setPlayWhenReady(true);

        } catch (Exception e) {
            maybeSkipToMain();
        }
    }

    private void simulateInitialization() {
//        int delay = new Random().nextInt(1000);
        int delay = 0;

        new android.os.Handler(getMainLooper()).postDelayed(() -> {
//            ToastUtils.showShortToast("模拟启动耗时" + delay + "ms");
            isInitializationFinished = true;
            // 初始化完成，显示跳过按钮
            if (needPlayVideo) {
                binding.btnSkip.setVisibility(View.VISIBLE);
                binding.btnSkip.setOnClickListener(v -> maybeSkipToMain());
            }
            // 同时检查是否可以跳转
            if (!needPlayVideo) {
                maybeSkipToMain();
            }
        }, delay);
    }

    private void maybeSkipToMain() {
        boolean canProceed = isInitializationFinished;
        if (canProceed && !isSkipClicked.get()) {
            isSkipClicked.set(true);
            performSkip();
        }
    }

    private void performSkip() {
        StorageManager.putBoolean(NEED_PLAY_VIDEO, false);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}