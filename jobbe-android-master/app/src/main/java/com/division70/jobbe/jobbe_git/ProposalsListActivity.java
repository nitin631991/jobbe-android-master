package com.division70.jobbe.jobbe_git;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ProposalsListActivity extends Activity {
    private ImageView JobIconView;
    private TextView JobNameView;
    private TextView RequestDateView;
    private TextView title_text;
    private LinearLayout ProposalsListLayout;
    private Button DeleteButton;
    private RelativeLayout PlaceholderLayout;
    private SwipeRefreshLayout swipeView;

    private String reqId;
    private ParseObject request;
    private ArrayList<ParseObject> proposals;
    private ParseObject job;
    private RelativeLayout spinnerLayout;
    private boolean activityStart = true;

    private String iconName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.proposalslist_layout);

        sendScreenAnalytics();

        new initAsync().execute();
        /*
        init();

        setJobBar();

        setRefreshView();

        setUpActionBar();
        */
    }

    private void setUpActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        CustomActionbar customActionbar = new CustomActionbar(this);
        customActionbar.setTitle(this.request.getString("title"));
        actionBar.setCustomView(customActionbar);
        actionBar.setDisplayShowCustomEnabled(true);

        ImageView back = (ImageView) customActionbar.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backFunction();
            }
        });

        RelativeLayout title = (RelativeLayout) customActionbar.findViewById(R.id.title_layout);
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProposalsListActivity.this, RequestsArchiveDetailsActivity.class);
                intent.putExtra("reqId", request.getObjectId());
                startActivity(intent);
            }
        });
    }

    private void sendScreenAnalytics(){
        Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
        t.setScreenName("CLIENTE - RICHIESTA - LISTA PROPOSTE");
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    /*
    private void init() {
        JobIconView = (ImageView) findViewById(R.id.jobicon_iv);
        JobNameView = (TextView) findViewById(R.id.jobname_tv);
        RequestDateView = (TextView) findViewById(R.id.requestdate_tv);
        title_text = (TextView) findViewById(R.id.title_text);
        ProposalsListLayout = (LinearLayout) findViewById(R.id.proposalslist_ll);
        DeleteButton = (Button) findViewById(R.id.delete_button);
        PlaceholderLayout = (RelativeLayout) findViewById(R.id.placeholder_layout);
        PlaceholderLayout.setVisibility(View.GONE);
        title_text.setVisibility(View.GONE);

        reqId = getIntent().getStringExtra("reqId");
        try {
            request = ParseObject.createWithoutData("Request",reqId);
            request.fetch();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        getProposals(false);

        DeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("deleteRequest", "these dicks");
                Map<String, String> params = new HashMap<String, String>();
                params.put("reqId", reqId);
                ParseCloud.callFunctionInBackground("deleteRequest", params, new FunctionCallback<Object>() {
                    @Override
                    public void done(Object o, ParseException e) {
                        if (e == null) {
                            Intent intent = new Intent(ProposalsListActivity.this, FirstActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            Toast.makeText(getApplicationContext(), "Richiesta eliminata correttamente",
                                    Toast.LENGTH_LONG).show();
                        } else e.printStackTrace();
                    }
                });
            }
        });

        setTitle();
    }
    */

    private void setViews(){
        JobIconView = (ImageView) findViewById(R.id.jobicon_iv);
        JobNameView = (TextView) findViewById(R.id.jobname_tv);
        RequestDateView = (TextView) findViewById(R.id.requestdate_tv);
        title_text = (TextView) findViewById(R.id.title_text);
        ProposalsListLayout = (LinearLayout) findViewById(R.id.proposalslist_ll);
        DeleteButton = (Button) findViewById(R.id.delete_button);
        PlaceholderLayout = (RelativeLayout) findViewById(R.id.placeholder_layout);
        PlaceholderLayout.setVisibility(View.GONE);
        title_text.setVisibility(View.GONE);

        reqId = getIntent().getStringExtra("reqId");


        DeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("deleteRequest", "yes");

                new AlertDialog.Builder(ProposalsListActivity.this)
                        .setTitle("Attenzione")
                        .setMessage("Vuoi veramente eliminare questa richiesta?")
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton("Conferma", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {

                                Map<String, String> params = new HashMap<String, String>();
                                params.put("reqId", reqId);
                                ParseCloud.callFunctionInBackground("deleteRequest", params, new FunctionCallback<Object>() {
                                    @Override
                                    public void done(Object o, ParseException e) {
                                        if (e == null) {

                                            //Get tracker.
                                            Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
                                            //Build and send an Event.
                                            t.send(new HitBuilders.EventBuilder()
                                                    .setCategory(getString(R.string.analytics_cat_quality_client))
                                                    .setAction(getString(R.string.analytics_action_click))
                                                    .setLabel("richiesta_annullata")
                                                    .build());

                                            Intent intent = new Intent(ProposalsListActivity.this, HomeClientActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            Toast.makeText(getApplicationContext(), "Richiesta eliminata correttamente",
                                                    Toast.LENGTH_LONG).show();
                                        } else e.printStackTrace();
                                    }
                                });

                            }
                        }).create().show();
            }
        });


    }

    private void setRequestBackground(){
        ParseObject jobCat = (ParseObject) request.get("jobCatId");
        try {
            jobCat.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        RelativeLayout jobBar = (RelativeLayout) findViewById(R.id.jobbar_rl);
        jobBar.setBackgroundColor(Color.parseColor("#" + jobCat.get("color").toString()));
        jobBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProposalsListActivity.this, RequestsArchiveDetailsActivity.class);
                intent.putExtra("reqId", request.getObjectId());
                startActivity(intent);
            }
        });
    }

    private void setTitle(){
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(this.request.get("title").toString());
    }

    private void setRequestPicture(){
        ParseObject jobCategory = (ParseObject) this.request.get("jobCatId");
        try {
            jobCategory.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.iconName = jobCategory.get("icon").toString().replace("-", "_");
        Log.i("ICON", this.iconName);
        int resourceId = getResources().getIdentifier(this.iconName, "drawable", "com.division70.jobbe.jobbe_git");
        this.JobIconView.setImageResource(resourceId);
    }

    /*
    private void getProposals(final boolean refresh){
        ParseQuery query = ParseQuery.getQuery("Proposal").whereEqualTo("requestId",request);
        query.findInBackground(new FindCallback() {
            @Override
            public void done(List list, ParseException e) {
                Log.i("proposalsSize", list.size() + "");
                proposals = (ArrayList<ParseObject>) list;
                if (proposals.size() == 0) showPlaceholder();
                else {
                    hidePlaceholder();
                    setProposalsList(refresh);
                }
            }

            @Override
            public void done(Object o, Throwable throwable) {
                proposals = (ArrayList<ParseObject>) o;
                if (proposals.size() == 0) showPlaceholder();
                else {
                    hidePlaceholder();
                    setProposalsList(refresh);
                }
            }
        });
    }
    */

    private boolean getProposals(){
        ParseQuery query = ParseQuery.getQuery("Proposal").whereEqualTo("requestId",request);
        try {
            proposals = (ArrayList<ParseObject>) query.find();
        } catch (ParseException e) {
            ParseErrorHandler.handleParseError(e, getApplicationContext());
            e.printStackTrace();
        }

        /*
        if (proposals.size() == 0) showPlaceholder();
        else {
             hidePlaceholder();
             setProposalsList(refresh);
        }
        */
        if (proposals.size() == 0) return true;
        else return false;
    }

    private void setJobBar(){
        job = request.getParseObject("job");
        try {
            job.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        JobNameView.setText(job.getString("name"));

        Date reqCreationDate = request.getCreatedAt();
        String dateText = createDateString(reqCreationDate);
        RequestDateView.setText(dateText);

        setRequestPicture();
        setRequestBackground();
    }

    public String createDateString(Date date){
        String intMonth = (String) android.text.format.DateFormat.format("MM", date); //06
        String year = (String) android.text.format.DateFormat.format("yyyy", date); //2013
        String day = (String) android.text.format.DateFormat.format("dd", date); //20
        return "richiesta del " + day + " " + getMonthFromNumber(intMonth) + " " + year;
    }

    public String getMonthFromNumber(String number){
        switch (number){
            case "01": return "gennaio";
            case "02": return "febbraio";
            case "03": return "marzo";
            case "04": return "aprile";
            case "05": return "maggio";
            case "06": return "giugno";
            case "07": return "luglio";
            case "08": return "agosto";
            case "09": return "settembre";
            case "10": return "ottobre";
            case "11": return "novembre";
            case "12": return "dicembre";
        }
        return "";
    }

    public void setProposalsList(){

        for(final ParseObject proposal : proposals){
            Log.i("Proposal", proposal.getString("description"));
            ProposalsListObjectView item = new ProposalsListObjectView(getApplicationContext());
            item.setProposal(proposal);
            if(proposal.getBoolean("deletedBySupplier"))
                item.disable(0);
            else if  (proposal.getBoolean("deleted"))
                item.disable(1);
            else
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        proposal.fetch();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (proposal.getBoolean("deletedBySupplier")) {
                        Toast.makeText(getApplicationContext(), "Il fornitore ha eliminato questa proposta",
                                Toast.LENGTH_LONG).show();
                        new refreshAsync().execute();
                    } else {
                        Intent intent = new Intent(ProposalsListActivity.this, BidRecapActivity.class);
                        intent.putExtra("propID", proposal.getObjectId());
                        intent.putExtra("iconName", iconName);
                        startActivity(intent);
                        overridePendingTransition(R.anim.pushleft_in, R.anim.pushleft_out);
                    }
                }
                });

            ProposalsListLayout.addView(item);
            //item.setRatingBarPublic();
        }
    }

    private void showPlaceholder(){
        title_text.setVisibility(View.GONE);
        PlaceholderLayout.setVisibility(View.VISIBLE);
    }

    private void hidePlaceholder(){
        PlaceholderLayout.setVisibility(View.GONE);
        title_text.setVisibility(View.VISIBLE);
    }

    private void setRefreshView(){
        swipeView = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new refreshAsync().execute();
            }
        });
    }

    private class initAsync extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            spinnerLayout = (RelativeLayout) findViewById(R.id.loadingSpinnerLayout);
            spinnerLayout.setVisibility(View.VISIBLE);
            setViews();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                request = ParseObject.createWithoutData("Request",reqId);
                request.fetch();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return getProposals();
        }

        @Override
        protected void onPostExecute(Boolean isEmpty) {
            if (isEmpty) showPlaceholder();
            else {
                hidePlaceholder();
                setProposalsList();
            }

            setTitle();
            setJobBar();
            setRefreshView();
            setUpActionBar();
            spinnerLayout.setVisibility(View.GONE);
        }

    }



    private class refreshAsync extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            swipeView.setRefreshing(true);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            boolean isEmpty = getProposals();
            return isEmpty;
        }

        @Override
        protected void onPostExecute(Boolean isEmpty) {
            ProposalsListLayout.removeAllViews();
            if (isEmpty) showPlaceholder();
            else {
                hidePlaceholder();
                setProposalsList();
            }
            swipeView.setRefreshing(false);
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        JobbeApplication.activityResumed();
        Utils.deleteNotifications(getApplicationContext());
        if(!activityStart)
            new refreshAsync().execute();
        else
            activityStart = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        JobbeApplication.activityPaused();
    }

    @Override
    public void onBackPressed(){
        backFunction();
    }

    private void backFunction(){
        Boolean fromPush = getIntent().getBooleanExtra("fromPush", false);
        Log.i("fromPush", fromPush + "");
        if (!fromPush)
            finish();
        else {
            toClientHome();
        }
    }

    private void toClientHome(){
        Intent intent = new Intent(ProposalsListActivity.this, HomeClientActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

}