package com.gpa.safecharge;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import java.util.concurrent.TimeUnit;

public class SafeChargerUtil
{

    public final static String TAG="BatteryStatus";
    public final static int MINIMUM_SAFE_LIMIT = 85;
    public final static int INITIAL_DELAY = 2;
    public final static int RECURRING_DELAY = 2;
    private final static int NOTIFICATION_ID = 1;
    public final static int REMAINING_MINUTES_FOR_SAFE_CHARGE = 15;


    public static Uri getNotification()
    {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if(notification == null)
        {
            Log.d(TAG,"Alarm is null");
            notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if(notification == null)
            {
                Log.d(TAG,"Notification is null");
                notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }
        return notification;
    }
    public static AudioAttributes getAudioAttributes()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();
        }
        return null;
    }
    public static void createNotificationChannel(Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            Log.d(TAG,"Entered to create notification channel");
            CharSequence name = context.getResources().getString(R.string.com_gpa_battery_status_channel_name);
            String description = context.getResources().getString(R.string.com_gpa_battery_status_channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(context.getResources().getString(R.string.com_gpa_battery_status_channel_id), name, importance);
            channel.setDescription(description);
            channel.setSound(getNotification(),getAudioAttributes());
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            Log.d(TAG,"channel details => name : " + name + " channel : " + channel );
            /*NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            notificationManagerCompat.createNotificationChannel(channel);*/
        }
    }
    public static void createJob(Context context,boolean isInitialDelayRequired)
    {
        //setting up the work constraints, the phone must be in the charging state for this work to be queued.
        Constraints constraints = new Constraints.Builder()
                .setRequiresCharging(true)
                .build();
        //building  the work request
        WorkRequest.Builder workRequestBuilder = new OneTimeWorkRequest.Builder(ChargeChecker.class)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.LINEAR,RECURRING_DELAY, TimeUnit.MINUTES)
                .addTag(TAG);
        if(isInitialDelayRequired)
        {
            workRequestBuilder.setInitialDelay(SafeChargerUtil.INITIAL_DELAY,TimeUnit.MINUTES);
        }
        WorkRequest chargeCheckRequest = workRequestBuilder.build();

        //queuing the work request
        Log.d(TAG,"queued the job");
        WorkManager.getInstance(context).enqueue(chargeCheckRequest);
    }
    public static void alert(Context context)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(String.valueOf(R.string.com_gpa_battery_status_app_level_preference),Context.MODE_PRIVATE);
        boolean isAlertEnabled = sharedPreferences.getBoolean(ApplicationConstants.isAlertEnabled.toString(),false);
        if(isAlertEnabled) {
            Uri notification = getNotification();
            Ringtone ringtone = RingtoneManager.getRingtone(context, notification);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getResources().getString(R.string.com_gpa_battery_status_channel_id))
                    .setSmallIcon(R.drawable.battery_notification_icon)
                    .setContentTitle(context.getResources().getString(R.string.com_gpa_battery_status_notification_title))
                    .setContentText(context.getResources().getString(R.string.com_gpa_battery_status_notification_text))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(context.getResources().getString(R.string.com_gpa_battery_status_notification_text)))
                    .setAutoCancel(true);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                builder.setSound(notification);
            }
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        /*NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(NOTIFICATION_ID,builder.build());*/
            Log.d(TAG, "inside alert method");
            //ringtone.play();
        }
    }

}
