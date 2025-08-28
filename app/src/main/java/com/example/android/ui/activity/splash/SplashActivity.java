package com.example.android.ui.activity.splash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.OptIn;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

import com.example.android.R;
import com.example.android.databinding.ActivitySplashBinding;
import com.example.android.ui.activity.main.MainActivity;
import com.example.core.base.BaseActivity;
import com.example.core.utils.SPUtils;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity<SplashViewModel, ActivitySplashBinding> {

    private final static String NEED_PLAY_VIDEO = "need_play_video";
    private ExoPlayer exoPlayer;
    private PlayerView playerView;
    private boolean isVideoFinished = false;

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
        boolean needPlayVideo = SPUtils.getInstance().getBoolean(NEED_PLAY_VIDEO, true);

        if (needPlayVideo) {
            // 初始化跳过按钮
            initSkipButton();

            // 初始化视频播放器
            initVideoPlayer();

            // 播放视频
            playVideo();
        } else {
            skipToMain();
        }
    }

    private void initSkipButton() {
        binding.btnSkip.setVisibility(View.VISIBLE);
        binding.btnSkip.setOnClickListener(v -> skipToMain());
    }

    @OptIn(markerClass = UnstableApi.class)
    private void initVideoPlayer() {
        // 创建PlayerView并添加到布局中
        playerView = new PlayerView(this);
        playerView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        // 设置播放器属性：铺满屏幕且不显示控制界面
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
        playerView.setUseController(false); // 不显示控制界面

        // 将PlayerView添加到根布局
        binding.getRoot().addView(playerView);

        // 隐藏默认的文本显示
        binding.tvText.setVisibility(View.GONE);

        // 创建ExoPlayer实例
        exoPlayer = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(exoPlayer);

        // 设置播放完成监听器
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_ENDED && !isVideoFinished) {
                    isVideoFinished = true;
                    skipToMain();
                }
            }
        });
    }

    private void playVideo() {
        try {
            // 获取本地MP4文件路径（假设放在raw文件夹中）
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.t1_ui;
            Uri uri = Uri.parse(videoPath);

            // 创建MediaItem
            MediaItem mediaItem = MediaItem.fromUri(uri);

            // 准备播放
            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.prepare();
            exoPlayer.setPlayWhenReady(true);

        } catch (Exception e) {
            e.printStackTrace();
            // 如果视频加载失败，直接跳转到主页面
            skipToMain();
        }
    }

    private void skipToMain() {
        SPUtils.getInstance().putBoolean(NEED_PLAY_VIDEO, false);

        // 跳转到主页面
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        // 停止播放
        if (exoPlayer != null) {
            exoPlayer.stop();
            exoPlayer.release();
        }

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.release();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (exoPlayer != null) {
            exoPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (exoPlayer != null) {
            exoPlayer.play();
        }
    }
}