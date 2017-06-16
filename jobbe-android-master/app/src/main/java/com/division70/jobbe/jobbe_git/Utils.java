package com.division70.jobbe.jobbe_git;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * Created by osvy on 24/11/15.
 */
public class Utils {

    public static Boolean checkConnection(Context context, boolean silence) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if(!isConnected && !silence) {
            Toast.makeText(context, context.getString(R.string.offline_default_alert), Toast.LENGTH_SHORT).show();
        }

        return isConnected;
    }

    public static Boolean checkConnection(Context context) {
        return checkConnection(context, false);
    }

    public static void deleteNotifications(Context context) {
        NotificationManager notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.cancelAll();
    }
}
