package com.division70.jobbe.jobbe_git;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.venmo.view.TooltipView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class HomeSupplierActivity extends Activity {
    private ArrayList<ParseObject> requests;
    private ParseUser user;
    private LinearLayout aggiudicate_List;
    private LinearLayout inviate_List;
    private LinearLayout inattesa_List;
    private SwipeRefreshLayout swipeView;
    private RelativeLayout aggiudicateRelative;
    private RelativeLayout inviateRelative;
    private RelativeLayout inattesaRelative;
    private RelativeLayout spinnerLayout;
    private RelativeLayout mainLayout;
    private RelativeLayout emptyLayout;
    private JSONObject offlineData;
    private ArrayList<Thread> threadArray;
    private boolean activityOpen;

    private RelativeLayout tooltipLayout;
    private RelativeLayout okTooltip;
    private Menu menu;
    private RelativeLayout tooltipAssignedLayout;
    private boolean firstTimeAssigned = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homesupplier_layout);

        sendScreenAnalytics();

        new initAsync().execute();

        /*
        checkUser();

        setLayouts();
        hideLists();

        if (Utils.checkConnection(getApplicationContext(), true)) {
            Log.i("ONLINE", "ONLINE");
            getUserRequests(false);
        }
        else {
            Log.i("OFFLINE", "OFFLINE");
            //Offline Procedure
            offlineProcedure();
        }

        setRefreshView();
        */

        //fillLayout();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("jobbe_local_not"));
    }

    private void sendScreenAnalytics(){
        Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
        t.setScreenName("FORNITORE - HOME");
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void setLayouts(){
        mainLayout = (RelativeLayout) findViewById(R.id.main_rl);
        emptyLayout = (RelativeLayout) findViewById(R.id.empty_layout);

        offlineData = new JSONObject();
        try {
            offlineData.put("requests",new JSONArray());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        this.tooltipAssignedLayout = (RelativeLayout) findViewById(R.id.assignedTooltipLayout);
    }

    private void hideLists(){
        aggiudicateRelative = (RelativeLayout) findViewById(R.id.aggiudicate_rl);
        inviateRelative = (RelativeLayout) findViewById(R.id.inviate_rl);
        inattesaRelative = (RelativeLayout) findViewById(R.id.inattesa_rl);
        spinnerLayout = (RelativeLayout) findViewById(R.id.loadingSpinnerLayout);

        aggiudicateRelative.setVisibility(View.GONE);
        inviateRelative.setVisibility(View.GONE);
        inattesaRelative.setVisibility(View.GONE);

    }

    private void getUserRequests(final boolean refresh){
        requests = new ArrayList<>();
        try {
            user.fetch();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ArrayList<String> requestsId = (ArrayList<String>) user.get("matchingRequests");
        for (String id: requestsId) {
            ParseObject x = ParseObject.createWithoutData("Request",id);
            requests.add(x);
        }

        try {
            ParseObject.fetchAll(requests);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Log.i("requests", requests.size() + "");

        /*
        if (refresh) {
            aggiudicate_List.removeAllViews();
            inviate_List.removeAllViews();
            inattesa_List.removeAllViews();
        }
        fillLayout();
        */



    }

    private void fillLayout(){

        aggiudicate_List = (LinearLayout) findViewById(R.id.aggiudicate_linearlayout);
        inviate_List = (LinearLayout) findViewById(R.id.inviate_linearlayout);
        inattesa_List = (LinearLayout) findViewById(R.id.inattesa_linearlayout);

        int agg_number = 0;
        int inv_number = 0;
        int inatt_number = 0;

        threadArray = new ArrayList<>();
        try {
            offlineData.put("requests", new JSONArray());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        boolean retired = false;
        boolean expired = true;

        for (final ParseObject request : requests){
             retired = false;
             expired = true;

            System.out.println(" Current UserID == " + user.getObjectId() +" ,  Request UserId ==  " + request.getParseObject("userId").getObjectId());

            if(!request.getParseObject("userId").getObjectId().equals(user.getObjectId()) ) {


                int state = request.getInt("state");
                Log.i("FILLLAYOUT", request.getString("title") + " " + state);
                //Non metto l'oggetto nella lista se:
                if (state == 3 || state == 4 || state == 6)
                    continue;      // (1) Il lavoro è terminato o incompleto
                // SE richiesta ritirata o assegnata a altri -> disable
                if (state == 5 || (request.getParseObject("choosenProposal") != null && request.getParseObject("choosenSupplier") != user)) {
                    retired = true;
                }

                int cat;

                cat = newCategorizeRequest(request);  // 1 -> aggiudicata, 2-> inviata, 3-> in attesa
                // cat = categorizeRequest(request);  // 1 -> aggiudicata, 2-> inviata, 3-> in attesa
                Log.i("category", request.getString("title") + " " + cat);

                if (isRequestExpired(request) && cat == 3) {
                    retired = true;
                    expired = true;
                }

                if (retired) {
                    if (cat == 2 || cat == 3)
                        cat = -cat;

                }
                final int category = cat;

                //Thread x aggiunta asincrona richiesta a JSON offline
                Runnable runnable = new Runnable() {
                    public void run() {
                        addToOfflineJson(request, category);
                    }
                };
                Thread mythread = new Thread(runnable);
                mythread.start();
                threadArray.add(mythread);

                final String requestId = request.getObjectId();
                SupplierHomeListObjectView item = new SupplierHomeListObjectView(getApplicationContext());
                item.setRequest(request);
                if (category != 10 && category > 0) {
                    item.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent;
                            switch (category) {
                                case 1:
                                    if (firstTimeAssigned) {
                                        disableFirstAssignedTooltip();
                                        unsetFirstTimeAssigned();
                                    }
                                    intent = new Intent(HomeSupplierActivity.this, AcceptedBidRecapSupplierActivity.class);
                                    break;
                                case 2:
                                    intent = new Intent(HomeSupplierActivity.this, BidRecapSupplierActivity.class);
                                    break;
                                case 3:
                                    intent = new Intent(HomeSupplierActivity.this, RequestRecapActivity.class);
                                    break;
                                default:
                                    intent = new Intent();
                            }

                            intent.putExtra("reqId", requestId);
                            if (!Utils.checkConnection(getApplicationContext(), true)) {
                                try {
                                    spinnerLayout.setVisibility(View.VISIBLE);
                                    loadJsonFromFile();
                                    JSONArray requests = offlineData.getJSONArray("requests");
                                    for (int i = 0; i < requests.length(); i++) {
                                        final JSONObject req = requests.getJSONObject(i);
                                        if (req.getString("reqId").equals(requestId)) {
                                            intent.putExtra("offlineDetails", req.toString());
                                            break;
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }


                            startActivity(intent);
                            overridePendingTransition(R.anim.pushleft_in, R.anim.pushleft_out);
                            spinnerLayout.setVisibility(View.GONE);
                        }
                    });

                    item.setOnLongClickListener(new View.OnLongClickListener() {

                        @Override
                        public boolean onLongClick(View v) {
                            if (category == 1)
                                return true; // Non è possibile eliminare le richieste già assegnate

                            if (category == 2) {
                                if (Utils.checkConnection(getApplicationContext()))
                                    removeProposal(request);
                            } else {
                                if (Utils.checkConnection(getApplicationContext()))
                                    removeRequest(request);
                            }

                            return false;
                        }
                    });

                } else if (category == 10) {
                    //Preventivo cancellato dal fornitore
                    item.disable();
                    item.setOnLongClickListener(new View.OnLongClickListener() {

                        @Override
                        public boolean onLongClick(View v) {
                            if (Utils.checkConnection(getApplicationContext()))
                                removeRequest(request);

                            return false;
                        }
                    });
                } else {
                    //category < 0 -> Richiesta ritirata
                    if (expired)
                        item.setExpired();
                    else
                        item.setRetired();

                    item.setOnLongClickListener(new View.OnLongClickListener() {

                        @Override
                        public boolean onLongClick(View v) {
                            if (category == 1)
                                return true; // Non è possibile eliminare le richieste già assegnate

                            if (Utils.checkConnection(getApplicationContext()))
                                removeRequest(request);

                            return false;
                        }
                    });
                }


                switch (category) {
                    case 1:

                        aggiudicate_List.addView(item);
                        agg_number++;

                        break;

                    case -1:

                        aggiudicate_List.addView(item);
                        agg_number++;

                        break;

                    case 2:

                        inviate_List.addView(item);
                        inv_number++;

                        break;

                    case -2:

                        inviate_List.addView(item);
                        inv_number++;

                        break;

                    case 10:

                        inviate_List.addView(item);
                        inv_number++;

                        break;

                    case 3:

                        inattesa_List.addView(item);
                        inatt_number++;

                        break;

                    case -3:

                        inattesa_List.addView(item);
                        inatt_number++;

                        break;
                }
            }
        }

        /*...................... End Of For ......................*/


        if (agg_number == 0 && inv_number == 0 && inatt_number == 0){
            showEmptyPlaceholder();
        }
        else {
            /*
            if (agg_number == 0) aggiudicateRelative.setVisibility(View.GONE);
            else aggiudicateRelative.setVisibility(View.VISIBLE);

            if (inv_number == 0) inviateRelative.setVisibility(View.GONE);
            else inviateRelative.setVisibility(View.VISIBLE);

            if (inatt_number == 0) inattesaRelative.setVisibility(View.GONE);
            else inattesaRelative.setVisibility(View.VISIBLE);
            */

            aggiudicateRelative.setVisibility(View.VISIBLE);
            inviateRelative.setVisibility(View.VISIBLE);
            inattesaRelative.setVisibility(View.VISIBLE);
            hideEmptyPlaceholder();

            if (agg_number > 0){
                firstTimeAssigned = checkIfFirstTimeAssigned();
                if (firstTimeAssigned){
                    enableFirstAssignedTooltip();
                }
            }
        }

        spinnerLayout.setVisibility(View.GONE);

        //Salvataggio asincrono JSON su file
        new offlineStoreAsync().execute();
    }

    private void enableFirstAssignedTooltip() {
        tooltipAssignedLayout.setVisibility(View.VISIBLE);
    }

    private void disableFirstAssignedTooltip() {
        tooltipAssignedLayout.setVisibility(View.GONE);
    }

    private boolean isRequestExpired(ParseObject request) {
        Log.i("isRequestExpired", request.getString("title") + " " + request.getString("timingType"));
        if (request.getString("timingType").equals("specific")) {
            Date reqDate = request.getDate("dateRequest");
            Date today = new Date();
            Log.i("isRequestExpired", "req-> " + reqDate.toString() + " | today->  " + today.toString());

            //if (reqDate.before(today)) return true;

            Calendar reqDateCal = Calendar.getInstance();
            reqDateCal.setTime(reqDate);
            int reqDateDay = reqDateCal.get(Calendar.DAY_OF_MONTH);
            int reqDateMonth = reqDateCal.get(Calendar.MONTH);
            int reqDateYear = reqDateCal.get(Calendar.YEAR);
            String reqDateString = String.valueOf(reqDateDay) + "-" + String.valueOf(reqDateMonth) + "-" + String.valueOf(reqDateYear);

            Calendar todayCal = Calendar.getInstance();
            todayCal.setTime(today);
            int todayDay = todayCal.get(Calendar.DAY_OF_MONTH);
            int todayMonth = todayCal.get(Calendar.MONTH);
            int todayYear = todayCal.get(Calendar.YEAR);
            String todayString = String.valueOf(todayDay) + "-" + String.valueOf(todayMonth) + "-" + String.valueOf(todayYear);

            SimpleDateFormat sdf = new SimpleDateFormat("dd-mm-yyyy HH:mm:ss");
            try {
                Date reqDateUntimed = sdf.parse(reqDateString + " 00:00:01");
                Date todayUntimed = sdf.parse(todayString + " 00:00:01");
                Log.i("isRequestExpired", "UNTIMED: req-> " + reqDateUntimed.toString() + " | today-> " + todayUntimed.toString());
                if (todayUntimed.after(reqDateUntimed)) return true;
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }


            //if (new Date().after(today)) {
            //    return true;
            //}
        }
        return false;
    }

    private void removeRequest(final ParseObject request){
        Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        vib.vibrate(50);

        new AlertDialog.Builder(HomeSupplierActivity.this)
                .setTitle(request.getString("title"))
                .setMessage("Vuoi eliminare questa richiesta?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton("Elimina", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        new deleteRequestAsync().execute(request.getObjectId());
                        /*
                        spinnerLayout.setVisibility(View.VISIBLE);
                        arg0.dismiss();
                        removeFromMatchingRequests(request.getObjectId());
                        getUserRequests(true);
                        Toast.makeText(getApplicationContext(), "Richiesta eliminata correttamente",Toast.LENGTH_LONG).show();
                        spinnerLayout.setVisibility(View.GONE);
                        */
                    }
                }).create().show();
    }

    private void removeProposal(final ParseObject request){
        Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        vib.vibrate(50);

        new AlertDialog.Builder(HomeSupplierActivity.this)
                .setTitle(request.getString("title"))
                .setMessage("Vuoi eliminare questa proposta?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton("Elimina", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        arg0.dismiss();
                        new deleteProposalAsync(request).execute();
                    }
                }).create().show();
    }

    public void removeFromMatchingRequests(String reqId){
        try {
            user.fetch();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ArrayList<String> requestsId = (ArrayList<String>) user.get("matchingRequests");
        for(String x: requestsId)
                if(x.equals(reqId)) {
                requestsId.remove(x);
                break;
            }
        user.put("matchingRequests", requestsId);
        try {
            user.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    public int newCategorizeRequest(ParseObject request){

        try {
          //  user.fetchIfNeeded();
            user.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    if(e == null){
                        System.out.println(" categorizeRequest Success");
                    }else{
                        System.out.println(" categorizeRequest Exception Output ===  " + e);
                    }
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }

        int state = request.getInt("state");

        ArrayList<String> proposalSupplierId = (ArrayList<String>) request.get("proposalSupplierId");

        if (proposalSupplierId == null){
            return 3;
        }else if(! proposalSupplierId.contains(user.getObjectId())){
            return 3;
        }else if(state == 1){
            return 2;
        } else if(state == 2){
            return 1;
        }

        try {
            Log.i("ids", request.getObjectId() + " - " + user.getObjectId());
            List<ParseObject> results = ParseQuery.getQuery("Proposal").whereEqualTo("requestId", request).whereEqualTo("supplierId",user).find();
            Log.i("ProposalsNumber", results.size() + "");
            for (ParseObject x : results){
                Log.i("deletedRequest", x.getBoolean("deleted") + " " + x.getBoolean("deletedBySupplier"));
                if (x.getBoolean("deletedBySupplier")) return 10; // Preventivo annullato da fornitore
            }
        } catch (ParseException e) {
            ParseErrorHandler.handleParseError(e, getApplicationContext());
            e.printStackTrace();
        }

        return 0;
    }



    public int categorizeRequest(ParseObject request){
        try {
            user.fetchIfNeeded();
            user.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    if(e == null){
                        System.out.println(" categorizeRequest Success");
                    }else{
                        System.out.println(" categorizeRequest Exception Output ===  " + e);
                    }
                }
            });
        } catch (ParseException e){
            e.printStackTrace();
        }

        ArrayList<String> proposalSupplierId = (ArrayList<String>) request.get("proposalSupplierId");

        if (proposalSupplierId == null)
            return 3;

        else if(! proposalSupplierId.contains(user.getObjectId()))
            return 3; //In attesa

        ParseObject choosenSupplier = (ParseObject) request.get("choosenSupplier");

        if (choosenSupplier != null){
            System.out.println("Choosen Supllier Not Null");
            if(choosenSupplier.equals(user))
                return 1; //Aggiudicata
        }else{
            System.out.println("Choosen Supllier Null");
        }


        try {
            Log.i("ids", request.getObjectId() + " - " + user.getObjectId());
            List<ParseObject> results = ParseQuery.getQuery("Proposal").whereEqualTo("requestId", request).whereEqualTo("supplierId",user).find();
            Log.i("ProposalsNumber", results.size() + "");
            for (ParseObject x : results){
                Log.i("deletedRequest", x.getBoolean("deleted") + " " + x.getBoolean("deletedBySupplier"));
                if (x.getBoolean("deletedBySupplier")) return 10; // Preventivo annullato da fornitore
            }
        } catch (ParseException e) {
            ParseErrorHandler.handleParseError(e, getApplicationContext());
            e.printStackTrace();
        }

        return 2; //Inviata
    }


    ///////// GESTIONE TOP MENU

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        this.menu=menu;
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            if (Utils.checkConnection(getApplicationContext())) {
                Intent intent = new Intent(HomeSupplierActivity.this, SupplierProfileActivity.class);
                startActivity(intent);
            }
        }
        else if (id == R.id.switch_profile){
            switchToClient();
            finish(); //Finishing this activity to avoid activity pile increment
        }
        else if(id == R.id.action_faq){
            if (Utils.checkConnection(getApplicationContext())) {
                Intent intent = new Intent(HomeSupplierActivity.this, WebViewActivity.class);
                intent.putExtra("url", getSupplierFaq());
                startActivity(intent);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private String getSupplierFaq(){
        return getString(R.string.web_view_endpoint) + getString(R.string.faq_fornitore);
    }

    /*
    private void switchToClient(){
        // Cambio il tipo di utente a client
        user = ParseUser.getCurrentUser();
        user.put("type", "client");
        try{
            user.save();
        }
        catch (ParseException e){
            Toast.makeText(getApplicationContext(), "C\'è stato un errore durante il salvataggio.", Toast.LENGTH_LONG).show();
            return;
        }

        int reqNumber = getUserRequestsNumber(user);
        Log.i("Request cliente", Integer.toString(reqNumber));
        if (reqNumber == 0){
            Intent intent = new Intent(HomeSupplierActivity.this, EmptyCustomerHomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        else {
            Intent intent = new Intent(HomeSupplierActivity.this, HomeClientActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
    */


    private void switchToClient(){
        spinnerLayout.setVisibility(View.VISIBLE);
        // Cambio il tipo di utente a client
        user = ParseUser.getCurrentUser();
        user.put("type", "client");
        if(Utils.checkConnection(getApplicationContext(), true)) {
            try {
                user.save();
            } catch (ParseException e) {
                //Toast.makeText(getApplicationContext(), "C\'è stato un errore durante il salvataggio.", Toast.LENGTH_LONG).show();
                ParseErrorHandler.handleParseError(e, getApplicationContext());
                return;
            }
        }
        Intent intent = new Intent(HomeSupplierActivity.this, HomeClientActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        spinnerLayout.setVisibility(View.GONE);
    }

    private void setRefreshView(){
        swipeView = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                new refreshAsync().execute();
                /*
                swipeView.setRefreshing(true);
                (new Handler()).post(new Runnable() {
                    @Override
                    public void run() {
                        if(Utils.checkConnection(getApplicationContext()))
                            getUserRequests(true);
                        swipeView.setRefreshing(false);
                    }
                });
                */


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

    private void showEmptyPlaceholder(){

        int numberOfClients;
        int minClientsToDisplay = 10;

        Map<String,Double> params = new HashMap<>();

        ParseGeoPoint coords = user.getParseGeoPoint("locationCords");
        //params.put("latitude",(Double) user.get("latitude"));
        //params.put("longitude",(Double) user.get("longitude"));
        params.put("latitude", coords.getLatitude());
        params.put("longitude", coords.getLongitude());
        Log.i("coords", coords.getLatitude() + " " + coords.getLongitude());
        try{
            numberOfClients = ParseCloud.callFunction("getNearClients",params);
        }
        catch (ParseException e){
            Log.i("e", e.getMessage());
            numberOfClients = minClientsToDisplay;
        }
        Log.i("numberOfClients", numberOfClients + "");

        TextView numberTextView = (TextView) findViewById(R.id.textView13);
        if (numberOfClients <= minClientsToDisplay)
            numberTextView.setText(Integer.toString(minClientsToDisplay));
        else
            numberTextView.setText(Integer.toString(numberOfClients));

        mainLayout.setVisibility(View.GONE);
        emptyLayout.setVisibility(View.VISIBLE);

        //Check if first time -> show tooltip
        if(checkIfFirstTimeEmpty())
            showTooltipLayout();
    }

    private void hideEmptyPlaceholder(){
        mainLayout.setVisibility(View.VISIBLE);
        emptyLayout.setVisibility(View.GONE);
    }

    private boolean checkIfFirstTimeEmpty(){
        SharedPreferences settings = getPreferences(0);
        boolean first = settings.getBoolean("isFirstTimeEmpty", true);
        return first;
    }

    private void unsetFirstTimeEmpty(){
        SharedPreferences settings = getPreferences(0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("isFirstTimeEmpty", false);
        editor.commit();
    }

    private boolean checkIfFirstTimeAssigned(){
        SharedPreferences settings = getPreferences(0);
        boolean first = settings.getBoolean("isFirstTimeAssigned", true);
        return first;
    }

    private void unsetFirstTimeAssigned(){
        SharedPreferences settings = getPreferences(0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("isFirstTimeAssigned", false);
        editor.commit();
    }

    private void showTooltipLayout(){

        this.tooltipLayout = (RelativeLayout) findViewById(R.id.tooltip_layout);
        this.okTooltip = (RelativeLayout) findViewById(R.id.ok_layout);

        this.tooltipLayout.setVisibility(View.VISIBLE);

        this.menu.findItem(R.id.switch_profile).setIcon(getResources().getDrawable(R.mipmap.menu_tooltip_switch));
        this.menu.findItem(R.id.menu_overflow).setIcon(getResources().getDrawable(R.mipmap.menu_tooltip_faq));

        this.okTooltip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideTooltipLayout();
            }
        });
    }

    private void hideTooltipLayout(){

        unsetFirstTimeEmpty(); //Disable tooltip from now on

        this.tooltipLayout = (RelativeLayout) findViewById(R.id.tooltip_layout);
        this.tooltipLayout.setVisibility(View.GONE);
        this.menu.findItem(R.id.switch_profile).setIcon(getResources().getDrawable(R.mipmap.menu_switch_icon));
        this.menu.findItem(R.id.menu_overflow).setIcon(getResources().getDrawable(R.drawable.abc_ic_menu_moreoverflow_mtrl_alpha));
    }


    //GESTIONE OFFLINE


    //SALVATAGGIO
    private void addToOfflineJson(ParseObject request, int category){
        try {
            JSONObject req_object = new JSONObject();
            req_object.put("reqId", request.getObjectId());
            req_object.put("title", request.getString("title"));
            req_object.put("timingString", getTimingString(request));

            req_object.put("category", category);
            if (category == 1) {
                //Add request data
                ParseUser client = request.getParseUser("userId").fetch();
                ParseObject proposal = request.getParseObject("choosenProposal").fetch();
                //JSONObject details = new JSONObject();
                req_object.put("client_name", client.getString("displayName"));
                req_object.put("client_id", client.getObjectId());
                req_object.put("req_title", request.getString("title"));
                req_object.put("timingString", getTimingString(request));
                req_object.put("req_address", request.getString("locationAddress"));
                req_object.put("req_desc", request.getString("description"));
                req_object.put("bid_desc", proposal.getString("description"));
                req_object.put("bid_price", proposal.getDouble("price"));
                req_object.put("bid_type", proposal.getString("priceType"));
                req_object.put("latitude", request.getParseGeoPoint("locationCords").getLatitude());
                req_object.put("longitude", request.getParseGeoPoint("locationCords").getLongitude());
                req_object.put("client_phone", client.getUsername());
            }
            offlineData.getJSONArray("requests").put(req_object);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private String getTimingString(ParseObject request) {
        String timingType = request.getString("timingType");
        Date date = new Date();
        if (timingType.equals("specific")) date = (Date) request.get("dateRequest");

        String text = "";
        switch (timingType) {
            case "notUrgent": text = "Da concordare/non urgente"; break;
            case "soonAsPossible": text = "Il prima possibile"; break;
            case "specific":
                if(date == null){
                    Log.i("Error","date cannot be null with specific timing");
                }
                else{
                    text = createDateString(date);
                }
                break;
        }
        return text;
    }

    public String createDateString(Date date){
        String dayOfTheWeek = (String) android.text.format.DateFormat.format("EEEE", date);//Thursday
        String stringMonth = (String) android.text.format.DateFormat.format("MMM", date); //Jun
        String intMonth = (String) android.text.format.DateFormat.format("MM", date); //06
        String year = (String) android.text.format.DateFormat.format("yyyy", date); //2013
        String day = (String) android.text.format.DateFormat.format("dd", date); //20
        return day + "/" + intMonth + "/" + year;
    }

    private void saveOfflineJson(JSONObject data){
        Log.i("OFFLINE JSON", data.toString());
        String FILENAME = "jobbe_offline_supplier.json";
        final File root = new File(Environment.getExternalStorageDirectory() + "/Jobbe/");
        root.mkdirs();
        File file = new File(root,FILENAME);
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

    //CARICAMENTO
    private void offlineProcedure(){
        spinnerLayout.setVisibility(View.VISIBLE);
        loadJsonFromFile();
        Log.i("OFFLINE DATA LOADED S", offlineData.toString());

        offlineFillLayout();
    }

    private void loadJsonFromFile(){
        String FILENAME = "jobbe_offline_supplier.json";
        final File root = new File(Environment.getExternalStorageDirectory() + "/Jobbe/");
        File file = new File(root,FILENAME);
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
        aggiudicate_List = (LinearLayout) findViewById(R.id.aggiudicate_linearlayout);
        inviate_List = (LinearLayout) findViewById(R.id.inviate_linearlayout);
        inattesa_List = (LinearLayout) findViewById(R.id.inattesa_linearlayout);

        int agg_number = 0;
        int inv_number = 0;
        int inatt_number = 0;

        try {
            JSONArray requests = offlineData.getJSONArray("requests");
            for (int i = 0; i < requests.length(); i++) {
                final JSONObject req = requests.getJSONObject(i);
                SupplierHomeListObjectView item = new SupplierHomeListObjectView(getApplicationContext());
                item.setRequestTitle(req.getString("title"));
                item.setTimingText(req.getString("timingString"));
                int category = req.getInt("category");
                switch (category){
                    case 1:
                        item.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(HomeSupplierActivity.this, AcceptedBidRecapSupplierActivity.class);
                                intent.putExtra("offlineDetails", req.toString());
                                startActivity(intent);
                            }
                        });
                        aggiudicate_List.addView(item);
                        agg_number++;
                        break;
                    case 2:
                        item.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(getApplicationContext(), "Questa funzione non è disponibile offline", Toast.LENGTH_LONG).show();
                            }
                        });
                        inviate_List.addView(item);
                        inv_number++;
                        break;
                    case 3:
                        item.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(getApplicationContext(), "Questa funzione non è disponibile offline", Toast.LENGTH_LONG).show();
                            }
                        });
                        inattesa_List.addView(item);
                        inatt_number++;
                        break;

                    case -1:
                        item.setRetired();
                        aggiudicate_List.addView(item);
                        agg_number++;
                        break;
                    case -2:
                        item.setRetired();
                        inviate_List.addView(item);
                        inv_number++;
                        break;
                    case -3:
                        item.setRetired();
                        inattesa_List.addView(item);
                        inatt_number++;
                        break;
                }
            }

            if (agg_number == 0 && inv_number == 0 && inatt_number == 0){
                showEmptyPlaceholder();
            }
            else {
                /*
                if (agg_number == 0) aggiudicateRelative.setVisibility(View.GONE);
                else aggiudicateRelative.setVisibility(View.VISIBLE);

                if (inv_number == 0) inviateRelative.setVisibility(View.GONE);
                else inviateRelative.setVisibility(View.VISIBLE);

                if (inatt_number == 0) inattesaRelative.setVisibility(View.GONE);
                else inattesaRelative.setVisibility(View.VISIBLE);
                */
                aggiudicateRelative.setVisibility(View.VISIBLE);
                inviateRelative.setVisibility(View.VISIBLE);
                inattesaRelative.setVisibility(View.VISIBLE);

                hideEmptyPlaceholder();
            }

            spinnerLayout.setVisibility(View.GONE);



        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class offlineStoreAsync extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            for(int i = 0; i < threadArray.size(); i++)
                try {
                    threadArray.get(i).join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            saveOfflineJson(offlineData);
            return true;
        }
    }

    private void checkUser(){
        user = ParseUser.getCurrentUser();
        if (user == null){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else
        if (Utils.checkConnection(getApplicationContext(),true)) {
            try {
                user.fetch();
            } catch (ParseException e) {
                ParseErrorHandler.handleParseError(e, getApplicationContext());
                e.printStackTrace();
            }
        }
    }



    private class initAsync extends AsyncTask<Void, Void, Void> {
        private Boolean online;

        @Override
        protected void onPreExecute() {

            spinnerLayout = (RelativeLayout) findViewById(R.id.loadingSpinnerLayout);
            spinnerLayout.setVisibility(View.VISIBLE);

            checkUser();
            setLayouts();
            hideLists();

            online = Utils.checkConnection(getApplicationContext(), true);

        }

        @Override
        protected Void doInBackground(Void... par) {
            if (online) {
                Log.i("ONLINE", "ONLINE");
                getUserRequests(false);
            }
            else {
                Log.i("OFFLINE", "OFFLINE");
                //Offline Procedure
                //offlineProcedure();
                loadJsonFromFile();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if (online)
                fillLayout();
            else
                offlineFillLayout();

            setRefreshView();
            spinnerLayout.setVisibility(View.GONE);
        }

    }


    private class refreshAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            Log.i("refreshAsync", "Start");
            swipeView.setRefreshing(true);
        }


        @Override
        protected Void doInBackground(Void... par) {
            Log.i("refreshAsync", "GetUserRequests");
            getUserRequests(false);
            Log.i("refreshAsync", "GetUserRequests over");
            return null;
        }


        @Override
        protected void onPostExecute(Void v) {
            Log.i("refreshAsync", "RemoveViews");
            aggiudicate_List.removeAllViews();
            inviate_List.removeAllViews();
            inattesa_List.removeAllViews();

            Log.i("refreshAsync", "Fill Layout");
            fillLayout();

            Log.i("refreshAsync", "Remove Refresh icon");
            swipeView.setRefreshing(false);

            Log.i("refreshAsync", "FINISH");
        }

    }


    private class deleteProposalAsync extends AsyncTask<Void, Void, Void> {

        private ParseObject request;
        private ParseObject proposta;

        public deleteProposalAsync(ParseObject request){
            this.request = request;
        }

        @Override
        protected void onPreExecute() {
            Log.i("refreshAsync", "Start");
            swipeView.setRefreshing(true);
        }


        @Override
        protected Void doInBackground(Void... par) {

            try {
                proposta = ParseQuery.getQuery("Proposal")
                        .whereEqualTo("requestId", request)
                        .whereEqualTo("supplierId", user).getFirst();
            } catch (ParseException e) {
                ParseErrorHandler.handleParseError(e, getApplicationContext());
                e.printStackTrace();
            }

            Map<String, Object> delete = new HashMap<>();
            delete.put("reqId", request.getObjectId());
            delete.put("proposalId", proposta.getObjectId());
            delete.put("deleted", "NO");
            delete.put("deletedBySupplier", "YES");
            try {
                ParseCloud.callFunction("deleteProposal", delete); //Delete proposal by supplier
            } catch (ParseException e) {
                e.printStackTrace();
            }

            removeFromMatchingRequests(request.getObjectId());

            getUserRequests(false); //Refresh view

            return null;
        }


        @Override
        protected void onPostExecute(Void v) {
            aggiudicate_List.removeAllViews();
            inviate_List.removeAllViews();
            inattesa_List.removeAllViews();

            Log.i("refreshAsync", "Fill Layout");
            fillLayout();

            Log.i("refreshAsync", "Remove Refresh icon");
            swipeView.setRefreshing(false);

            Toast.makeText(getApplicationContext(), "Richiesta eliminata correttamente",
                    Toast.LENGTH_LONG).show();
        }
    }


    private class deleteRequestAsync extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute(){swipeView.setRefreshing(true);}

        @Override
        protected Void doInBackground(String... params){
            String reqId = params[0];
            removeFromMatchingRequests(reqId);
            getUserRequests(true);
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {

            aggiudicate_List.removeAllViews();
            inviate_List.removeAllViews();
            inattesa_List.removeAllViews();

            Log.i("refreshAsync", "Fill Layout");
            fillLayout();

            Toast.makeText(getApplicationContext(), "Richiesta eliminata correttamente",Toast.LENGTH_LONG).show();
            swipeView.setRefreshing(false);
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

            if(type.equals("new_request") || type.equals("proposal_accepted")) {
                new refreshAsync().execute();
            }

        }
    };

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        super.onResume();
        try {
            Badges.removeBadge(this);
            Badges.setBadge(this, 0);
        } catch (BadgesNotSupportedException badgesNotSupportedException) {
            Log.d("hdcg", badgesNotSupportedException.getMessage());
        }

        JobbeApplication.activityResumed();
        Utils.deleteNotifications(getApplicationContext());
        activityOpen = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        JobbeApplication.activityPaused();
        activityOpen = false;
    }




}
