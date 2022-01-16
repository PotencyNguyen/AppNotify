package com.stronger.bankNotification;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.paperdb.Paper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class notification_listener_service extends NotificationListenerService {
    Context context;
    SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        Paper.init(context);
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        Notification notification = public_func.get_notification_obj(getApplicationContext(), getString(R.string.Notification_Listener_title));
        startForeground(public_func.notification_listener_service_notify_id, notification);
    }

    @Override
    public void onNotificationPosted(@NotNull StatusBarNotification sbn) {
        final String package_name = sbn.getPackageName();
        Bundle extras = sbn.getNotification().extras;
        if (extras != null) {
            String data = extras.getString(NotificationCompat.EXTRA_TEXT, "");
            String data2 = extras.getString(NotificationCompat.EXTRA_TITLE, "") + " " + extras.getString(NotificationCompat.EXTRA_BIG_TEXT, "").replace(" Nhấn để xem chi tiết.", "");
            new AsyncTask().execute(sharedPreferences.getString("deviceName", ""), "APP", package_name, data, data2);
        }
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }
}
