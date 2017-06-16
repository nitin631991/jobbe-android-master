package com.division70.jobbe.jobbe_git;

import android.app.Activity;
import android.app.Application;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

/**
 * Created by osvy on 30/10/15.
 */
public class JobbeApplication extends MultiDexApplication {
    private Tracker mTracker;

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);
        //Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_client_id));
/*        Parse.initialize(new Parse.Configuration.Builder(this)
                        .applicationId("jobbe")
                        .clientKey("jobbeClientKey")
                        .server("https://jobbe-staging.herokuapp.com/parse/") // The trailing slash is important.

                        .build()*/
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("jobbe")
                .clientKey("jobbeClientKeyForB3tt3rW0rld")
                .server("https://jobbe-eu-1.herokuapp.com/parse/") // The trailing slash is important.

                .build()
        );

        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        ParseUser user = ParseUser.getCurrentUser();
        if(user!=null){
            installation.put("userId",user);
            Log.i("user","notnull");
        }
        else
            Log.i("user", "isNull");
        installation.saveInBackground();

    }

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            /*mTracker = analytics.newTracker(R.xml.global_tracker);*/
            mTracker = analytics.newTracker(121212584);
        }
        return mTracker;
    }


    //ROBA X FORE/BACKGROUND

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    private static boolean activityVisible;

}
