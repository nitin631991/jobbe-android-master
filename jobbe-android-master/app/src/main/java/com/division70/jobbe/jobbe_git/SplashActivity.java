package com.division70.jobbe.jobbe_git;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by giorgio on 17/02/16.
 */
public class SplashActivity extends Activity {
    private ParseObject[] jobs;
    private ParseObject[] zones;
    private int reqNumber;

    private Intent intent;

    private final int SPLASH_DISPLAY_LENGTH = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        selectPage();
    }

    private void selectPage(){
        ParseUser user = ParseUser.getCurrentUser();
        // WARNING : SOLO PER DEBUG
        //user = null;
        // END WARNING

        long startTime = System.currentTimeMillis();

        if (user != null){
            if(Utils.checkConnection(getApplicationContext(),true)) {
                try {
                    user.fetch();
                } catch (ParseException e) {
                    ParseErrorHandler.handleParseError(e, getApplicationContext());
                    e.printStackTrace();
                    return;
                }
            }
            String type = user.get("type").toString();
            Log.i("FirstActivity_UserType", type.toString());
            if (type.equals("supplier")){
                this.intent = new Intent(SplashActivity.this, HomeSupplierActivity.class);
                //startActivity(intent);
            }
            else {
                this.intent = new Intent(SplashActivity.this, HomeClientActivity.class);
                //startActivity(intent);
            }
        }
        else{
            //Vai alla pagina accedi / registrati
            this.intent = new Intent(SplashActivity.this, MainActivity.class);
            //startActivity(intent);
        }

        long finishTime = System.currentTimeMillis();

        long waitTime = (finishTime - startTime) > SPLASH_DISPLAY_LENGTH ? 0 : SPLASH_DISPLAY_LENGTH - (finishTime - startTime);
        Log.i("Wait", "time: " + waitTime);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                startActivity(intent);
            }
        }, waitTime);

    }


    @Override
    protected void onResume() {
        super.onResume();
        JobbeApplication.activityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        JobbeApplication.activityPaused();
    }
}
