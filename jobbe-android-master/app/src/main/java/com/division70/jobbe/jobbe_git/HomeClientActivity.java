package com.division70.jobbe.jobbe_git;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.arturogutierrez.Badges;
import com.github.arturogutierrez.BadgesNotSupportedException;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeClientActivity extends Activity {
    private static final String TAG =HomeClientActivity.class.getSimpleName() ;
    private ArrayList<ParseObject> requests;
    private ParseUser user;
    private LinearLayout listLayout;
    private Button newRequestButton;
    private SwipeRefreshLayout swipeView;
    private Map<String, String> popupResult;
    private RelativeLayout emptyLayout;
    private RelativeLayout requestsLayout;
    private RelativeLayout spinnerLayout;
    private ArrayList<Thread> threadArray;
    private JSONObject offlineData;
    private boolean activityOpen;
    private boolean activityStart = true;

    private RelativeLayout tooltipLayout;
    private boolean first = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homeclient_layout);
   //     System.out.println(" HomeClientActivity onCreate !!!!!!!!");
        sendScreenAnalytics();

        /*
        checkUser();

        setListener();

        initRequest();
        */
        new initAsync().execute();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("jobbe_local_not"));
    }


/*    private void setParseInstallation() {

        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        ParseUser user = ParseUser.getCurrentUser();
        if (user != null) installation.put("userId", user.getObjectId());
*//*        installation.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

            }
        });*//*
        installation.saveInBackground();
    }*/

    private void sendScreenAnalytics(){
        Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
        t.setScreenName("CLIENTE - HOME");
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void setListener() {
        listLayout = (LinearLayout) findViewById(R.id.requests_linearlayout);
        final Context ctx = HomeClientActivity.this;
        newRequestButton = (Button) findViewById(R.id.newrequestbutton);
        newRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(first)
                    hideTooltipLayout();

                if (Utils.checkConnection(ctx)) {

                    //Get tracker.
                    Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
                    //Build and send an Event.
                    t.send(new HitBuilders.EventBuilder()
                            .setCategory(getString(R.string.analytics_cat_flow_client))
                            .setAction(getString(R.string.analytics_action_click))
                            .setLabel("di_cosa_hai_bisogno")
                            .build());

                    Intent intent = new Intent(HomeClientActivity.this, JobChooserActivity.class);
                    intent.putExtra("purpose", "requestCompletion");
                    startActivity(intent);
                }
            }
        });

        swipeView = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Utils.checkConnection(getApplicationContext()))
                    new refreshAsync().execute();
                    /*
                    swipeView.setRefreshing(true);
                    (new Handler()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (Utils.checkConnection(getApplicationContext()))
                                refreshRequestList();
                            swipeView.setRefreshing(false);
                        }
                    });
                    */

            }
        });

        emptyLayout = (RelativeLayout) findViewById(R.id.empty_layout);
        requestsLayout = (RelativeLayout) findViewById(R.id.requests_relative);
    }

    private boolean checkIfFirstTime(){
        SharedPreferences settings = getPreferences(0);
        this.first = settings.getBoolean("isFirstTime", true);
        return first;
    }

    private void unsetFirstTime(){
        SharedPreferences settings = getPreferences(0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("isFirstTime", false);
        editor.commit();
    }

    private void showTooltipLayout(){
        this.tooltipLayout = (RelativeLayout) findViewById(R.id.tooltip_layout);
        this.tooltipLayout.setVisibility(View.VISIBLE);
    }

    private void hideTooltipLayout(){

        unsetFirstTime(); //Disable tooltip from now on

        this.tooltipLayout = (RelativeLayout) findViewById(R.id.tooltip_layout);
        this.tooltipLayout.setVisibility(View.GONE);
    }

    private Boolean initRequest() throws JobbeSessionException {

        getUserRequests();

        if (requests.size() > 0) {
            popupResult = getRequestsForPopup();
            return false;
        } else
            return true;
        /*
        if (popupResult == null)
            fillLayout();
        else
            launchPopup(popupResult);
        */
    }

    /*
    private void refreshRequestList() {
        getUserRequests();
        listLayout.removeAllViews();

        if (requests.size() > 0) {
            Map<String, String> popupResult = getRequestsForPopup();
            if (popupResult == null)
                fillLayout();
            else
                launchPopup(popupResult);
        } else {
            showEmptyPlaceholder();
        }
    }
    */

    private void getUserRequests() throws JobbeSessionException {
        /*
        user = ParseUser.getCurrentUser();
        try {
            user.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        */
        ArrayList<Integer> illegalStates = new ArrayList<Integer>();
        illegalStates.add(4);
        illegalStates.add(5);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
        query.whereEqualTo("userId", user).whereNotContainedIn("state", illegalStates);
        query.whereNotEqualTo("confirmUncompleteReq", true);
        try {
            requests = (ArrayList<ParseObject>) query.find();
            Log.i("requests", requests.size() + "");
        } catch (ParseException e) {
            ParseErrorHandler.handleParseError(e, getApplicationContext());
            e.printStackTrace();
            throw new JobbeSessionException();
        }
    }

    private void fillLayout() {
        //int numberOfRequests = requests.size();

        offlineData = new JSONObject();
        threadArray = new ArrayList<>();
        try {
            offlineData.put("requests", new JSONArray());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < requests.size(); i++) {
            final RequestListObjectView item = new RequestListObjectView(getApplicationContext());
            final ParseObject request = requests.get(i);
            if (request.getInt("state") == 6)
                continue;

            //Thread x aggiunta asincrona richiesta a JSON offline
            Runnable runnable = new Runnable() {
                public void run() {
                    addToOfflineJson(request);
                }
            };
            Thread mythread = new Thread(runnable);
            mythread.start();
            threadArray.add(mythread);

            item.setRequest(request);

            if (request.getInt("state") != 3)
                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (Utils.checkConnection(getApplicationContext(), true)) {
                            //request.put("unseenProposals", 0);
                            //request.saveInBackground();
                            //item.setUnseenProposals(0);
                            Intent intent = createIntentfromRequestType(request, item);
                            startActivity(intent);
                            overridePendingTransition(R.anim.pushleft_in, R.anim.pushleft_out);
                        } else {
                            if (request.getInt("state") == 2) {
                                try {
                                    spinnerLayout.setVisibility(View.VISIBLE);
                                    Intent intent = new Intent(HomeClientActivity.this, AcceptedBidRecapClientActivity.class);
                                    loadJsonFromFile();
                                    JSONArray requests = offlineData.getJSONArray("requests");
                                    for (int i = 0; i < requests.length(); i++) {
                                        final JSONObject req = requests.getJSONObject(i);
                                        if (req.getString("reqId").equals(request.getObjectId())) {
                                            Log.i("REQ FOUND", req.getString("reqId"));
                                            intent.putExtra("offlineData", req.toString());
                                            break;
                                        }
                                    }
                                    intent.putExtra("iconName", item.getIconName());
                                    startActivity(intent);
                                    spinnerLayout.setVisibility(View.GONE);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else
                                Toast.makeText(getApplicationContext(), getString(R.string.offline_default_alert), Toast.LENGTH_LONG).show();
                        }

                    }
                });


            item.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    if (request.getInt("state") == 2)
                        return true;

                    Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    // Vibrate for 500 milliseconds
                    vib.vibrate(50);

                    new AlertDialog.Builder(HomeClientActivity.this)
                            .setTitle("Attenzione")
                            .setMessage("Vuoi veramente eliminare questa richiesta?")
                            .setNegativeButton(android.R.string.no, null)
                            .setPositiveButton("Conferma", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {

                                    new deleteReqAsync().execute(request.getObjectId());
                                    /*
                                    Map<String, String> params = new HashMap<String, String>();
                                    params.put("reqId", request.getObjectId());
                                    ParseCloud.callFunctionInBackground("deleteRequest", params, new FunctionCallback<Object>() {
                                        @Override
                                        public void done(Object o, ParseException e) {
                                            if (e == null) {
                                                refreshRequestList();
                                                Toast.makeText(getApplicationContext(), "Richiesta eliminata correttamente",
                                                        Toast.LENGTH_LONG).show();
                                            } else e.printStackTrace();
                                        }
                                    });
                                    */


                                }
                            }).create().show();

                    return false;
                }
            });


            listLayout.addView(item);
        }

        //Salvataggio asincrono JSON su file
        new offlineStoreAsync().execute();
    }

    private Map<String, String> getRequestsForPopup() {
        Map<String, String> returnMap;
        for (ParseObject request : requests) {

            if (request.getInt("state") == 3) {
                returnMap = new HashMap<>();
                returnMap.put("type", "3");
                returnMap.put("reqId", request.getObjectId());
                return returnMap;
            } else if (request.getInt("state") == 6) {
                Boolean uncompleteConfirmed = request.getBoolean("confirmUncompleteReq");
                if (!uncompleteConfirmed) {
                    returnMap = new HashMap<>();
                    returnMap.put("type", "6");
                    returnMap.put("reqId", request.getObjectId());
                    return returnMap;
                }
            }

        }
        return null;
    }

    private void launchPopup(Map<String, String> values) {
        if (activityOpen) {
            if (values.get("type").equals("3"))
                launchReviewPopup(values.get("reqId"));
            else if (values.get("type").equals("6"))
                launchIncompletePopup(values.get("reqId"));
        }

    }

    private void launchReviewPopup(String reviewReqId) {
        Intent intent = new Intent(HomeClientActivity.this, JobCompletedActivity.class);
        intent.putExtra("reqId", reviewReqId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }

    private void launchIncompletePopup(String incompleteReqId) {
        Intent intent = new Intent(HomeClientActivity.this, JobNotCompletedActivity.class);
        intent.putExtra("reqId", incompleteReqId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }


    ///////// GESTIONE TOP MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Log.i("MenuId", id + "");
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            if (Utils.checkConnection(getApplicationContext())) {
                Intent intent = new Intent(HomeClientActivity.this, SettingsClientActivity.class);
                startActivity(intent);
            }
        } else if (id == R.id.switch_profile) {
            switchToSupplier();
        } else if (id == R.id.action_faq) {
            if (Utils.checkConnection(getApplicationContext())) {
                Intent intent = new Intent(HomeClientActivity.this, WebViewActivity.class);
                intent.putExtra("url", getClientFaq());
                startActivity(intent);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private String getClientFaq() {
        return getString(R.string.web_view_endpoint) + getString(R.string.faq_cliente);
    }

    private void switchToSupplier() {
        spinnerLayout.setVisibility(View.VISIBLE);
        final boolean isSupplier = ParseUser.getCurrentUser().getBoolean("isSupplier");
        Log.i("isSupplier", isSupplier + "");
        chooseSwitchRoute(isSupplier);
        spinnerLayout.setVisibility(View.GONE);
        //finish(); //Finishing this activity to avoid activity pile increment
    }

    private void chooseSwitchRoute(boolean originalIsSupplier) {
        if (originalIsSupplier) {
            Log.i("isSupplier", originalIsSupplier + "");
            //L'utente è già stato supplier, lo mando alla home giusta
            user = ParseUser.getCurrentUser();
            if (Utils.checkConnection(getApplicationContext(), true)) {
                try {
                    user.fetch();
                } catch (ParseException e) {
                    ParseErrorHandler.handleParseError(e, getApplicationContext());
                    e.printStackTrace();
                    finish();
                    return;
                }
                user.put("type", "supplier");
                try {
                    user.save();
                } catch (ParseException e) {
                    ParseErrorHandler.handleParseError(e, getApplicationContext());
                    e.printStackTrace();
                }
            } else
                user.put("type", "supplier");

            Intent intent = new Intent(HomeClientActivity.this, HomeSupplierActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

        } else {

            new AlertDialog.Builder(HomeClientActivity.this)
                    .setTitle("Attenzione")
                    .setMessage("Stai avviando la procedura che ti farà diventare un fornitore. Vuoi proseguire?")
                    .setNegativeButton("NO", null)
                    .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {

                            //L'utente non è mai stato supplier, lo mando alla registrazione del supplier
                            if (Utils.checkConnection(getApplicationContext())) {
                                Intent intent = new Intent(HomeClientActivity.this, JobChooserActivity.class);
                                intent.putExtra("switchFromClient", true);
                                intent.putExtra("purpose", "supplierSignup");
                                startActivity(intent);
                            }

                        }
                    }).create().show();
        }
    }

    private Intent createIntentfromRequestType(ParseObject request, RequestListObjectView item) {
        int reqState = request.getInt("state");
        Intent intent = null;
        switch (reqState) {
            case 1:
                intent = new Intent(HomeClientActivity.this, ProposalsListActivity.class);
                intent.putExtra("reqId", request.getObjectId());
                break;

            case 2:
                try {
                    ParseObject proposal = request.getParseObject("choosenProposal");
                    proposal.fetchIfNeeded();
                    intent = new Intent(HomeClientActivity.this, AcceptedBidRecapClientActivity.class);
                    intent.putExtra("propID", proposal.getObjectId());
                    intent.putExtra("iconName", item.getIconName());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
        }
        return intent;
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


    private class initAsync extends AsyncTask<Void, Void, Map<String, Boolean>> {


        @Override
        protected void onPreExecute() {
            spinnerLayout = (RelativeLayout) findViewById(R.id.loadingSpinnerLayout);
            spinnerLayout.setVisibility(View.VISIBLE);
            boolean ok = checkUser();
            if (ok)
                setListener();
            else {
                finish();
                cancel(true);
            }
        }

        @Override
        protected Map<String, Boolean> doInBackground(Void... voids) {
            if (Utils.checkConnection(getApplicationContext(), true)) {
                //Get Jobs and Categories Lists
                Boolean empty = null;
                try {
                    empty = initRequest();
                } catch (JobbeSessionException e) {
                    Map result = new HashMap();
                    result.put("ok", false);
                    return result;
                }
                Log.i("LoadingAsync", "getJobsIsOver");
                Map result = new HashMap();
                result.put("online", true);
                result.put("empty", empty);
                result.put("ok", true);
                return result;
            } else {
                offlineProcedure();
                Map result = new HashMap();
                result.put("online", false);
                result.put("ok", true);
                return result;
            }
        }

        @Override
        protected void onPostExecute(Map<String, Boolean> result) {
            Log.i("LoadingAsync", "Into onPostExecute");
            Boolean ok = result.get("ok");
            if (ok) {
                Boolean online = result.get("online");
                if (online) {
                    Boolean empty = result.get("empty");
                    if (empty) {
                        //Show placeholder
                        showEmptyPlaceholder();
                    } else {
                        if (popupResult == null)
                            fillLayout();
                        else
                            launchPopup(popupResult);

                        hideEmptyPlaceholder();
                    }
                } else {
                    offlineFillLayout();
                }

                spinnerLayout.setVisibility(View.GONE);
            }
            else
                finish();
        }

    }

    private void showEmptyPlaceholder() {
        int numberOfSuppliers;
        int minSuppliersToDisplay = 5;
        Map<String, Double> params = new HashMap<>();
        params.put("latitude", (Double) user.get("latitude"));
        params.put("longitude", (Double) user.get("longitude"));
        try {
            numberOfSuppliers = ParseCloud.callFunction("getNearSuppliers", params);
        } catch (ParseException e) {
            numberOfSuppliers = minSuppliersToDisplay;
        }

        TextView numberTextView = (TextView) findViewById(R.id.textView89);

        if (numberOfSuppliers <= minSuppliersToDisplay)
            numberTextView.setText(Integer.toString(minSuppliersToDisplay));
        else
            numberTextView.setText(Integer.toString(numberOfSuppliers));

        requestsLayout.setVisibility(View.GONE);
        emptyLayout.setVisibility(View.VISIBLE);

        //Check if first time -> show tooltip
        if(checkIfFirstTime())
            showTooltipLayout();
    }

    private void hideEmptyPlaceholder() {
        requestsLayout.setVisibility(View.VISIBLE);
        emptyLayout.setVisibility(View.GONE);
    }


    private void addToOfflineJson(ParseObject request) {
        try {
            JSONObject req_object = new JSONObject();
            req_object.put("reqId", request.getObjectId());
            req_object.put("title", request.getString("title"));
            req_object.put("received_proposals", request.getInt("numberOfProposals"));
            req_object.put("icon_name", getIconName(request));

            int state = request.getInt("state");
            req_object.put("state", state);
            if (state == 2) {
                ParseObject proposal = request.getParseObject("choosenProposal");
                ParseObject supplier = request.getParseObject("choosenSupplier");
                proposal.fetch();
                supplier.fetch();
                req_object.put("bid_id", proposal.getObjectId());
                req_object.put("bid_desc", proposal.getString("description"));
                req_object.put("bid_price", proposal.getDouble("price"));
                req_object.put("bid_type", proposal.getString("priceType"));
                req_object.put("supp_type", proposal.getString("type"));
                req_object.put("supp_phone", supplier.getString("username"));
                req_object.put("supp_name", supplier.getString("displayName"));
                req_object.put("supp_job", getSupplierJob(supplier));
                req_object.put("supp_num_review", supplier.getInt("numberOfReviews"));
                req_object.put("supp_avg_rating", supplier.getDouble("averageRating"));

                ArrayList<HashMap<String, Object>> results = (ArrayList<HashMap<String, Object>>) supplier.get("averageRatingSet");
                if (results.size() > 0) {
                    HashMap<String, Object> ratings = results.get(0);
                    //Log.i("ratings", ratings.get("courtesy").toString());
                    req_object.put("supp_price_rating", objToDouble(ratings.get("price")));
                    req_object.put("supp_timing_rating", objToDouble(ratings.get("timing")));
                    req_object.put("supp_quality_rating", objToDouble(ratings.get("quality")));
                    req_object.put("supp_courtesy_rating", objToDouble(ratings.get("courtesy")));
                } else {
                    req_object.put("supp_price_rating", objToDouble(0.0));
                    req_object.put("supp_timing_rating", objToDouble(0.0));
                    req_object.put("supp_quality_rating", objToDouble(0.0));
                    req_object.put("supp_courtesy_rating", objToDouble(0.0));
                }

            }
            offlineData.getJSONArray("requests").put(req_object);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private String getIconName(ParseObject request) {
        ParseObject jobCategory = (ParseObject) request.get("jobCatId");
        try {
            jobCategory.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String iconName = jobCategory.get("icon").toString().replace("-", "_");
        return iconName;
    }

    private String getSupplierJob(ParseObject supplier) {
        ArrayList<ParseObject> resultJob = new ArrayList<>();
        try {
            resultJob = (ArrayList<ParseObject>) ParseQuery.getQuery("Jobs").whereEqualTo("supplierId", supplier).find();
        } catch (ParseException e) {
            ParseErrorHandler.handleParseError(e, getApplicationContext());
            e.printStackTrace();
        }
        return resultJob.get(0).getString("name");
    }

    private double objToDouble(Object value) {
        try {
            return ((double) ((Integer) value).intValue());
        } catch (Exception e) {
            return (double) value;
        }
    }

    private class offlineStoreAsync extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            for (int i = 0; i < threadArray.size(); i++)
                try {
                    threadArray.get(i).join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            saveOfflineJson(offlineData);
            return true;
        }
    }

    private void saveOfflineJson(JSONObject data) {
        Log.i("OFFLINE JSON", data.toString());
        String FILENAME = "jobbe_offline_client.json";
        final File root = new File(Environment.getExternalStorageDirectory() + "/Jobbe/");
        root.mkdirs();
        File file = new File(root, FILENAME);
        Log.i("FILE PATH", file.getPath());
        try {
            FileWriter fw = new FileWriter(file);
            fw.write(data.toString());
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void offlineProcedure() {
        loadJsonFromFile();
        Log.i("OFFLINE DATA LOADED C", offlineData.toString());
        //offlineFillLayout();
    }

    private void loadJsonFromFile() {
        String FILENAME = "jobbe_offline_client.json";
        final File root = new File(Environment.getExternalStorageDirectory() + "/Jobbe/");
        File file = new File(root, FILENAME);
        try {
            FileReader reader = new FileReader(file);
            char[] chars = new char[(int) file.length()];
            reader.read(chars);
            String content = new String(chars);
            reader.close();
            offlineData = new JSONObject(content);
        } catch (JSONException e) {
            //JSONException -> File non valido
            showEmptyPlaceholder();
        } catch (IOException e) {
            //IOException -> File non presente, no offline data
            showEmptyPlaceholder();
        }
    }

    private void offlineFillLayout() {
        listLayout = (LinearLayout) findViewById(R.id.requests_linearlayout);
        try {
            JSONArray requests = offlineData.getJSONArray("requests");
            for (int i = 0; i < requests.length(); i++) {
                final JSONObject req = requests.getJSONObject(i);

                final RequestListObjectView item = new RequestListObjectView(getApplicationContext());
                item.setRequestTitle(req.getString("title"));
                item.setNumberOfProposals(req.getInt("received_proposals"));
                item.setUnseenProposals(0);
                item.setOfflineRequestPicture(req.getString("icon_name"));
                final int state = req.getInt("state");
                item.setRequestState(state);


                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (state == 2) {
                            Intent intent = new Intent(HomeClientActivity.this, AcceptedBidRecapClientActivity.class);
                            intent.putExtra("offlineData", req.toString());
                            intent.putExtra("iconName", item.getIconName());
                            startActivity(intent);
                        } else
                            Toast.makeText(getApplicationContext(), getString(R.string.offline_default_alert), Toast.LENGTH_LONG).show();
                    }
                });

                listLayout.addView(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private boolean checkUser() {
 //       System.out.println(" checkUser !!!!!!!!");
        user = ParseUser.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else if (Utils.checkConnection(getApplicationContext(), true)) {
 /*           try {
     //           System.out.println(" user.fetch() start !!!!!!!!");
             //   user.fetch();
    //            System.out.println(" user.fetch() end !!!!!!!!");
            } catch (Exception e) {
          //      ParseErrorHandler.handleParseError(e, getApplicationContext());
                e.printStackTrace();
                return false;
            }*/
        }
        return true;
    }

    private class refreshAsync extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            Log.i("refreshAsync", "Start");
            swipeView.setRefreshing(true);
        }


        @Override
        protected Boolean doInBackground(Void... par) {
            Log.i("refreshAsync", "GetUserRequests");
            try {
                getUserRequests();
            } catch (JobbeSessionException e) {
                //e.printStackTrace();
                return false;
            }

            Log.i("refreshAsync", "GetUserRequests over");
            return true;
        }


        @Override
        protected void onPostExecute(Boolean ok) {
            if(ok) {
                listLayout.removeAllViews();

                if (requests.size() > 0) {
                    Map<String, String> popupResult = getRequestsForPopup();
                    if (popupResult == null)
                        fillLayout();
                    else
                        launchPopup(popupResult);
                } else {
                    showEmptyPlaceholder();
                }

                Log.i("refreshAsync", "Remove Refresh icon");
                swipeView.setRefreshing(false);

                Log.i("refreshAsync", "FINISH");
            }
            else
                finish();
        }
    }


    private class deleteReqAsync extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            Log.i("refreshAsync", "Start");
            swipeView.setRefreshing(true);
        }


        @Override
        protected Boolean doInBackground(String... strings) {
            String reqId = strings[0];
            Log.i("refreshAsync", "GetUserRequests");
            Map<String, String> params = new HashMap<String, String>();
            params.put("reqId", reqId);
            try {
                ParseCloud.callFunction("deleteRequest", params);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            //Get tracker.
            Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
            //Build and send an Event.
            t.send(new HitBuilders.EventBuilder()
                    .setCategory(getString(R.string.analytics_cat_quality_client))
                    .setAction(getString(R.string.analytics_action_click))
                    .setLabel("richiesta_annullata")
                    .build());
            //getUserRequests();

            try {
                getUserRequests();
            } catch (JobbeSessionException e) {
                return false;
            }

            return true;

            //return null;
        }


        @Override
        protected void onPostExecute(Boolean ok) {
            if (ok) {
                Toast.makeText(getApplicationContext(), "Richiesta eliminata correttamente",
                        Toast.LENGTH_LONG).show();

                listLayout.removeAllViews();

                if (requests.size() > 0) {
                    Map<String, String> popupResult = getRequestsForPopup();
                    if (popupResult == null)
                        fillLayout();
                    else
                        launchPopup(popupResult);
                } else {
                    showEmptyPlaceholder();
                }

                swipeView.setRefreshing(false);

                Log.i("refreshAsync", "FINISH");
            }
            else
                finish();
        }
    }


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            String type = intent.getStringExtra("type");
            Log.i("receiver", "Got message: " + message + ", type: " + type);
            //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

            if(type.equals("new_proposal") || type.equals("complete_request") || type.equals("incomplete_request")) {
                new refreshAsync().execute();
                Log.i("activityOpen", activityOpen + "");
            }

        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        try {
            Badges.removeBadge(this);
            Badges.setBadge(this, 0);
        } catch (BadgesNotSupportedException badgesNotSupportedException) {
            Log.d(TAG, badgesNotSupportedException.getMessage());
        }


        JobbeApplication.activityResumed();
        Utils.deleteNotifications(getApplicationContext());
        activityOpen = true;

        // Non lancio il refresh se è appena stato fatto l'init
        if (!activityStart)
            new refreshAsync().execute();
        else
            activityStart = false;

        Utils.deleteNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();
        JobbeApplication.activityPaused();
        activityOpen = false;
    }


}
