package com.salam.jambi.player;

import android.content.Context;
import android.media.MediaRecorder;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import com.salam.jambi.data.constant.AppConstants;
import com.salam.jambi.listeners.MediaRecorderListener;
import com.salam.jambi.listeners.PlayerListener;

import java.io.IOException;

public class PlayerManager implements MediaRecorder.OnErrorListener {

    private Context context;

    private static PlayerManager mPlayerManager = null;

    private PlayerListener playerListener;
    private boolean isPlayerPlaying = false;
    private boolean isMediaRecorderOn = false;
    private MediaRecorder mRecorder = null;
    private SimpleExoPlayer mExoPlayer = null;
    private Player.EventListener eventListener = null;
    private MediaRecorderListener mediaRecorderListener;
    private static final int DEFAULT_MIN_BUFFER_MS = 5000;
    private static final int DEFAULT_MAX_BUFFER_MS = 10000;
    private static final int DEFAULT_BUFFER_FOR_PLAYBACK_MS = 3500;


    private PlayerManager() {
    }

    public static PlayerManager getInstance(Context context) {

        context=context;
        if (mPlayerManager == null) {
            mPlayerManager = new PlayerManager();
        }
        return mPlayerManager;
    }

    /*
         DefaultAllocator allocator,
      int minBufferMs,
      int maxBufferMs,
      int bufferForPlaybackMs,
      int bufferForPlaybackAfterRebufferMs,
      int targetBufferBytes,
      boolean prioritizeTimeOverSizeThresholds)
    * */

    public ExoPlayer getExoPlayer(Context context) {
        if (mExoPlayer == null) {
/*            mExoPlayer = ExoPlayerFactory.newSimpleInstance(
                    context,
                    new DefaultRenderersFactory(context),
                    new DefaultTrackSelector(),
                    new DefaultLoadControl(),
                    DEFAULT_MIN_BUFFER_MS,
                    DEFAULT_MAX_BUFFER_MS ,
                    DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                    DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
                    ); */

            mExoPlayer =  ExoPlayerFactory.newSimpleInstance(context, new DefaultRenderersFactory(context), new DefaultTrackSelector());

            AudioAttributes.Builder builder = new AudioAttributes.Builder().setContentType(C.CONTENT_TYPE_MUSIC);
            mExoPlayer.setAudioAttributes(builder.build());
            initExoPlayerListeners();
            mExoPlayer.addListener(eventListener);

            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(context, "my-player");
            DefaultExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            Handler mainHandler = new Handler();

            if (AppConstants.RADIO_URL.contains(AppConstants.MEDIA_FORMAT_M3U8)) {
/*                HlsMediaSource hlsMediaSource = new HlsMediaSource(Uri.parse(AppConstants.RADIO_URL),
                        dataSourceFactory,
                        mainHandler,
                        null);*/

                HlsMediaSource hlsMediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(AppConstants.RADIO_URL));
                mExoPlayer.prepare(hlsMediaSource);
            }
            else {
                MediaSource mediaSource = new ExtractorMediaSource(Uri.parse(AppConstants.RADIO_URL),
                        dataSourceFactory,
                        extractorsFactory,
                        mainHandler,
                        null);
                mExoPlayer.prepare(mediaSource);
            }
        }
        return mExoPlayer;
    }

    // For staring media player;
    public void startExoPlayer(Context context, PlayerListener playerListener) {
        getExoPlayer(context);
        this.playerListener = playerListener;
        mExoPlayer.setPlayWhenReady(true);
    }

    /**
     * ExoPlayer Doesn't start normally after network connected. It needs to prepare with the data source again
     */
    public void resumeExoPlayerWhenNetConnectionAvailable(Context mContext) {
        if (mExoPlayer != null) {
            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(mContext, "my-player");
            DefaultExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            Handler mainHandler = new Handler();
            MediaSource mediaSource = new ExtractorMediaSource(Uri.parse(AppConstants.RADIO_URL),
                    dataSourceFactory,
                    extractorsFactory,
                    mainHandler,
                    null);
            mExoPlayer.prepare(mediaSource);
            mExoPlayer.setPlayWhenReady(true);
        }
    }

    // To pause media player;
    public void pauseExoPlayer() {
        if (mExoPlayer != null && mExoPlayer.getPlayWhenReady()) {
            //Log.d("ExoPlayerTesting", "PlayerManager -> ExoPlayer is going to pause");
            mExoPlayer.setPlayWhenReady(false);
        }
    }

    // To stop player player;
    public void stopExoPlayer() {
        if (mExoPlayer != null) {
            mExoPlayer.removeListener(eventListener);
            mExoPlayer.stop();
            mExoPlayer.release();
            mExoPlayer = null;
            isPlayerPlaying = false;
            playerListener.onPlayerStop();
        }
    }

    // To check if player is playing;
    public boolean isExoPlayerPlaying() {
        if (isPlayerPlaying)
            return true;
        else
            return false;
    }

    private void initExoPlayerListeners() {
        eventListener = new Player.EventListener() {
            /*@Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {

            }*/

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playWhenReady) {
                    if (playbackState == PlaybackState.STATE_PLAYING) {
                        playerListener.onStartPlaying();
                        isPlayerPlaying = true;
                    }
                } else {
                    if (playbackState == PlaybackState.STATE_PLAYING && isPlayerPlaying) {
                        isPlayerPlaying = false;
                        playerListener.onPlayerPause();
                    } else if (playbackState == PlaybackState.STATE_PAUSED && isPlayerPlaying) {
                        isPlayerPlaying = false;
                        playerListener.onPlayerPause();
                    } else if (playbackState == PlaybackState.STATE_STOPPED && isPlayerPlaying) {
                        isPlayerPlaying = false;
                        playerListener.onPlayerPause();
                    }
                }

            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                playerListener.onPlayerError();
            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }
        };
    }

    public void startMediaRecorder(String mFileName, MediaRecorderListener mediaRecorderListener) {
        if (mExoPlayer.getPlayWhenReady() && mRecorder == null) {
            this.mediaRecorderListener = mediaRecorderListener;
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);
                mRecorder.setAudioEncodingBitRate(48000);
            } else {
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                mRecorder.setAudioEncodingBitRate(64000);
            }
            mRecorder.setAudioSamplingRate(16000);
            mRecorder.setOutputFile(mFileName);

            try {
                mRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mRecorder.start();
            this.mediaRecorderListener.onRecordingStart();
            isMediaRecorderOn = true;
        }
    }

    public void stopMediaRecorder() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            mediaRecorderListener.onRecordingStop();
            mediaRecorderListener = null;
            isMediaRecorderOn = false;
        }
    }

    public boolean isMediaRecorderOn() {
        return isMediaRecorderOn;
    }

    @Override
    public void onError(MediaRecorder mediaRecorder, int i, int i1) {
        mediaRecorderListener.onRecordingError();
    }

}
