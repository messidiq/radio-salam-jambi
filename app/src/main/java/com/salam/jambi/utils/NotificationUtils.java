package com.salam.jambi.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.salam.jambi.R;
import com.salam.jambi.activity.MainActivity;
import com.salam.jambi.data.constant.AppConstants;
import com.salam.jambi.service.PlayerService;

public class NotificationUtils {

    public static Notification generateNotification(Context mContext) {
        Intent intent = new Intent(mContext, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(AppConstants.CONTENT_INTENT_REQUEST_CODE, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent playAction = getPendingIntentAction(mContext, AppConstants.PLAY_INTENT_REQUEST_CODE, AppConstants.ACTION_PLAY);
        PendingIntent pauseAction = getPendingIntentAction(mContext, AppConstants.PAUSE_INTENT_REQUEST_CODE, AppConstants.ACTION_PAUSE);
        PendingIntent stopAction = getPendingIntentAction(mContext, AppConstants.STOP_INTENT_REQUEST_CODE, AppConstants.ACTION_STOP);

        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, AppConstants.CHANNEL_ID);
        mBuilder.addAction(R.drawable.img_cross_icon_notify, mContext.getString(R.string.stop), stopAction);
        mBuilder.addAction(R.drawable.img_play_icon_notify, mContext.getString(R.string.play), playAction);
        mBuilder.addAction(R.drawable.img_pause_icon_notify, mContext.getString(R.string.pause), pauseAction);
        Bitmap largeIcon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher);
        mBuilder.setContentTitle(AppConstants.RADIO_CHANNEL_NAME);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setLargeIcon(largeIcon);
        mBuilder.setOngoing(true);
        mBuilder.setChannelId(AppConstants.CHANNEL_ID);
        mBuilder.setContentIntent(resultPendingIntent);
        Notification notification = mBuilder.build();
        notification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(AppConstants.CHANNEL_ID, AppConstants.RADIO_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            if(mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(mChannel);
            }
        }

        return notification;
    }

    private static PendingIntent getPendingIntentAction(Context mContext, int requestCode, String action) {
        Intent actionIntent = new Intent(mContext, PlayerService.RadioActionReceiver.class);
        actionIntent.setAction(action);
        return PendingIntent.getBroadcast(mContext, requestCode, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
