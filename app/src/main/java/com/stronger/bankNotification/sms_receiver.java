package com.stronger.bankNotification;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


public class sms_receiver extends BroadcastReceiver {
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    public void onReceive(final Context context, Intent intent) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        if (intent.getAction().equals(SMS_RECEIVED)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                // get sms objects
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus.length == 0) {
                    return;
                }
                // large message might be broken into many
                SmsMessage[] messages = new SmsMessage[pdus.length];
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    sb.append(messages[i].getMessageBody());
                }
                String sender = messages[0].getOriginatingAddress();
                String message = sb.toString();
                new AsyncTask().execute(sharedPreferences.getString("deviceName", ""), "SMS", sender, message, "");
            }
        }
    }
}


