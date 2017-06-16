package com.division70.jobbe.jobbe_git;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by osvy on 19/11/15.
 */
public class PushManagerActivity extends Activity {
    private JSONObject json;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("PushManagerActivity", "Sono in PushManagerActivity! Bellaaaaa!!!");

        ParseUser user = ParseUser.getCurrentUser();
        if (user == null)
            goToLogin();
        else {
            Intent intent = getIntent();
            try {
                json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
                String type = json.getString("type");
                String message = json.getString("alert");
                String param = json.getString("param");
                Log.i("JSONdata", type + " " + message + " " + param + " ");

                Intent toGoIntent = getIntentFromType(type, param);
                toGoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(toGoIntent);
                finish();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private Intent getIntentFromType(String type, String param){
        switch (type){
            case "new_request": {
                Log.i("type_newReq",type);
                Intent intent = new Intent(PushManagerActivity.this, RequestRecapActivity.class);
                intent.putExtra("reqId", param);
                intent.putExtra("fromPush", true);
                return intent;
            }
            case "proposal_accepted": {
                Log.i("type_PropAcc",type);
                Intent intent = new Intent(PushManagerActivity.this, AcceptedBidRecapSupplierActivity.class);
                intent.putExtra("reqId", param);
                intent.putExtra("fromPush", true);
                return intent;
            }
            case "new_proposal": {
                Log.i("type_newProp",type);
                Intent intent = new Intent(PushManagerActivity.this, BidRecapActivity.class);
                intent.putExtra("propID", param);
                intent.putExtra("fromPush", true);
                return intent;
            }
            case "new_review": {
                Log.i("type_newRew",type);
                Intent intent = new Intent(PushManagerActivity.this, SupplierProfileActivity.class);
                intent.putExtra("fromPush", true);
                return intent;
            }
            case "complete_request": {
                Log.i("type_compReq",type);
                Intent intent = new Intent(PushManagerActivity.this, JobCompletedActivity.class);
                intent.putExtra("reqId", param);
                intent.putExtra("fromPush", true);
                return intent;
            }
            case "incomplete_request": {
                Log.i("type_incReq",type);
                Intent intent = new Intent(PushManagerActivity.this, JobNotCompletedActivity.class);
                intent.putExtra("reqId", param);
                intent.putExtra("fromPush", true);
                return intent;
            }
            case "reminder_supplier": {
                Log.i("type_remSupp", type);
                Intent intent = new Intent(PushManagerActivity.this, AcceptedBidRecapSupplierActivity.class);
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

    private void goToLogin(){
        Log.i("PushManager", "Not Logged in");
        Intent intent = new Intent(PushManagerActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
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
