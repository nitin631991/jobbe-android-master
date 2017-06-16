package com.division70.jobbe.jobbe_git;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.github.arturogutierrez.Badges;
import com.github.arturogutierrez.BadgesNotSupportedException;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.PushService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by osvy on 18/11/15.
 */
public class CustomPushReceiver extends ParsePushBroadcastReceiver {

    public CustomPushReceiver() {
        super();
        Log.i("CustomPushReceiver", "Sono in CustomPushReceiver.");
    }

    @Override
    protected Class<? extends Activity> getActivity(Context context,
                                                    Intent intent) {
        return PushManagerActivity.class;
    }

    @Override
    protected Notification getNotification(Context context, Intent intent) {
        Notification notification = super.getNotification(context, intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notification.color = context.getResources().getColor(R.color.jobbe_violet);
        }
        return notification;
    }


    @Override
    protected void onPushReceive(Context context, Intent intent) {
        //super.onPushReceive(context, intent);
        Log.i("Push Received", "Push Received");
        setBadge(context);
        String type ="";
        String message = "";
        String param = "";

        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            type = json.getString("type");
            message = json.getString("alert");
            param = json.getString("param");
            Log.i("Push Type", type);
            Log.i("Param", param);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        // FAI NOTIFICA INTERNA
        Log.i("sender", "Broadcasting message");
        Intent localIntent = new Intent("jobbe_local_not");
        //Intent localIntent = new Intent("test_alert");
        // You can also include some extra data.
        localIntent.putExtra("type", type);
        localIntent.putExtra("message", "This is my message!");
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);

        //LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);

        Log.i("CustomPushReceiver", "startForegroundCheck");

        //if (isAppForeground(context)) {
        //if(true){
        if (JobbeApplication.isActivityVisible()) {
            // App is in Foreground
            Log.i("CustomPushReceiver", "App in Foreground");
            //Intent i = new Intent(context, AlertFromPushActivity.class);
            Intent i = getIntentFromType(context, type, param, message);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);

        } else {
            // App is in Background
            Log.i("CustomPushReceiver", "App in Background");
            super.onPushReceive(context,intent);
        }


        //Intent i = new Intent(context, AlertFromPushActivity.class);
        //i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //i.putExtra("message", message);
        //context.startActivity(i);
        //LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
    }


    public boolean isAppForeground(Context mContext) {
        Log.i("CustomPushReceiver", "into isAppForeground");
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        Log.i("CustomPushReceiver", "isAppForeground check 1");
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        Log.i("CustomPushReceiver", "isAppForeground check 2");
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(mContext.getPackageName())) {
                Log.i("CustomPushReceiver", "isAppForeground returns false");
                return false;
            }
        }
        Log.i("CustomPushReceiver", "isAppForeground returns true");
        return true;
    }


    /*
    public static boolean isAppForeground(Context context) {

        // Get the Activity Manager
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        // Get a list of running tasks, we are only interested in the last one,
        // the top most so we give a 1 as parameter so we only get the topmost.
        List<ActivityManager.RunningAppProcessInfo> task = manager.getRunningAppProcesses();
        Log.i("tasks", task.size() + "");
        // Get the info we need for comparison.
        ComponentName componentInfo = task.get(0).importanceReasonComponent;
        Log.i("componentInfo_ReasonCod", task.get(0).importanceReasonCode + "");
        // Check if it matches our package name.
        if(componentInfo.getPackageName().equals(context.getPackageName()))
            return true;
        // If not then our app is not on the foreground.
        return false;
    }
    */

    /*
    public static boolean isAppForeground(Context context) {
        // Returns a list of application processes that are running on the
        // device

        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = context.getPackageName();

        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null)
            return false;

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }

        return false;
    }
    */




    /*
    @Override
    protected void onPushOpen(Context context, Intent intent) {
        super.onPushOpen(context, intent);

        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            parsePushJson(context, json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }




    private void parsePushJson(Context context, JSONObject json) {
        try {
            String type = json.getString("type");
            String message = json.getString("alert");
            String param = json.getString("param");
            Log.i("JSONdata", type + " " + message + " " + param + " ");

            Intent toGoIntent = getIntentFromType(context, type, param);
            toGoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(toGoIntent);
        } catch (JSONException e) {
            Log.e("PUSHEXCEPTION", "Push message json exception: " + e.getMessage());
        }
    }
*/

    private Intent getIntentFromType(Context context, String type, String param, String message){
        Intent i = new Intent(context, AlertFromPushActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        switch (type){

            case "new_request": {
                i.putExtra("message", message);
                i.putExtra("param", param);
                i.putExtra("fromPush", true);
                i.putExtra("classToGo", RequestRecapActivity.class);
                i.putExtra("type", type);
                return i;
            }
            case "proposal_accepted": {
                i.putExtra("message", message);
                i.putExtra("param", param);
                i.putExtra("fromPush", true);
                i.putExtra("classToGo", AcceptedBidRecapSupplierActivity.class);
                i.putExtra("type", type);
                return i;
            }
            case "new_proposal": {
                i.putExtra("message", message);
                i.putExtra("param", param);
                i.putExtra("fromPush", true);
                i.putExtra("classToGo", BidRecapActivity.class);
                //i.putExtra("classToGo", ProposalsListActivity.class);
                i.putExtra("type", type);
                return i;
            }
            case "new_review": {
                i.putExtra("message", message);
                i.putExtra("param", param);
                i.putExtra("fromPush", true);
                i.putExtra("classToGo", SupplierProfileActivity.class);
                i.putExtra("type", type);
                return i;
            }
            case "complete_request": {
                i.putExtra("message", message);
                i.putExtra("param", param);
                i.putExtra("fromPush", true);
                i.putExtra("classToGo", JobCompletedActivity.class);
                i.putExtra("type", type);
                return i;
            }
            case "incomplete_request": {
                i.putExtra("message", message);
                i.putExtra("param", param);
                i.putExtra("fromPush", true);
                i.putExtra("classToGo", JobNotCompletedActivity.class);
                i.putExtra("type", type);
                return i;
            }
            case "reminder_supplier": {
                i.putExtra("message", message);
                i.putExtra("param", param);
                i.putExtra("fromPush", true);
                i.putExtra("classToGo", AcceptedBidRecapSupplierActivity.class);
                i.putExtra("type", type);
                return i;
            }


        }

        return null;
    }

    public static void setBadge(Context context) {
        try {
            Badges.setBadge(context, 1);
        } catch (BadgesNotSupportedException badgesNotSupportedException) {
            Log.d("wydtws", badgesNotSupportedException.getMessage());
        }

    }

    public static String getLauncherClassName(Context context) {

        PackageManager pm = context.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
            if (pkgName.equalsIgnoreCase(context.getPackageName())) {
                String className = resolveInfo.activityInfo.name;
                return className;
            }
        }
        return null;
    }
}
