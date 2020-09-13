package com.gpa.safecharge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.work.WorkManager;

public class BootCompletedReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(intent !=null && intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
        {
            WorkManager.getInstance(context).cancelAllWorkByTag(SafeChargerUtil.TAG);
            SafeChargerUtil.createNotificationChannel(context);
            SharedPreferences sharedPreferences = context.getSharedPreferences(String.valueOf(R.string.com_gpa_battery_status_app_level_preference),Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            boolean isFirstJobCreated =  sharedPreferences.getBoolean(ApplicationConstants.isFirstJobCreated.toString(),false);
            if(!isFirstJobCreated)
            {
                editor.putBoolean(ApplicationConstants.isFirstJobCreated.toString(),true);
                editor.apply();
                SafeChargerUtil.createJob(context,false);
            }
        }
    }
}
