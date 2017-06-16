package com.division70.jobbe.jobbe_git;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.arturogutierrez.Badges;
import com.github.arturogutierrez.BadgesNotSupportedException;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.parse.ParseException;
import com.parse.ParseObject;

/**
 * Created by giorgio on 12/11/15.
 */
public class JobCompletedActivity extends Activity {

    private Button reviewButton;
    private TextView requestTitle;
    private RelativeLayout jobUndone;

    private ParseObject supplier;
    private ParseObject client;
    private ParseObject request;

    private String reqId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jobcompleted_layout);

        sendScreenAnalytics();

        this.init();

        try {
            Badges.removeBadge(this);
            Badges.setBadge(this, 0);
        } catch (BadgesNotSupportedException badgesNotSupportedException) {
            Log.d("wydtws", badgesNotSupportedException.getMessage());
        }

    }

    private void sendScreenAnalytics(){
        Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
        t.setScreenName("CLIENTE - ALERT - LAVORO SVOLTO");
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void init(){
        this.reviewButton = (Button) findViewById(R.id.confirm_button);
        this.requestTitle = (TextView) findViewById(R.id.request_title);
        this.jobUndone = (RelativeLayout) findViewById(R.id.job_undone);

        this.reqId = getIntent().getStringExtra("reqId");

        this.request = ParseObject.createWithoutData("Request", this.reqId);
        try {
            this.request.fetch();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        this.supplier = (ParseObject) this.request.get("choosenSupplier");
        try {
            this.supplier.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        this.client = (ParseObject) this.request.get("userId");
        try {
            this.client.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        this.requestTitle.setText(this.request.get("title").toString() + ":");
        setListeners();
    }

    private void setListeners(){

        this.reviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(JobCompletedActivity.this, SupplierReviewActivity.class);
                intent.putExtra("reqId", request.getObjectId());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        this.jobUndone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(JobCompletedActivity.this, JobUndoneActivity.class);
                intent.putExtra("reqId", request.getObjectId());
                intent.putExtra("clientId", client.getObjectId());
                intent.putExtra("userType", "client"); //Says it is the CLIENT who is setting the job as UNDONE
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Attenzione")
                .setMessage("Vuoi chiudere l\'applicazione?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        finish();
                    }
                }).create().show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("Jobbe_SP", Context.MODE_PRIVATE);
        int count = sharedPreferences.getInt("count", 0);
        if (count > 0)
            sharedPreferences.edit().putInt("count", count - 1).commit();
        JobbeApplication.activityResumed();
        Utils.deleteNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();
        JobbeApplication.activityPaused();
    }

}
