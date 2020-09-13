package com.gpa.safecharge;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.navigation.NavigationView;

import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    public final static String TAG="BatteryStatus";
    ActionBarDrawerToggle drawerToggle = null;
    DrawerLayout dl = null;
    View headerView = null;
    Switch alertSwitch = null;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Liquid Swipe related code
        ViewPager viewPager = findViewById(R.id.viewpager);
        InstructionPageAdapter adapter = new InstructionPageAdapter(this.getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(Resources.values().length * 10);

        //Navigation drawer related code
        dl = findViewById(R.id.main_activity);
        drawerToggle = new ActionBarDrawerToggle(this,dl,R.string.open_drawer,R.string.close_drawer);
        dl.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        NavigationView navigationView = findViewById(R.id.nav_view);
        headerView = navigationView.getHeaderView(0);
        alertSwitch = (Switch) headerView.findViewById(R.id.alertSwitch);
        alertSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                SharedPreferences sharedPreferences1 = view.getContext().getSharedPreferences(String.valueOf(R.string.com_gpa_battery_status_app_level_preference),Context.MODE_PRIVATE);
                SharedPreferences.Editor editor1 = sharedPreferences1.edit();
                boolean isAlertEnabled1 = sharedPreferences1.getBoolean(ApplicationConstants.isAlertEnabled.toString(),false);
                Switch alertSwitch1 = (Switch) view.findViewById(R.id.alertSwitch);
                alertSwitch1.setChecked(!isAlertEnabled1);
                editor1.putBoolean(ApplicationConstants.isAlertEnabled.toString(),!isAlertEnabled1);
                editor1.apply();
            }
        });
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                return true;
            }
        });

        //Core code consisting of adding work to queue
        SafeChargerUtil.createNotificationChannel(this.getApplicationContext());
        SharedPreferences sharedPreferences = this.getApplicationContext().getSharedPreferences(String.valueOf(R.string.com_gpa_battery_status_app_level_preference),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        boolean isFirstJobCreated =  sharedPreferences.getBoolean(ApplicationConstants.isFirstJobCreated.toString(),false);
        if(!isFirstJobCreated)
        {
            editor.putBoolean(ApplicationConstants.isFirstJobCreated.toString(),true);
            editor.apply();
            SafeChargerUtil.createJob(this.getApplicationContext(),false);
        }
    }
    //function related to navigation view
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences sharedPreferences = this.getApplicationContext().getSharedPreferences(String.valueOf(R.string.com_gpa_battery_status_app_level_preference),Context.MODE_PRIVATE);
        final boolean isAlertEnabled = sharedPreferences.getBoolean(ApplicationConstants.isAlertEnabled.toString(),false);
        alertSwitch.setChecked(isAlertEnabled);
        if(drawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

}