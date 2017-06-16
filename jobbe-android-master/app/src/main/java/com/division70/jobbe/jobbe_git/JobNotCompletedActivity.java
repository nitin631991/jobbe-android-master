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
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by giorgio on 12/11/15.
 */
public class JobNotCompletedActivity extends Activity {

    private Button confirmButton;
    private TextView requestTitle;
    private TextView motivation;
    private RelativeLayout jobDone;

    private ParseObject supplier;
    private ParseObject client;
    private ParseObject request;

    private String reqId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jobnotcompleted_layout);

        this.init();

        try {
            Badges.removeBadge(this);
            Badges.setBadge(this, 0);
        } catch (BadgesNotSupportedException badgesNotSupportedException) {
            Log.d("wydtws", badgesNotSupportedException.getMessage());
        }

    }

    private void init(){
        this.confirmButton = (Button) findViewById(R.id.confirm_button);
        this.requestTitle = (TextView) findViewById(R.id.request_title);
        this.motivation = (TextView) findViewById(R.id.motivation);
        this.jobDone = (RelativeLayout) findViewById(R.id.job_done);

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

        this.requestTitle.setText(this.request.get("title").toString());
        this.motivation.setText(this.request.get("uncompletionNote").toString() + ".");
        setListeners();
    }

    private void setListeners(){

        this.confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmJobNotCompleted();
                toClientHome();
            }
        });

        this.jobDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(JobNotCompletedActivity.this, SupplierReviewActivity.class);
                intent.putExtra("reqId", request.getObjectId()); //No flags to enhance performances
                startActivity(intent);
            }
        });

    }

    private void confirmJobNotCompleted(){
        this.request.put("uncompletionNoteClient", "Confermato");
        this.request.put("confirmUncompleteReq", true);
        try {
            this.request.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void toClientHome(){
        Intent intent = new Intent(JobNotCompletedActivity.this, HomeClientActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private int getUserRequestsNumber(ParseUser user) {
        List<ParseObject> list;
        ArrayList<Integer> illegalStates = new ArrayList<Integer>();
        illegalStates.add(4);
        illegalStates.add(5);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
        query.whereEqualTo("userId", user);
        query.whereNotContainedIn("state", illegalStates);
        query.whereNotEqualTo("confirmUncompleteReq", true);
        query.include("job");
        query.include("jobCatId");
        query.include("choosenProposal");
        query.include("choosenSupplier");
        query.orderByDescending("createdAt");
        try {
            list = query.find();
            return list.size();
        }
        catch (ParseException e){
            Log.i("REQUEST_LIST", "FAILED");
            Log.i("ParseException",e.toString());
            ParseErrorHandler.handleParseError(e, getApplicationContext());
        }
        return 0;
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
