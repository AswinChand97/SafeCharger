package com.example.chargestatus;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class ChargeChecker extends Worker
{

    /*private long initialTime = -1l;
    private int initialLevel = -1;
    private long timeAfterTheFirstTry = -1l;
    private int levelAfterTheFirstTry = -1;
    private int chargeIncreaseRate = -1;
    private int remainingMinutesForSafeCharge = -1;*/
    public ChargeChecker(@NonNull Context context, @NonNull WorkerParameters workerParams)
    {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork()
    {
        ListenableFuture<List<WorkInfo>> workInformation = WorkManager.getInstance(this.getApplicationContext()).getWorkInfosByTag(BatteryStatusReceiver.TAG);
        final Intent batteryStatus = this.getApplicationContext().registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
        if(level>=BatteryStatusReceiver.minimumSafeLimit)
        {
            alert();
            return Result.success();
        }
        try
        {
            List<WorkInfo> requiredWorkInformation = workInformation.get();
            WorkInfo workInfo = requiredWorkInformation.get(0);
            int runAttemptCount = workInfo.getRunAttemptCount();
            if(runAttemptCount >= 4)
            {
                alert();
                return Result.success();
            }

            /*if(runAttemptCount == 1)
            {
                initialTime = System.currentTimeMillis();
                initialLevel = level;
            }
            else if(runAttemptCount == 2)
            {
                timeAfterTheFirstTry = System.currentTimeMillis();
                levelAfterTheFirstTry = level;
                long differenceInTime = timeAfterTheFirstTry - initialTime;
                int differenceInLevel = levelAfterTheFirstTry - initialLevel;
                int differenceInMinutes = (int) (differenceInTime/(1000*60));
                chargeIncreaseRate = differenceInLevel/differenceInMinutes;
                remainingMinutesForSafeCharge = (BatteryStatusReceiver.minimumSafeLimit - level)/chargeIncreaseRate;
            }*/
        }
        catch (Exception ex)
        {
            Log.e(BatteryStatusReceiver.TAG,ex.getMessage());
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
