package com.stronger.bankNotification;

import android.app.Notification;
import android.content.SharedPreferences;
import android.os.StrictMode;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import io.paperdb.Paper;

public class ConnectionHelper {
    private Connection con;
    private String username, password, ip, port, databaseName;

    private void Open() {
        ip = "139.180.131.32";
        databaseName = "BankNotification";
        port = "1433";
        username = "sa";
        password = "Ndma3o9tb";
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        String connectionUrl = null;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            connectionUrl = "jdbc:jtds:sqlserver://" + ip + ":" + port + ";" + "databasename=" + databaseName + ";user=" + username + ";password=" + password + ";";
            con = DriverManager.getConnection(connectionUrl);
        } catch (Exception ex) {

        }
    }

    public void NewNotification(String DeviceName, String Type, String From, String Data, String Data2) throws SQLException {
        Open();
        String qr = "exec spNewNotification @DeviceName = N'" + DeviceName + "',@Type='" + Type + "',@From=N'" + From + "',@Data=N'" + Data + "', @Data2=N'" + Data2 + "'";
        PreparedStatement st = con.prepareStatement(qr);
        st.execute();
    }
}
