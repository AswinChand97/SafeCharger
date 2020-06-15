package com.example.chargestatus;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.chargestatus.BatteryStatusReceiver.TAG;

public class ChargeChecker extends Worker
{

    public ChargeChecker(@NonNull Context context, @NonNull WorkerParameters workerParams)
    {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork()
    {
        SharedPreferences sharedPreferences = this.getApplicationContext().getSharedPreferences(String.valueOf(R.string.com_gpa_battery_status_preference),Context.MODE_PRIVATE);
        boolean isFinalCheck = sharedPreferences.getBoolean(ApplicationConstants.finalCheck.toString(),false);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        ListenableFuture<List<WorkInfo>> workInformation = WorkManager.getInstance(this.getApplicationContext()).getWorkInfosByTag(TAG);
        final Intent batteryStatus = this.getApplicationContext().registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
        if(level>=BatteryStatusReceiver.MINIMUM_SAFE_LIMIT)
        {
            alert();
            return Result.success();
        }
        if(!isFinalCheck) {
            try {
                List<WorkInfo> requiredWorkInformation = workInformation.get();
                WorkInfo workInfo = requiredWorkInformation.get(0);
                int runAttemptCount = workInfo.getRunAttemptCount();

                if (runAttemptCount == 1) {
                    editor.putLong(ApplicationConstants.initialTime.toString(), System.currentTimeMillis());
                    editor.putLong(ApplicationConstants.initialLevel.toString(), System.currentTimeMillis());
                    editor.apply();
                } else if (runAttemptCount == 2) {
                    long timeAfterTheFirstTry = System.currentTimeMillis();
                    editor.putLong(ApplicationConstants.timeAfterTheFirstTry.toString(), timeAfterTheFirstTry);
                    editor.putLong(ApplicationConstants.levelAfterTheFirstTry.toString(), level);
                    long differenceInTime = timeAfterTheFirstTry - sharedPreferences.getLong(ApplicationConstants.initialTime.toString(), 0);
                    int differenceInLevel = level - sharedPreferences.getInt(ApplicationConstants.initialLevel.toString(), 0);
                    int differenceInMinutes = (int) (differenceInTime / (1000 * 60));
                    int chargeIncreaseRate = differenceInLevel / differenceInMinutes;
                    editor.putInt(ApplicationConstants.chargeIncreaseRate.toString(), chargeIncreaseRate);
                    editor.apply();
                } else {
                    int chargeIncreaseRate = sharedPreferences.getInt(ApplicationConstants.chargeIncreaseRate.toString(), 0);
                    int remainingMinutesForSafeCharge = (BatteryStatusReceiver.MINIMUM_SAFE_LIMIT - level) / chargeIncreaseRate;
                    if (remainingMinutesForSafeCharge <= 14) {
                        //setting up the work constraints, the phone must be in the charging state for this work to be queued.
                        Constraints constraints = new Constraints.Builder()
                                .setRequiresCharging(true)
                                .build();
                        //building  the work request
                        WorkRequest chargeCheckRequest = new OneTimeWorkRequest.Builder(ChargeChecker.class)
                                .setConstraints(constraints)
                                .setBackoffCriteria(BackoffPolicy.LINEAR, BatteryStatusReceiver.RECURRING_DELAY, TimeUnit.MINUTES)
                                .addTag(TAG)
                                .build();
                        //queuing the work request
                        WorkManager.getInstance(this.getApplicationContext()).enqueue(chargeCheckRequest);
                        editor.putBoolean(ApplicationConstants.finalCheck.toString(),true);
                        editor.apply();
                        return Result.failure();
                    }
                }
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
            }
        }
        return Result.retry();
    }
    private void alert()
    {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        MediaPlayer mp = MediaPlayer.create(this.getApplicationContext(),notification);
        mp.start();
    }
}
