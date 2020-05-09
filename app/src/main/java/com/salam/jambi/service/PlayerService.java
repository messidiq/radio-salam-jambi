package com.salam.jambi.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.salam.jambi.data.constant.AppConstants;
import com.salam.jambi.listeners.MediaRecorderListener;
import com.salam.jambi.listeners.PlayerListener;
import com.salam.jambi.player.PlayerManager;
import com.salam.jambi.utils.AppUtility;
import com.salam.jambi.utils.NotificationUtils;


public class PlayerService extends Service {

    private PlayerManager mPlayerManager;
    private static PlayerService playerService;
    private final IBinder mBinder = new LocalBinder();
    private static int lastState = TelephonyManager.CALL_STATE_IDLE;

    /**
     * Start Player Service Methods
     */
    public PlayerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startPlayer();
        pushServiceToForeground();
        initializePhoneStateListener();
        playerService = this;
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }

    public class LocalBinder extends Binder {
        public PlayerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return PlayerService.this;
        }
    }

    public void pushServiceToForeground() {
        startForeground(AppConstants.FOREGROUND_NOTIFICATION_REQUEST_CODE, NotificationUtils.generateNotification(getApplicationContext()));
    }
    /** End Player Service Methods */


    /*** Start Player Controlling Methods*/
    public void startPlayer() {

        mPlayerManager = PlayerManager.getInstance(getApplicationContext());
        mPlayerManager.startExoPlayer(getApplicationContext(), new PlayerListener() {

            @Override
            public void onStartPlaying() {
                AppUtility.sendBroadCastMessages(getApplicationContext(), AppConstants.ON_START_PLAYING);
            }

            @Override
            public void onPlayerPause() {
                AppUtility.sendBroadCastMessages(getApplicationContext(), AppConstants.ON_PLAYER_PAUSE);
            }

            @Override
            public void onPlayerStop() {
                AppUtility.sendBroadCastMessages(getApplicationContext(), AppConstants.ON_PLAYER_STOP);
            }

            @Override
            public void onPlayerError() {
                AppUtility.sendBroadCastMessages(getApplicationContext(), AppConstants.ON_PLAYER_ERROR);
            }
        });
    }

    public void pausePlayer() {
        mPlayerManager.pauseExoPlayer();
    }

    public void resumePlayerWhenNetConnectionAvailable() {
        mPlayerManager.resumeExoPlayerWhenNetConnectionAvailable(getApplicationContext());
    }

    public void stopPlayer() {
        mPlayerManager.stopExoPlayer();
    }

    public boolean isPlayerPlaying() {
        if (mPlayerManager.isExoPlayerPlaying())
            return true;
        else
            return false;
    }
    /** End Player Controlling Methods */


    /*** Start Recorder Controlling Methods*/
    public void startRecording(String filePath) {
        mPlayerManager.startMediaRecorder(filePath, new MediaRecorderListener() {
            @Override
            public void onRecordingStart() {
                AppUtility.sendBroadCastMessages(getApplicationContext(), AppConstants.ON_START_RECORDING);
            }

            @Override
            public void onRecordingStop() {
                AppUtility.sendBroadCastMessages(getApplicationContext(), AppConstants.ON_STOP_RECORDING);
            }

            @Override
            public void onRecordingError() {
                AppUtility.sendBroadCastMessages(getApplicationContext(), AppConstants.ON_RECORDING_ERROR);
            }
        });
    }

    public void stopRecording() {
        mPlayerManager.stopMediaRecorder();
    }

    public boolean isRecorderOn() {
        return mPlayerManager.isMediaRecorderOn();
    }
    /** End Recorder Controlling Methods */

    /*** Start Listeners And BroadCastReceivers Method*/
    private void initializePhoneStateListener() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
    }

    public class MyPhoneStateListener extends PhoneStateListener {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            if (lastState == state) {
                //No change, de_bounce extras;
                return;
            }
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    if (!isPlayerPlaying()) {
                        startPlayer();
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if (isPlayerPlaying()) {
                        pausePlayer();
                    }
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    if (isPlayerPlaying()) {
                        pausePlayer();
                    }
                    break;
            }
            lastState = state;
        }
    }

    public static class RadioActionReceiver extends BroadcastReceiver {

        public RadioActionReceiver() {
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String actionName = intent.getAction();

            if (actionName.equals(AppConstants.ACTION_PLAY)) {
                playerService.startPlayer();
            } else if (actionName.equals(AppConstants.ACTION_PAUSE)) {
                playerService.pausePlayer();
            } else if (actionName.equals(AppConstants.ACTION_STOP)) {
                playerService.stopPlayer();
                playerService.stopSelf();
            }

        }
    }

    /** End Listeners And BroadCastReceivers Method*/

}
