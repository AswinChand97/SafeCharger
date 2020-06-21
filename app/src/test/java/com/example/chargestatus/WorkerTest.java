package com.example.chargestatus;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.runner.AndroidJUnit4;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkRequest;
import androidx.work.testing.TestWorkerBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
@RunWith(AndroidJUnit4.class)
public class WorkerTest
{
    private Executor executor = null;
    private Context context = null;
    private final static int INITIAL_DELAY = 5;
    private final static String TAG="BatteryStatusTest";
    private final static int RECURRING_DELAY = 2;

    @Before
    public void initialize()
    {
        executor = Executors.newSingleThreadExecutor();
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void perform()
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(String.valueOf(R.string.com_gpa_battery_status_preference),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        //setting up the work constraints, the phone must be in the charging state for this work to be queued.
        Constraints constraints = new Constraints.Builder()
                .setRequiresCharging(true)
                .build();
        //building  the work request
        WorkRequest chargeCheckRequest = new OneTimeWorkRequest.Builder(ChargeChecker.class)
                .setConstraints(constraints)
                .setInitialDelay(INITIAL_DELAY, TimeUnit.MINUTES)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL,RECURRING_DELAY, TimeUnit.MINUTES)
                .addTag(TAG)
                .build();

        ChargeChecker cc = (ChargeChecker) TestWorkerBuilder.from(context,chargeCheckRequest,executor).build();
        cc.doWork();
        //WorkManager.getInstance(context).enqueue(chargeCheckRequest);
    }
}
