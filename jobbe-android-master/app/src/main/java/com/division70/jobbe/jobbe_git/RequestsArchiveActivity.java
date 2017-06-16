package com.division70.jobbe.jobbe_git;

import android.app.ActionBar;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class RequestsArchiveActivity extends Activity {
    private ArrayList<ParseObject> requests;
    private ParseObject user;
    private RelativeLayout spinnerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.requests_archive_activity_layout);

        sendScreenAnalytics();

        //getUserRequests();
        setUpActionBar();
        new initAsync().execute();
    }

    private void setUpActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        CustomActionbar customActionbar = new CustomActionbar(this);
        customActionbar.setTitle("Archivio richieste");
        customActionbar.disableSubtitle();
        actionBar.setCustomView(customActionbar);
        actionBar.setDisplayShowCustomEnabled(true);

        ImageView back = (ImageView) customActionbar.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void sendScreenAnalytics(){
        Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
        t.setScreenName("FORNITORE - PROFILO - STORICO RICHIESTE");
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void getUserRequests(){
        try {
            user = ParseUser.getCurrentUser().fetch();
        } catch (ParseException e) {
            ParseErrorHandler.handleParseError(e, getApplicationContext());
            e.printStackTrace();
        }

        ParseQuery query = ParseQuery.getQuery("Request");
        query.whereEqualTo("choosenSupplier", user);
        try {
            requests = (ArrayList<ParseObject>) query.find();
        } catch (ParseException e) {
            ParseErrorHandler.handleParseError(e, getApplicationContext());
            e.printStackTrace();
        }

    }

    private void fillLayout(){
        LinearLayout requestList = (LinearLayout) findViewById(R.id.requests_ll);
        for (final ParseObject request : requests){
            RequestListObjectView item = new RequestListObjectView(getApplicationContext());
            item.setArchiveRequest(request);
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(RequestsArchiveActivity.this, RequestsArchiveDetailsActivity.class);
                    intent.putExtra("reqId", request.getObjectId());
                    startActivity(intent);

                }
            });

            requestList.addView(item);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private class initAsync extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute(){
            spinnerLayout = (RelativeLayout) findViewById(R.id.loadingSpinnerLayout);
            spinnerLayout.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... params){
            getUserRequests();
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if (requests.size() > 0)
                fillLayout();
            else
                showPlaceholder();
            spinnerLayout.setVisibility(View.GONE);
        }

    }

    private void showPlaceholder(){
        TextView placeholder = (TextView) findViewById(R.id.placeholderText);
        placeholder.setVisibility(View.VISIBLE);
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
