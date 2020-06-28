package com.gpa.safecharge;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel(this.getApplicationContext());
    }
    private void createNotificationChannel(Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            CharSequence name = context.getResources().getString(R.string.com_gpa_battery_status_channel_name);
            String description = context.getResources().getString(R.string.com_gpa_battery_status_channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(context.getResources().getString(R.string.com_gpa_battery_status_channel_id), name, importance);
            channel.setDescription(description);
            channel.setSound(BatteryStatusReceiver.getNotification(),BatteryStatusReceiver.getAudioAttributes());
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}