package com.stronger.bankNotification;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.PermissionChecker;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.paperdb.Paper;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.dnsoverhttps.DnsOverHttps;
import okhttp3.logging.HttpLoggingInterceptor;


class public_func {
    static final String BROADCAST_STOP_SERVICE = "com.rxz.bank_notifier.stop_all";
    static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    static final int battery_notify_id = 1;
    static final int chat_command_notify_id = 2;
    static final int notification_listener_service_notify_id = 3;
    static final int send_ussd_servce_notify_id = 4;
    static final int resend_service_notify_id = 5;

    static long parse_long(String content) {
        long result = 0;
        try {
            result = Long.parseLong(content);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return result;
    }

    static boolean check_network_status(@NotNull Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert manager != null;
        boolean network_status = false;
        Network[] networks = manager.getAllNetworks();
        if (networks.length != 0) {
            for (Network network : networks) {
                NetworkCapabilities network_capabilities = manager.getNetworkCapabilities(network);
                assert network_capabilities != null;
                if (network_capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)) {
                    network_status = true;
                }
            }
        }
        return network_status;
    }

    @NotNull
    static String get_send_phone_number(@NotNull String phone_number) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < phone_number.length(); ++i) {
            char c = phone_number.charAt(i);
            if (c == '+' || Character.isDigit(c)) {
                result.append(c);
            }
        }
        return result.toString();
    }

    static String get_dual_sim_card_display(Context context, int slot, boolean show_name) {
        String dual_sim = "";
        if (slot == -1) {
            return dual_sim;
        }
        if (public_func.get_active_card(context) >= 2) {
            String result = "";
            if (show_name) {
                result = "(" + get_sim_display_name(context, slot) + ")";
            }
            dual_sim = "SIM" + (slot + 1) + result + " ";
        }
        return dual_sim;
    }

    @NotNull
    @Contract(pure = true)
    static String get_url(String token, String func) {
        return "https://api.telegram.org/bot" + token + "/" + func;
    }

