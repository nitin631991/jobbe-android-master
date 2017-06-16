package com.division70.jobbe.jobbe_git;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;

/**
 * Created by osvy on 30/11/15.
 */
public class AlertFromPushActivity extends Activity {
    private String message;
    private String params;
    private String type;
    private Boolean fromPush;
    private Object classToGo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("ALERT ACTIVITY", "SONO IN ALERTFROMPUSHACTIVITY");
        //IntentFilter filter = new IntentFilter("test_alert");
        //this.registerReceiver(mReceivedSMSReceiver, filter);

        message = getIntent().getStringExtra("message");
        classToGo = getIntent().getExtras().get("classToGo");
        params = getIntent().getStringExtra("param");
        type = getIntent().getStringExtra("type");
        fromPush = getIntent().getBooleanExtra("fromPush", true);

        displayAlert();
    }

    private void displayAlert()
    {
        Log.i("ALERT ACTIVITY", "SONO IN DISPLAYALERT");
        Log.i("ReqId_1", params);

        Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        vib.vibrate(250);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ehi!");
        builder.setMessage(message).setCancelable(
                false).setPositiveButton("Apri",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i("ReqId_2", params);
                        Intent x = new Intent(AlertFromPushActivity.this, (Class) classToGo);

                        /*
                        x.putExtra("message", message);
                        x.putExtra("reqId", params);
                        x.putExtra("fromPush", fromPush);
                        */
                        x = getIntentFromType(type, params, message, x);
                        startActivity(x);
                        //dialog.cancel();
                        finish();
                    }
                }).setNegativeButton("Continua",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //dialog.cancel();
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private final BroadcastReceiver mReceivedSMSReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("ALERT ACTIVITY", "SONO IN ONRECEIVE");
            String action = intent.getAction();
            //your SMS processing code
            displayAlert();
        }
    };


    private Intent getIntentFromType(String type, String param, String message, Intent intent){
        switch (type){
            case "new_request": {
                Log.i("type_newReq",type);
                intent.putExtra("message", message);
                intent.putExtra("reqId", param);
                intent.putExtra("fromPush", true);
                return intent;
            }
            case "proposal_accepted": {
                Log.i("type_PropAcc", type);
                intent.putExtra("message", message);
                intent.putExtra("reqId", param);
                intent.putExtra("fromPush", true);
                return intent;
            }
            case "new_proposal": {
                Log.i("type_newProp", type);
                intent.putExtra("message", message);
                intent.putExtra("propID", param);
                intent.putExtra("fromPush", true);
                return intent;
            }
            case "new_review": {
                Log.i("type_newRew", type);
                intent.putExtra("message", message);
                intent.putExtra("fromPush", true);
                return intent;
            }
            case "complete_request": {
                Log.i("type_compReq", type);
                intent.putExtra("message", message);
                intent.putExtra("reqId", param);
                intent.putExtra("fromPush", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                return intent;
            }
            case "incomplete_request": {
                Log.i("type_incReq", type);
                intent.putExtra("message", message);
                intent.putExtra("reqId", param);
                intent.putExtra("fromPush", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                return intent;
            }
            case "reminder_supplier": {
                Log.i("type_remSupp", type);
                intent.putExtra("message", message);
                intent.putExtra("reqId", param);
                intent.putExtra("fromPush", true);
                return intent;
            }
            /*
            case "reminder_client": {
                Log.i("type_remClient", type);
                Intent intent = new Intent(PushManagerActivity.this, FirstActivity.class);
                return intent;
            }
            */
        }

        return null;
    }


    @Override
    protected void onResume() {
        super.onResume();
        JobbeApplication.activityResumed();
        Utils.deleteNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();
        JobbeApplication.activityPaused();
    }

}
