package com.example.chargestatus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.util.Log;
import android.widget.TextView;

import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import java.util.concurrent.TimeUnit;

public class BatteryStatusReceiver extends BroadcastReceiver
{

    public final static String TAG="BatteryStatus";
    public final static int minimumSafeLimit = 85;
    public final static int maximumSafeLimit = 90;
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(intent.getAction() == Intent.ACTION_POWER_CONNECTED)
        {
            final Intent batteryStatus = context.registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
            if(level == 85)
            {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                MediaPlayer mp = MediaPlayer.create(context,notification);
                mp.start();
            }
            //setting up the work constraints, the phone must be in the charging state for this work to be queued.
            Constraints constraints = new Constraints.Builder()
                    .setRequiresCharging(true)
                    .build();
            //building  the work request
            WorkRequest chargeCheckRequest = new OneTimeWorkRequest.Builder(ChargeChecker.class)
                    .setConstraints(constraints)
                    .setInitialDelay(10,TimeUnit.MINUTES)
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL,2, TimeUnit.MINUTES)
                    .addTag(TAG)
                    .build();
            WorkManager.getInstance(context).enqueue(chargeCheckRequest);


        }
        else if(intent.getAction() == Intent.ACTION_POWER_DISCONNECTED)
        {
            //terminate the WorkManager
            WorkManager.getInstance(context).cancelAllWorkByTag(TAG);
        }
    }
}
