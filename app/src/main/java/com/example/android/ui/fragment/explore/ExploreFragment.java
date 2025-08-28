package com.example.android.ui.fragment.explore;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.example.android.databinding.FragmentExploreBinding;
import com.example.core.base.BaseFragment;

import org.jetbrains.annotations.NotNull;

public class ExploreFragment extends BaseFragment<ExploreViewModel, FragmentExploreBinding> {
    private PlayerView playerView;
    private ExoPlayer player;

    @Override
    protected ExploreViewModel createViewModel() {
        return new ViewModelProvider(this).get(ExploreViewModel.class);
    }

    @Override
    protected FragmentExploreBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentExploreBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initializeViews() {
        playerView = binding.playerView;
    }

    private void initializePlayer() {
        if (player == null) {
            player = new ExoPlayer.Builder(this.requireContext()).build();
            playerView.setPlayer(player);

            String videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
            MediaItem mediaItem = MediaItem.fromUri(videoUrl);
            player.setMediaItem(mediaItem);

            // 添加错误监听
            player.addListener(new PlayerEventListener());

            player.prepare();
            player.setPlayWhenReady(true);
        }
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (player == null) {
            initializePlayer();
        }
        if (Build.VERSION.SDK_INT >= 24) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT < 24 || player == null) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT < 24) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT >= 24) {
            releasePlayer();
        }
    }

    private class PlayerEventListener implements Player.Listener {
        @Override
        public void onPlayerError(@NotNull PlaybackException error) {
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "播放错误: " + error.getMessage(), Toast.LENGTH_LONG).show()
            );
        }
    }
}