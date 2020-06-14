package com.example.chargestatus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import java.util.concurrent.TimeUnit;

public class BatteryStatusReceiver extends BroadcastReceiver
{

    public final static String TAG="BatteryStatus";
    public final static int MINIMUM_SAFE_LIMIT = 85;
    private final static int INITIAL_DELAY = 10;
    private final static int RECURRING_DELAY = 2;
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(Intent.ACTION_POWER_CONNECTED.equals(intent.getAction()))
        {
            //setting up the work constraints, the phone must be in the charging state for this work to be queued.
            Constraints constraints = new Constraints.Builder()
                    .setRequiresCharging(true)
                    .build();
            //building  the work request
            WorkRequest chargeCheckRequest = new OneTimeWorkRequest.Builder(ChargeChecker.class)
                    .setConstraints(constraints)
                    .setInitialDelay(INITIAL_DELAY,TimeUnit.MINUTES)
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL,RECURRING_DELAY, TimeUnit.MINUTES)
                    .addTag(TAG)
                    .build();
            //queuing the work request
            WorkManager.getInstance(context).enqueue(chargeCheckRequest);


        }
        else if(Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction()))
        {
            //terminate the WorkManager
            WorkManager.getInstance(context).cancelAllWorkByTag(TAG);
        }
    }
}
