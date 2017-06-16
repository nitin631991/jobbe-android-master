package com.division70.jobbe.jobbe_git;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.SupportMapFragment;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.venmo.view.TooltipView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by giorgio on 09/11/15.
 */
public class BidCreationActivity extends Activity {

    private TextView clientName;
    private TextView reqAddress;
    private TextView reqTiming;
    private EditText bidPrice;
    private ActionEditText bidDetails;

    private Button confirmBid;
    private Button typeCorpo;
    private Button typeMisura;

    private String corpoLabel = "corpo";
    private String misuraLabel = "ore";
    private String selectedType = corpoLabel; //Default
    private String buttonEnabledColor = "#bedadb";
    private String buttonDisabledColor = "#e5e8e9";

    private String title;
    private String name;
    private String address;
    private String timing;

    private String reqId;
    private RelativeLayout spinnerLayout;

    private ParseObject request;
    private TooltipView tooltip;
    private boolean firstTime = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bidcreation_layout);

        sendScreenAnalytics("FORNITORE - CREA PROPOSTA - FORM");

        this.init();
        setUpActionBar();
    }

    /*
    private void setUpActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        CustomActionbar customActionbar = new CustomActionbar(this);
        customActionbar.setTitle(this.title);
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
    */

    private void setUpActionBar(){
        try {
            request = ParseQuery.getQuery("Request").whereEqualTo("objectId", reqId).getFirst();
        } catch (ParseException e) {
            e.printStackTrace();
        }
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
                finish();
            }
        });

        RelativeLayout title = (RelativeLayout) customActionbar.findViewById(R.id.title_layout);
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BidCreationActivity.this, RequestsArchiveDetailsActivity.class);
                intent.putExtra("reqId", request.getObjectId());
                startActivity(intent);
            }
        });
    }

    private void sendScreenAnalytics(String screenName){
        Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
        t.setScreenName(screenName);
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void init() {
        this.clientName = (TextView) findViewById(R.id.client_name);
        this.reqAddress = (TextView) findViewById(R.id.req_address);
        this.reqTiming = (TextView) findViewById(R.id.req_timing);
        this.bidDetails = (ActionEditText) findViewById(R.id.bid_details);
        this.bidPrice = (EditText) findViewById(R.id.bid_price);
        this.confirmBid = (Button) findViewById(R.id.bid_confirm_button);
        this.typeCorpo = (Button) findViewById(R.id.type_corpo);
        this.typeMisura = (Button) findViewById(R.id.type_misura);
        this.spinnerLayout = (RelativeLayout) findViewById(R.id.loadingSpinnerLayout);
        this.tooltip = (TooltipView) findViewById(R.id.tooltip);

        bidDetails.setHorizontallyScrolling(false);
        bidDetails.setLines(1);
        bidDetails.setMaxLines(6);

        setLabels();
        setListeners();

        firstTime = checkIfFirstTime();
        if (firstTime) enableTooltip();
    }

    private void setLabels() {
        this.title = getIntent().getStringExtra("title");
        this.name = getIntent().getStringExtra("name");
        this.address = getIntent().getStringExtra("address");
        this.timing = getIntent().getStringExtra("timing");
        this.reqId = getIntent().getStringExtra("reqId");

        this.clientName.setText(this.name);
        this.reqAddress.setText(this.address);
        this.reqTiming.setText(this.timing);
    }

    private void setListeners() {
        this.confirmBid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendScreenAnalytics("FORNITORE - CREA PROPOSTA - PAGINA RIEPILOGO");

                if (checkBidFields()) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(BidCreationActivity.this);
                    builder.setTitle(getString(R.string.bid_creation_alert_title))
                            .setMessage(getString(R.string.bid_creation_alert_desc))
                            .setCancelable(false)
                            .setPositiveButton("Conferma", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                /*
                                createBid(); //Creating bid
                                Toast.makeText(getApplicationContext(), "Creazione preventivo completata",
                                        Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                                Intent intent = new Intent(BidCreationActivity.this, HomeSupplierActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                                */
                                    new createBidAsync().execute();
                                }
                            }).setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });


        this.typeCorpo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableCorpo();
            }
        });

        this.typeMisura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableMisura();
            }
        });

        this.bidPrice.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (firstTime) {
                    disableTooltip();
                    unsetFirstTime();
                    firstTime = false;
                }
                return false;
            }
        });
    }

    private boolean isRequestValid(){
        //request = null;
        try {
            request = ParseQuery.getQuery("Request").whereEqualTo("objectId", reqId).getFirst();
            request.fetch();

            if(request.getInt("state") != 1)
                return false;
            else
                return true;

        } catch (ParseException e) {
            ParseErrorHandler.handleParseError(e, getApplicationContext());
            e.printStackTrace();
        }

        return false;
    }

    private void createBid() {
        ParseUser supplier = ParseUser.getCurrentUser();
        ParseObject request = getRequestPointerFromID();
        ParseUser client = (ParseUser) request.get("userId");
        String propType = getPropType(supplier, request);
        if (request != null && supplier != null) {
            Map<String, Object> bid = new HashMap<>();
            bid.put("supplierId", supplier.getObjectId());
            bid.put("clientId", client.getObjectId());
            bid.put("reqId", request.getObjectId());
            bid.put("price", Float.parseFloat(this.bidPrice.getText().toString()));
            bid.put("desc", this.bidDetails.getText().toString());
            //bid.put("propType", "professionist");
            bid.put("propType", propType);
            bid.put("priceType", this.selectedType);
            bid.put("supplierDisplayName", supplier.getString("displayName"));
            bid.put("reqTitle", this.title);
            //ParseCloud.callFunctionInBackground("createProposal", bid); //Adding proposal
            try {
                ParseCloud.callFunction("createProposal", bid); //Adding proposal
            } catch (ParseException e) {
                ParseErrorHandler.handleParseError(e, getApplicationContext());
                e.printStackTrace();
            }

            //Get tracker.
            Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
            //Build and send an Event.
            t.send(new HitBuilders.EventBuilder()
                    .setCategory(getString(R.string.analytics_cat_pref_supp))
                    .setAction(getString(R.string.analytics_action_click))
                    .setLabel(this.selectedType.toLowerCase())
                    .build());
        }
        //else
            //Toast.makeText(getApplicationContext(), "Qualcosa è andato storto, per favore riprova",
            //        Toast.LENGTH_LONG).show();
    }

    private ParseObject getRequestPointerFromID() {
        ArrayList<ParseObject> request = null;
        try {
            request = (ArrayList<ParseObject>) ParseQuery.getQuery("Request").whereEqualTo("objectId", this.reqId).find();
            return request.get(0);
        } catch (ParseException e) {
            e.printStackTrace();
            ParseErrorHandler.handleParseError(e, getApplicationContext());
        }
        return null;
    }

    private String getPropType(ParseUser supplier, ParseObject request) {
        try {
            Log.i("getPropType", "Job id: " + request.getParseObject("job").getObjectId());
            Log.i("getPropType", "Supp id: " + supplier.getObjectId());
            ParseObject job = ParseQuery.getQuery("Jobs")
                    .whereEqualTo("jobListId", request.getParseObject("job"))
                    .whereEqualTo("supplierId", supplier).getFirst();

            String propType = job.getString("supplierType");
            if (propType == null)
                return "hobby";
            Log.i("propType", propType);
            Log.i("job", job.getString("name"));
            return propType;
        } catch (ParseException e) {
            e.printStackTrace();
            ParseErrorHandler.handleParseError(e, getApplicationContext());
            return "hobby";
        } catch (NullPointerException e){
            e.printStackTrace();
            return "hobby";
        }
        //return "hobby";
    }

    private boolean checkBidFields() {
        if (this.bidPrice.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Inserisci il prezzo della proposta.",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        else if (Float.parseFloat(this.bidPrice.getText().toString()) >= 10000){
            Toast.makeText(getApplicationContext(), "Inserisci un prezzo corretto per la tua proposta.",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        else if (this.bidDetails.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Inserisci i dettagli dell'intervento da svolgere.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        else if(!new RegexValidator(getApplicationContext()).validate(this.bidDetails.getText().toString()))
            return false;

        return true;
    }

    private void enableCorpo() {
        this.typeCorpo.setBackgroundColor(Color.parseColor(this.buttonEnabledColor));
        this.typeMisura.setBackgroundColor(Color.parseColor(this.buttonDisabledColor));
        this.selectedType = corpoLabel;
    }

    private void enableMisura() {
        this.typeMisura.setBackgroundColor(Color.parseColor(this.buttonEnabledColor));
        this.typeCorpo.setBackgroundColor(Color.parseColor(this.buttonDisabledColor));
        this.selectedType = misuraLabel;
    }


    private class createBidAsync extends AsyncTask<Void, Void, Boolean> {


        @Override
        protected void onPreExecute() {
            spinnerLayout.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... par) {
            if(!isRequestValid())
                return false;

            createBid(); //Creating bid
            return true;
        }

        @Override
        protected void onPostExecute(Boolean state) {
            spinnerLayout.setVisibility(View.GONE);

            if(state) {
                Toast.makeText(getApplicationContext(), "Creazione proposta completata", Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(getApplicationContext(), "Il cliente ha eliminato questa richiesta",
                        Toast.LENGTH_LONG).show();
            }

            Intent intent = new Intent(BidCreationActivity.this, HomeSupplierActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
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

    private void enableTooltip() {
        tooltip.setVisibility(View.VISIBLE);
    }

    private void disableTooltip() {
        tooltip.setVisibility(View.GONE);
    }

    private boolean checkIfFirstTime(){
        SharedPreferences settings = getPreferences(0);
        boolean first = settings.getBoolean("isFirstTime", true);
        return first;
    }

    private void unsetFirstTime(){
        SharedPreferences settings = getPreferences(0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("isFirstTime", false);
        editor.commit();
    }
}