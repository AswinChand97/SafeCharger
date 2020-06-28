package com.gpa.safecharge;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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

import static com.gpa.safecharge.BatteryStatusReceiver.TAG;

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
        Log.d(TAG,"isFinalCheck : " + isFinalCheck);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        ListenableFuture<List<WorkInfo>> workInformation = WorkManager.getInstance(this.getApplicationContext()).getWorkInfosByTag(TAG);
        try
        {
            List<WorkInfo> requiredWorkInformation = workInformation.get();
            WorkInfo workInfo = requiredWorkInformation.get(0);
            Log.d(TAG,"work info : " + workInfo);
            int runAttemptCount = workInfo.getRunAttemptCount();
            Log.d(TAG," runAttemptCount : " + runAttemptCount);
            final Intent batteryStatus = this.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            if (level >= BatteryStatusReceiver.MINIMUM_SAFE_LIMIT)
            {
                BatteryStatusReceiver.alert(this.getApplicationContext());
                return Result.success();
            }
            if (!isFinalCheck && runAttemptCount!=0)
            {
                boolean isDifferenceInLevelExist = sharedPreferences.getBoolean(ApplicationConstants.isDifferenceInLevelExist.toString(),false);
                if (!isDifferenceInLevelExist)
                {
                    long time = System.currentTimeMillis();
                    long differenceInTime = time- sharedPreferences.getLong(ApplicationConstants.initialTime.toString(), 0);
                    int differenceInLevel = level - sharedPreferences.getInt(ApplicationConstants.initialLevel.toString(), 0);
                    Log.d(TAG,"difference in level : " + differenceInLevel);
                    long differenceInMinutes =  (differenceInTime / (1000 * 60));
                    Log.d(TAG,"difference in minutes : " + differenceInMinutes);
                    if(differenceInLevel>0)
                    {
                        float chargeIncreaseRate = ((float)differenceInLevel) / differenceInMinutes;
                        Log.d(TAG,"charge increase rate : " + chargeIncreaseRate + " charge increase rate int : " + chargeIncreaseRate);
                        editor.putBoolean(ApplicationConstants.isDifferenceInLevelExist.toString(),true);
                        editor.putFloat(ApplicationConstants.chargeIncreaseRate.toString(), chargeIncreaseRate);
                        editor.apply();
                    }

                }
                else
                {
                    float chargeIncreaseRate = sharedPreferences.getFloat(ApplicationConstants.chargeIncreaseRate.toString(), -1f);
                    int remainingMinutesForSafeCharge = (int) Math.ceil((BatteryStatusReceiver.MINIMUM_SAFE_LIMIT - level) / chargeIncreaseRate);
                    Log.d(TAG,"remaining minutes of safe charge : " + remainingMinutesForSafeCharge + " at retry count : " + runAttemptCount );
                    if (remainingMinutesForSafeCharge <= BatteryStatusReceiver.REMAINING_MINUTES_FOR_SAFE_CHARGE)
                    {
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
                        editor.putBoolean(ApplicationConstants.finalCheck.toString(), true);
                        editor.apply();
                        return Result.failure();
                    }
                }
            }
        }
        catch (Exception ex)
        {
            Log.e(TAG, ex.getMessage());
        }
        return Result.retry();
    }
}
