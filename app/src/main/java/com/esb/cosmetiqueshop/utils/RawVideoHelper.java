package com.esb.cosmetiqueshop.utils;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.esb.cosmetiqueshop.R;

/**
 * Vidéos res/raw — lecture auto (muet, boucle) avec secours image si échec.
 */
public final class RawVideoHelper {

    private RawVideoHelper() {
    }

    public static @RawRes int resolveRawId(Context context, String rawName) {
        if (TextUtils.isEmpty(rawName)) {
            return 0;
        }
        return context.getResources().getIdentifier(rawName, "raw", context.getPackageName());
    }

    public static void bind(PlayerView playerView, ImageView placeholder, Context context, String rawName) {
        int rawId = resolveRawId(context, rawName);
        if (rawId == 0 && !TextUtils.isEmpty(DemoSeedImages.SAMPLE_HERO_MP4_URL)) {
            playUri(playerView, placeholder, context, DemoSeedImages.SAMPLE_HERO_MP4_URL, true,
                    DemoSeedImages.HERO_DRAWABLE);
            return;
        }
        if (rawId == 0) {
            showImageFallback(playerView, placeholder, DemoSeedImages.HERO_DRAWABLE);
            return;
        }
        playRaw(playerView, placeholder, context, rawId, true, DemoSeedImages.HERO_DRAWABLE);
    }

    /**
     * Carte engagement : image stable (évite 4 ExoPlayer simultanés qui plantent l’émulateur).
     */
    public static void bindEngagementCard(PlayerView playerView,
                                          ImageView placeholder,
                                          Context context,
                                          String preferredRawName,
                                          String fallbackImageDrawable) {
        showImageFallback(playerView, placeholder, "drawable://" + fallbackImageDrawable);
    }

    private static void playRaw(PlayerView playerView, ImageView placeholder,
                                Context context, int rawId, boolean loop,
                                @Nullable String fallbackDrawable) {
        Uri uri = Uri.parse("android.resource://" + context.getPackageName() + "/" + rawId);
        playUri(playerView, placeholder, context, uri.toString(), loop, fallbackDrawable);
    }

    private static void playUri(PlayerView playerView, ImageView placeholder,
                                Context context, String uri, boolean loop,
                                @Nullable String fallbackDrawable) {
        release(playerView);
        placeholder.setVisibility(View.VISIBLE);
        playerView.setVisibility(View.VISIBLE);
        playerView.setUseController(false);
        playerView.setKeepContentOnPlayerReset(true);

        final Context appContext = context.getApplicationContext();
        ExoPlayer player = new ExoPlayer.Builder(appContext).build();
        playerView.setPlayer(player);
        playerView.setTag(R.id.tag_exoplayer, player);

        player.setMediaItem(MediaItem.fromUri(uri));
        player.setRepeatMode(loop ? Player.REPEAT_MODE_ONE : Player.REPEAT_MODE_OFF);
        player.setVolume(0f);
        player.setPlayWhenReady(true);
        player.addListener(new Player.Listener() {
            @Override
            public void onRenderedFirstFrame() {
                placeholder.setVisibility(View.GONE);
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    placeholder.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                // Codec / émulateur : bascule image pour ne pas bloquer l’écran
                showImageFallback(playerView, placeholder,
                        fallbackDrawable != null ? fallbackDrawable : DemoSeedImages.HERO_DRAWABLE);
            }
        });
        try {
            player.prepare();
        } catch (Exception e) {
            showImageFallback(playerView, placeholder,
                    fallbackDrawable != null ? fallbackDrawable : DemoSeedImages.HERO_DRAWABLE);
        }
    }

    private static void showImageFallback(PlayerView playerView, ImageView placeholder, String drawableRef) {
        release(playerView);
        if (playerView != null) {
            playerView.setVisibility(View.GONE);
        }
        if (placeholder != null) {
            placeholder.setVisibility(View.VISIBLE);
            ProductImageLoader.load(placeholder, drawableRef);
        }
    }

    public static void release(PlayerView playerView) {
        if (playerView == null) {
            return;
        }
        Object tag = playerView.getTag(R.id.tag_exoplayer);
        playerView.setPlayer(null);
        playerView.setTag(R.id.tag_exoplayer, null);
        if (tag instanceof ExoPlayer) {
            ExoPlayer player = (ExoPlayer) tag;
            try {
                player.setPlayWhenReady(false);
                player.stop();
                player.clearMediaItems();
                player.release();
            } catch (Exception ignored) {
                // Déjà libéré / thread mort — ignorer
            }
        }
    }
}
