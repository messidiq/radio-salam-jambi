package com.salam.jambi.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
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
import com.salam.jambi.model.Program;

public class AlarmReceiver extends BroadcastReceiver {
    private String programName = "";
    private String hostName = "";
    @Override
    public void onReceive(Context mContext, Intent intent) {
        String alarmIntent = intent.getStringExtra(AppConstants.ALARM_INTENT_NAME);
        switch (alarmIntent) {
            case AppConstants.RADIO_PROGRAM_NOTIFICATION_ALARM:
                if(intent.hasExtra(AppConstants.PROGRAM_EXTRAS)){
                    Program program = (Program) intent.getExtras().getSerializable(AppConstants.PROGRAM_EXTRAS);
                    programName = program.getProgramName();
                    hostName = program.getProgramHostName();
                }
                sendNotification(mContext);
                break;
        }
    }

    private void sendNotification(Context mContext) {

        Intent intent = new Intent(mContext, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(AppConstants.PROGRAM_NOTIFICATION_REQUEST_CODE, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        Bitmap largeIcon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher);
        Notification customNotification = new NotificationCompat.Builder(mContext, AppConstants.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(largeIcon)
                .setContentTitle(AppConstants.RADIO_CHANNEL_NAME)
                .setContentText(programName)
                .setSubText(hostName)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setContentIntent(resultPendingIntent)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(AppConstants.CHANNEL_ID, AppConstants.RADIO_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(mChannel);
        }

        mNotificationManager.notify(AppConstants.PROGRAM_NOTIFICATION_NOTIFICATION_ID, customNotification);
    }
}
