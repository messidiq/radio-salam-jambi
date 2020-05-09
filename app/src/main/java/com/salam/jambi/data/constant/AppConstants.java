package com.salam.jambi.data.constant;

public class AppConstants {

    public static final int INDEX_ZERO = 0;
    public static final int FLAG_ZERO = 0;

    public static String RADIO_CHANNEL_NAME = "";
    public static String RADIO_URL = "";

    public static final String PLAYER_SERVICE_NAME = ".service.PlayerService";
    public static final String PROGRAM_EXTRAS = "program";

    // INTENT ACTION
    public static final String ACTION_PLAY = "com.mcc.radio.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.mcc.radio.ACTION_PAUSE";
    public static final String ACTION_STOP = "com.mcc.radio.ACTION_STOP";
    public static final String MY_BROADCAST_RECEIVER = "my_broad_cast_receiver";

    // MEDIA FORMAT
    public static final String MEDIA_FORMAT_M3U8 = "m3u8";

    // ALARM ACTION
    public static final String ALARM_INTENT_NAME = "alarm_intent";
    public static final String RADIO_PROGRAM_NOTIFICATION_ALARM = "radio_program_notification_alarm";

    // NOTIFICATION CONSTANTS
    public static final int FOREGROUND_NOTIFICATION_REQUEST_CODE = 200;
    public static final int STOP_INTENT_REQUEST_CODE = 201;
    public static final int CONTENT_INTENT_REQUEST_CODE = 202;
    public static final int PAUSE_INTENT_REQUEST_CODE = 203;
    public static final int PLAY_INTENT_REQUEST_CODE = 204;
    public static final int PROGRAM_NOTIFICATION_REQUEST_CODE = 205;
    public static final int PROGRAM_NOTIFICATION_NOTIFICATION_ID = 206;
    public static final String CHANNEL_ID = "MY-RADIO";

    // BROADCAST_MESSAGING_FLAGS
    public static final String ON_START_PLAYING = "onStartPlaying";
    public static final String ON_PLAYER_PAUSE = "onPlayerPause";
    public static final String ON_PLAYER_STOP = "onPlayerStop";
    public static final String ON_PLAYER_ERROR = "onPlayerError";
    public static final String ON_START_RECORDING = "onStartRecording";
    public static final String ON_STOP_RECORDING = "onStopRecording";
    public static final String ON_RECORDING_ERROR = "onRecordingError";

    // NETWORK CONNECTIVITY CHECKING FLAGS
    public static final int TYPE_WIFI = 1;
    public static final int TYPE_MOBILE = 2;
    public static final int TYPE_NOT_CONNECTED = 0;

}
