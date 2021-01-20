package com.gpa.safecharge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.work.WorkManager;

public class BootCompletedReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(SafeChargerUtil.TAG," boot received intent : ");
        if(intent !=null && intent.getAction() != null && ( intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) || intent.getAction().equals(Intent.ACTION_LOCKED_BOOT_COMPLETED) ))
        {
            Log.d(SafeChargerUtil.TAG," intent not null : " + intent.getAction());
            WorkManager.getInstance(context).cancelAllWorkByTag(SafeChargerUtil.TAG);
            SafeChargerUtil.createNotificationChannel(context);
            SharedPreferences sharedPreferences = context.getSharedPreferences(String.valueOf(R.string.com_gpa_battery_status_app_level_preference),Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            boolean isAlertEnabled =  sharedPreferences.getBoolean(ApplicationConstants.isAlertEnabled_v2.toString(),false);
            Log.d(SafeChargerUtil.TAG," is alert enabled : " + isAlertEnabled);
            editor.clear();
            editor.apply();
            boolean isFirstJobCreated =  sharedPreferences.getBoolean(ApplicationConstants.isFirstJobCreated_v2.toString(),false);
            Log.d(SafeChargerUtil.TAG," is First job created : " + isFirstJobCreated);
            if(!isFirstJobCreated)
            {
                editor.putBoolean(ApplicationConstants.isFirstJobCreated_v2.toString(),true);
                editor.putBoolean(ApplicationConstants.isAlertEnabled_v2.toString(),isAlertEnabled);
                editor.apply();
                SafeChargerUtil.createJob(context,false);
                Log.d(SafeChargerUtil.TAG," job created : "  );
            }
        }
    }
}
