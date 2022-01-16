package com.stronger.bankNotification;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import io.paperdb.Paper;


public class main_activity extends AppCompatActivity {
    private Context context = null;

    private static boolean set_permissionSMS_back = false;
    private static boolean set_permissionListenNotification_back = false;
    private SharedPreferences sharedPreferences;
    private Connection connection;
    String ConnectionResul = "";

    @SuppressLint("BatteryLife")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();

        final EditText txtDeviceName = findViewById(R.id.txtDeviceName);
        final Button btnSave = findViewById(R.id.btnSave);
        final Switch swSMSPermission = findViewById(R.id.swSMSPermission);
        final Switch swPermissionEnableNotificationListener = findViewById(R.id.swPermissionEnableNotificationListener);

        Paper.init(context);

        //load config
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        String deviceName = sharedPreferences.getString("deviceName", "");
        //boolean isInitialed = sharedPreferences.getBoolean("isInitialed",false);
        if (!deviceName.isEmpty()) {
            txtDeviceName.setText(deviceName);
        }

        //check permission read sms
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED) {
            swSMSPermission.setChecked(true);
        }
        //check permission listen notification
        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(context);
        if (packageNames.contains(context.getPackageName())) {
            swPermissionEnableNotificationListener.setChecked(true);
        }

//        if(!isInitialed){
//            ComponentName thisComponent = new ComponentName(context, notification_listener_service.class);
//            context.getPackageManager().setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.putBoolean("isInitialed", true);
//            editor.apply();
//        }
        //event
        swSMSPermission.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                ActivityCompat.requestPermissions(main_activity.this, new String[]{Manifest.permission.RECEIVE_SMS}, 1);
                set_permissionSMS_back = true;
            }
        });
        swPermissionEnableNotificationListener.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                set_permissionListenNotification_back = true;
            }
        });
        btnSave.setOnClickListener(v -> {
            String dv = txtDeviceName.getText().toString();
            if (!dv.isEmpty()) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("deviceName", dv);
                editor.apply();

                Toast toast = Toast.makeText(context, "Save successfully", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP, 20, 30);
                toast.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (set_permissionSMS_back) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                final Switch swSMSPermission = findViewById(R.id.swSMSPermission);
                swSMSPermission.setChecked(false);
            }
            set_permissionSMS_back = false;
        }
        if (set_permissionListenNotification_back) {
            Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(context);
            if (!packageNames.contains(context.getPackageName())) {
                final Switch swPermissionEnableNotificationListener = findViewById(R.id.swPermissionEnableNotificationListener);
                swPermissionEnableNotificationListener.setChecked(false);
            }
            set_permissionListenNotification_back = false;
        }
    }
}