//    @NotNull
//    static OkHttpClient get_okhttp_obj(boolean doh_switch, proxy_config proxy_item) {
//        OkHttpClient.Builder okhttp = new OkHttpClient.Builder()
//                .connectTimeout(15, TimeUnit.SECONDS)
//                .readTimeout(15, TimeUnit.SECONDS)
//                .writeTimeout(15, TimeUnit.SECONDS)
//                .retryOnConnectionFailure(true);
//        Proxy proxy = null;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            if (proxy_item.enable) {
//                InetSocketAddress proxyAddr = new InetSocketAddress(proxy_item.proxy_host, proxy_item.proxy_port);
//                proxy = new Proxy(Proxy.Type.SOCKS, proxyAddr);
//                Authenticator.setDefault(new Authenticator() {
//                    @Override
//                    protected PasswordAuthentication getPasswordAuthentication() {
//                        if (getRequestingHost().equalsIgnoreCase(proxy_item.proxy_host)) {
//                            if (proxy_item.proxy_port == getRequestingPort()) {
//                                return new PasswordAuthentication(proxy_item.username, proxy_item.password.toCharArray());
//                            }
//                        }
//                        return null;
//                    }
//                });
//                okhttp.proxy(proxy);
//                doh_switch = true;
//            }
//        }
//        if (doh_switch) {
//            OkHttpClient.Builder doh_http_client = new OkHttpClient.Builder().retryOnConnectionFailure(true);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                if (proxy_item.enable && proxy_item.dns_over_socks5) {
//                    doh_http_client.proxy(proxy);
//                }
//            }
//            okhttp.dns(new DnsOverHttps.Builder().client(doh_http_client.build())
//                    .url(HttpUrl.get("https://cloudflare-dns.com/dns-query"))
//                    .bootstrapDnsHosts(get_by_ip("2606:4700:4700::1001"), get_by_ip("2606:4700:4700::1111"), get_by_ip("1.0.0.1"), get_by_ip("1.1.1.1"))
//                    .includeIPv6(true)
//                    .build());
//        }
//        if (BuildConfig.DEBUG) {
//            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
//            interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
//            okhttp.addInterceptor(interceptor);
//        }
//        return okhttp.build();
//    }

    private static InetAddress get_by_ip(String host) {
        try {
            return InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    static boolean is_phone_number(@NotNull String str) {
        for (int i = str.length(); --i >= 0; ) {
            char c = str.charAt(i);
            if (c == '+') {
                Log.d("is_phone_number", "is_phone_number: found +.");
                continue;
            }
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }


    @NotNull
    static Notification get_notification_obj(Context context, String notification_name) {
        Notification.Builder notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(notification_name, notification_name,
                    NotificationManager.IMPORTANCE_MIN);
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(channel);
            notification = new Notification.Builder(context, notification_name);
        } else {//Notification generation method after O
            notification = new Notification.Builder(context).setPriority(Notification.PRIORITY_MIN);
        }
        notification.setAutoCancel(false)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .setTicker(context.getString(R.string.AppName))
                .setContentTitle(context.getString(R.string.AppName))
                .setContentText(notification_name + context.getString(R.string.service_is_running));
        return notification.build();
    }

    static void stop_all_service(@NotNull Context context) {
        Intent intent = new Intent(BROADCAST_STOP_SERVICE);
        context.sendBroadcast(intent);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static void start_service(Context context) {
        if (is_notify_listener(context)) {
            Log.d("start_service", "start_service: ");
            ComponentName thisComponent = new ComponentName(context, notification_listener_service.class);
            PackageManager pm = context.getPackageManager();
            pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }
    }

    static int get_sub_id(Context context, int slot) {
        int active_card = public_func.get_active_card(context);
        if (active_card >= 2) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return -1;
            }
            SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            assert subscriptionManager != null;
            return subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(slot).getSubscriptionId();
        }
        return -1;
    }

    static int get_active_card(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            Log.d("get_active_card", "No permission.");
            return -1;
        }
        SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        assert subscriptionManager != null;
        return subscriptionManager.getActiveSubscriptionInfoCount();
    }


    static String get_sim_display_name(Context context, int slot) {
        final String TAG = "get_sim_display_name";
        String result = "Unknown";
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "No permission.");
            return result;
        }
        SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        assert subscriptionManager != null;
        SubscriptionInfo info = subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(slot);
        if (info == null) {
            Log.d(TAG, "The active card is in the second card slot.");
            if (get_active_card(context) == 1 && slot == 0) {
                info = subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(1);
            }
            if (info == null) {
                return result;
            }
            return result;
        }
        result = info.getDisplayName().toString();
        if (info.getDisplayName().toString().contains("CARD") || info.getDisplayName().toString().contains("SUB")) {
            result = info.getCarrierName().toString();
        }
        return result;
    }


    static void write_log(@NotNull Context context, String log) {
        Log.i("write_log", log);
        int new_file_mode = Context.MODE_APPEND;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(context.getString(R.string.time_format), Locale.UK);
        String write_string = "\n" + simpleDateFormat.format(new Date(System.currentTimeMillis())) + " " + log;
        write_log_file(context, write_string, new_file_mode);
    }

    static String read_log(@NotNull Context context, int line) {
        String result = context.getString(R.string.no_logs);
        String TAG = "read_file_last_line";
        StringBuilder builder = new StringBuilder();
        FileInputStream file_stream = null;
        FileChannel channel = null;
        try {
            file_stream = context.openFileInput("error.log");
            channel = file_stream.getChannel();
            ByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            buffer.position((int) channel.size());
            int count = 0;
            for (long i = channel.size() - 1; i >= 0; i--) {
                char c = (char) buffer.get((int) i);
                builder.insert(0, c);
                if (c == '\n') {
                    if (count == (line - 1)) {
                        break;
                    }
                    ++count;
                }
            }
            if (!builder.toString().isEmpty()) {
                return builder.toString();
            } else {
                return result;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Unable to read the file.");
            return result;
        } finally {
            try {
                if (file_stream != null) {
                    file_stream.close();
                }
                if (channel != null) {
                    channel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static void reset_log_file(Context context) {
        write_log_file(context, "", Context.MODE_PRIVATE);
    }

    private static void write_log_file(@NotNull Context context, @NotNull String write_string, int mode) {
        FileOutputStream file_stream = null;
        try {
            file_stream = context.openFileOutput("error.log", mode);
            byte[] bytes = write_string.getBytes();
            file_stream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (file_stream != null) {
                try {
                    file_stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    static boolean is_notify_listener(Context context) {
        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(context);
        return packageNames.contains(context.getPackageName());
    }
}
