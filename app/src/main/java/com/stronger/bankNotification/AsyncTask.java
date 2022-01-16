package com.stronger.bankNotification;

import java.sql.SQLException;

public class AsyncTask extends android.os.AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... strings) {
        try {
            ConnectionHelper connectionHelper = new ConnectionHelper();
            connectionHelper.NewNotification(strings[0], strings[1], strings[2], strings[3],strings[4]);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }
}
