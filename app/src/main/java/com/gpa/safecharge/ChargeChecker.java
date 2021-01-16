package com.gpa.safecharge;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.Objects;

import static com.gpa.safecharge.SafeChargerUtil.TAG;

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
        SharedPreferences sharedPreferencesAppLevel = this.getApplicationContext().getSharedPreferences(String.valueOf(R.string.com_gpa_battery_status_app_level_preference),Context.MODE_PRIVATE);
        String currentTagName = sharedPreferencesAppLevel.getString(ApplicationConstants.currentTag.toString(),TAG);
        Log.d(TAG,"current tag name  : " + currentTagName);
        boolean isFinalCheck = sharedPreferences.getBoolean(ApplicationConstants.finalCheck_v2.toString(),false);
        Log.d(TAG,"isFinalCheck : " + isFinalCheck);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        ListenableFuture<List<WorkInfo>> workInformation = WorkManager.getInstance(this.getApplicationContext()).getWorkInfosByTag(currentTagName);
        try
        {

            List<WorkInfo> requiredWorkInformation = workInformation.get();
            Log.d(SafeChargerUtil.TAG," required work info : " + requiredWorkInformation);
            final Intent batteryStatus = this.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            assert batteryStatus != null;
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            if (level >= SafeChargerUtil.MINIMUM_SAFE_LIMIT)
            {
                SafeChargerUtil.alert(this.getApplicationContext());
                editor.clear();
                editor.apply();
                SafeChargerUtil.createJob(this.getApplicationContext(),true);
                return Result.success();
            }
            boolean isInitialSettingDone = sharedPreferences.getBoolean(ApplicationConstants.initialSetting_v2.toString(),false);
            if (!isFinalCheck)
            {
                if (!isInitialSettingDone)
                {
                    long currentTime = System.currentTimeMillis();
                    Log.d(TAG,"Initial setting : current time millis : " + currentTime + " level : " + level);
                    editor.putLong(ApplicationConstants.initialTime_v2.toString(), currentTime);
                    editor.putInt(ApplicationConstants.initialLevel_v2.toString(), level);
                    editor.putBoolean(ApplicationConstants.initialSetting_v2.toString(),true);
                    editor.apply();
                }
                else
                {

                    boolean isDifferenceInLevelExist = sharedPreferences.getBoolean(ApplicationConstants.isDifferenceInLevelExist_v2.toString(), false);
                    if (!isDifferenceInLevelExist)
                    {
                        long time = System.currentTimeMillis();
                        float chargeIncreaseRate = 0f;

                        long differenceInTime = time - sharedPreferences.getLong(ApplicationConstants.initialTime_v2.toString(), 0);
                        int differenceInLevel = level - sharedPreferences.getInt(ApplicationConstants.initialLevel_v2.toString(), 0);
                        Log.d(TAG, "difference in level : " + differenceInLevel);
                        long differenceInMinutes = (differenceInTime / (1000 * 60));
                        Log.d(TAG, "difference in minutes : " + differenceInMinutes);
                        if (differenceInLevel > 0)
                        {
                            chargeIncreaseRate = ((float) differenceInLevel) / differenceInMinutes;
                            editor.putBoolean(ApplicationConstants.isDifferenceInLevelExist_v2.toString(), true);
                            editor.putFloat(ApplicationConstants.chargeIncreaseRate_v2.toString(), chargeIncreaseRate);
                            editor.apply();
                            Log.d(TAG," charge increase rate : " + chargeIncreaseRate);
                        }

                    }
                    else
                    {
                        float chargeIncreaseRate = sharedPreferences.getFloat(ApplicationConstants.chargeIncreaseRate_v2.toString(), -1f);
                        int remainingMinutesForSafeCharge = (int) Math.ceil((SafeChargerUtil.MINIMUM_SAFE_LIMIT - level) / chargeIncreaseRate);
                        Log.d(TAG, "remaining minutes of safe charge : " + remainingMinutesForSafeCharge );
                        if (remainingMinutesForSafeCharge <= SafeChargerUtil.REMAINING_MINUTES_FOR_SAFE_CHARGE)
                        {
                            SafeChargerUtil.createJob(this.getApplicationContext(),false);
                            editor.putBoolean(ApplicationConstants.finalCheck_v2.toString(), true);
                            editor.apply();
                            return Result.failure();
                        }
                    }
                }
            }
        }
        catch (Exception ex)
        {
            Log.e(TAG, Objects.requireNonNull(ex.getMessage()));
        }
        return Result.retry();
    }
}
