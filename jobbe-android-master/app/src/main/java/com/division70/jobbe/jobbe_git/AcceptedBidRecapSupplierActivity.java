package com.division70.jobbe.jobbe_git;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.arturogutierrez.Badges;
import com.github.arturogutierrez.BadgesNotSupportedException;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by giorgio on 09/11/15.
 */
public class AcceptedBidRecapSupplierActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private TextView clientName;
    private TextView reqAddress;
    private TextView reqTiming;
    private TextView description;
    private TextView bidPrice;
    private TextView bidDetails;
    private TextView address;
    private Button jobDone;
    private Button jobNotDone;
    private RelativeLayout phoneCall;

    private String reqId;
    private String reqTitle;

    private ParseUser supplier;
    private ParseObject request;
    private ParseObject proposta;
    private ParseObject client;
    private ParseUser reqUser;

    private ParseGeoPoint center;
    private double latitude;
    private double longitude;

    private String offlineData;
    private String phonenumber;
    private String client_name;
    private String request_address;
    private String timing_string;
    private String bid_desc;
    private String bid_price;
    private String req_desc;
    private String clientId;

    private JSONObject offlineDataJSON;
    private RelativeLayout spinnerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acceptedbidrecapsupplier_layout);

        this.init();

        try {
            Badges.removeBadge(this);
            Badges.setBadge(this, 0);
        } catch (BadgesNotSupportedException badgesNotSupportedException) {
            Log.d("wydtws", badgesNotSupportedException.getMessage());
        }

        /*
        setUpMapIfNeeded();
        addPosToMap(mMap, this.center);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setUpActionBar();
        */
    }

    private void setUpActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        CustomActionbar customActionbar = new CustomActionbar(this);
        customActionbar.setTitle(reqTitle);
        customActionbar.disableSubtitle();
        actionBar.setCustomView(customActionbar);
        actionBar.setDisplayShowCustomEnabled(true);

        ImageView back = (ImageView) customActionbar.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backFunction();
            }
        });
        getActionBar().setDisplayHomeAsUpEnabled(false);
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
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
    }

    private CircleOptions newCircleOptions(LatLng coords) {
        return new CircleOptions()
                .radius(70)
                .center(coords)
                .strokeColor(Color.BLUE)
                .strokeWidth(4)
                .fillColor(0x8571BED1);
    }


    private void init() {
        new initAsync().execute();
    }

    /*
    private void init(){
        try {
            this.clientName = (TextView) findViewById(R.id.client_name);
            this.reqAddress = (TextView) findViewById(R.id.req_address);
            this.reqTiming = (TextView) findViewById(R.id.req_timing);
            this.description = (TextView) findViewById(R.id.description);
            this.bidPrice = (TextView) findViewById(R.id.bid_price);
            this.bidDetails = (TextView) findViewById(R.id.bid_details);
            this.address = (TextView) findViewById(R.id.address);
            this.jobDone = (Button) findViewById(R.id.job_done_button);
            this.jobNotDone = (Button) findViewById(R.id.job_not_done_button);
            this.phoneCall = (RelativeLayout) findViewById(R.id.call_layout);

            this.reqId = getIntent().getStringExtra("reqId");

            offlineData = getIntent().getStringExtra("offlineDetails");
            if(offlineData == null) {
                this.supplier = ParseUser.getCurrentUser();
                this.supplier.fetchIfNeeded();

                this.request = ParseObject.createWithoutData("Request", this.reqId);
                this.request.fetch();

                this.proposta = (ParseObject) this.request.get("choosenProposal");
                this.proposta.fetchIfNeeded();

                this.client = (ParseObject) this.request.get("userId");
                this.client.fetchIfNeeded();

                getOnlineData();


            }
            else {
                getOfflineData();
            }


            setReqTextFields();

            setListeners();

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    */

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 23:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the task you need to do.

                    phoneAlert();
                } else {
                    return;
                    // permission denied, boo! Disable the functionality that depends on this permission.
                }
                break;

        }
    }

public void phoneAlert(){
    final AlertDialog.Builder builder = new AlertDialog.Builder(AcceptedBidRecapSupplierActivity.this);
    builder.setTitle(getString(R.string.phone_call_alert_title))
            .setMessage("+39 " + phonenumber)
            .setCancelable(false)
            .setPositiveButton("Chiama", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {


                    String uri = "tel:" + phonenumber;
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(uri));
                    if (ActivityCompat.checkSelfPermission(AcceptedBidRecapSupplierActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        ActivityCompat.requestPermissions(AcceptedBidRecapSupplierActivity.this, new String[]{android.Manifest.permission.CALL_PHONE}, 23);

                        return;
                    }
                    startActivity(intent);

                    dialog.dismiss();
                }
            }).setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
            dialog.dismiss();
        }
    });
    AlertDialog alert = builder.create();
    alert.show();
}



    private void setListeners() {

        this.phoneCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        this.jobDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Utils.checkConnection(getApplicationContext()))
                    return;

                final AlertDialog.Builder builder = new AlertDialog.Builder(AcceptedBidRecapSupplierActivity.this);
                builder.setTitle(getString(R.string.job_done_alert_title))
                        .setMessage(getString(R.string.job_done_alert_desc))
                        .setCancelable(false)
                        .setPositiveButton("Conferma", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                Map<String, Object> complete = new HashMap<>();
                                complete.put("reqId", reqId);
                                complete.put("reqTitle", reqTitle);
                                complete.put("clientId", clientId);
                                try {
                                    ParseCloud.callFunction("completeRequest", complete); //Delete proposal by supplier
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                removeFromMatchingRequests();

                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Lavoro concluso",
                                        Toast.LENGTH_LONG).show();

                                //Get tracker.
                                Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
                                //Build and send an Event.
                                t.send(new HitBuilders.EventBuilder()
                                        .setCategory(getString(R.string.analytics_cat_pref_supp))
                                        .setAction(getString(R.string.analytics_action_click))
                                        .setLabel("lavoro_concluso")
                                        .build());

                                toSupplierHome();
                            }
                        }).setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Annulla",
                                Toast.LENGTH_LONG).show();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        this.jobNotDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Utils.checkConnection(getApplicationContext()))
                    return;

                Intent intent = new Intent(AcceptedBidRecapSupplierActivity.this, JobUndoneActivity.class);
                intent.putExtra("reqId", request.getObjectId());
                intent.putExtra("clientId", client.getObjectId());
                intent.putExtra("userType", "supplier"); //Says it is the SUPPLIER who is setting the job as UNDONE
                startActivity(intent);
            }
        });
    }

    private void getOnlineData(){
        try {
            Log.i("getOnlineData", "Start");
            this.reqTitle = this.request.get("title").toString();
            Log.i("getOnlineData", "gotReqTitle");
            this.reqUser = (ParseUser) this.request.get("userId");
            this.reqUser.fetchIfNeeded();
            Log.i("getOnlineData", "gotReqUser");

            this.client_name = reqUser.get("displayName").toString();
            Log.i("getOnlineData", "gotClientName");
            this.clientId = reqUser.getObjectId();
            Log.i("getOnlineData", "gotClientId");
            this.center = (ParseGeoPoint) this.request.get("locationCords");
            Log.i("getOnlineData", "gotCenter");
            this.latitude = this.center.getLatitude();
            Log.i("getOnlineData", "gotLatitude");
            this.longitude = this.center.getLongitude();
            Log.i("getOnlineData", "gotLongitude");
            this.phonenumber = client.get("username").toString();
            Log.i("getOnlineData", "gotPhonenumber");
            //this.request_address = new GoogleMapsGeocodingRequest()
            //        .getAddressFromCoords(new LatLng(this.latitude, this.longitude));
            Log.i("getOnlineData", "gotRequestAddress");
            this.timing_string = getTimingFromTimingType(this.request.get("timingType").toString(), null);
            Log.i("getOnlineData", "gotTimingString");
            this.bid_price = this.proposta.get("price").toString() + "€ " + this.proposta.get("priceType").toString();
            Log.i("getOnlineData", "gotBidPrice");
            this.req_desc = this.request.get("description").toString();
            Log.i("getOnlineData", "gotRequestDesc");
            this.bid_desc = this.proposta.get("description").toString();
            Log.i("getOnlineData", "gotPropDesc");

        } catch (ParseException e) {
            e.printStackTrace();

        }/* catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        */

    }

    private void setReqTextFields(){
        this.clientName.setText(client_name);
        this.reqTiming.setText(timing_string);
        this.description.setText(req_desc);
        this.bidPrice.setText(bid_price);
        this.bidDetails.setText(bid_desc);
        //String address = new GoogleMapsGeocodingRequest()
        //        .getAddressForMapFromCoords(new LatLng(this.latitude, this.longitude));
        this.reqAddress.setText(request_address);
        this.address.setText("nei pressi di " + request_address);
    }

    public String getTimingFromTimingType(String timing, Date date){
        String text = "";
        switch (timing) {
            case "notUrgent": text = "Da concordare/non urgente"; break;
            case "soonAsPossible": text = "Il prima possibile"; break;
            case "specific":
                if(date == null){
                    Log.i("Error", "date cannot be null with specific timing");
                }
                else{
                    text = createDateString(date);
                }
                break;
        }
        return text;
    }

    public String createDateString(Date date){
        String intMonth = (String) android.text.format.DateFormat.format("MM", date); //06
        String year = (String) android.text.format.DateFormat.format("yyyy", date); //2013
        String day = (String) android.text.format.DateFormat.format("dd", date); //20
        return day + "/" + intMonth + "/" + year;
    }

    private void addPosToMap(GoogleMap googleMap, ParseGeoPoint geoPos){
        googleMap.clear(); //Delete markers & circles for further positions
        googleMap.addMarker(new MarkerOptions()
                .position(geoposToLatlng(geoPos)));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(geoposToLatlng(geoPos)));
        googleMap.addCircle(this.newCircleOptions(geoposToLatlng(geoPos)));
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(16));
    }

    private LatLng geoposToLatlng(ParseGeoPoint point){
        return new LatLng(point.getLatitude(), point.getLongitude());
    }

    private void toSupplierHome(){
        Intent intent = new Intent(AcceptedBidRecapSupplierActivity.this, HomeSupplierActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void removeFromMatchingRequests(){
        try {
            supplier.fetch();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ArrayList<String> matchingRequests = (ArrayList<String>) this.supplier.get("matchingRequests");
        for (String x : matchingRequests)
            if (x.equals(reqId)) {
                matchingRequests.remove(x);
                break;
            }
        supplier.put("matchingRequests", matchingRequests);
        try {
            supplier.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed(){
        backFunction();
    }

    private void backFunction(){
        Boolean fromPush = getIntent().getBooleanExtra("fromPush", false);
        if (!fromPush)
            finish();
        else
            toSupplierHome();
    }

    private void getOfflineData(){
        try {
            offlineDataJSON = new JSONObject(offlineData);
            this.reqId = offlineDataJSON.getString("reqId");
            this.reqTitle = offlineDataJSON.getString("req_title");
            this.latitude = offlineDataJSON.getDouble("latitude");
            this.longitude = offlineDataJSON.getDouble("longitude");
            this.phonenumber = offlineDataJSON.getString("client_phone");
            this.center = new ParseGeoPoint(this.latitude,this.longitude);
            this.reqTitle = offlineDataJSON.getString("req_title");
            this.request_address = offlineDataJSON.getString("req_address");
            this.timing_string = offlineDataJSON.getString("timingString");
            this.client_name = offlineDataJSON.getString("client_name");
            this.clientId = offlineDataJSON.getString("client_id");

            this.bid_price = offlineDataJSON.getDouble("bid_price") + "€ " + offlineDataJSON.getString("bid_type");
            this.req_desc = offlineDataJSON.getString("req_desc");
            this.bid_desc = offlineDataJSON.getString("bid_desc");

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    private class initAsync extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            clientName = (TextView) findViewById(R.id.client_name);
            reqAddress = (TextView) findViewById(R.id.req_address);
            reqTiming = (TextView) findViewById(R.id.req_timing);
            description = (TextView) findViewById(R.id.description);
            bidPrice = (TextView) findViewById(R.id.bid_price);
            bidDetails = (TextView) findViewById(R.id.bid_details);
            address = (TextView) findViewById(R.id.address);
            jobDone = (Button) findViewById(R.id.job_done_button);
            jobNotDone = (Button) findViewById(R.id.job_not_done_button);
            phoneCall = (RelativeLayout) findViewById(R.id.call_layout);

            reqId = getIntent().getStringExtra("reqId");

            offlineData = getIntent().getStringExtra("offlineDetails");
            spinnerLayout = (RelativeLayout) findViewById(R.id.loadingSpinnerLayout);
            spinnerLayout.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... par) {
            if(offlineData == null) {
                try {
                    Log.i("DoInBackground", "online");
                    supplier = ParseUser.getCurrentUser();
                    supplier.fetchIfNeeded();
                    Log.i("DoInBackground", "Supplier fetched");

                    request = ParseObject.createWithoutData("Request", reqId);
                    request.fetch();
                    Log.i("DoInBackground", "Request fetched");

                    proposta = (ParseObject) request.get("choosenProposal");
                    proposta.fetchIfNeeded();
                    Log.i("DoInBackground", "Proposal fetched");

                    client = (ParseObject) request.get("userId");
                    client.fetchIfNeeded();
                    Log.i("DoInBackground", "Client fetched");

                    getOnlineData();
                    Log.i("DoInBackground", "Got online data");
                } catch (ParseException e){
                    ParseErrorHandler.handleParseError(e, getApplicationContext());
                    e.printStackTrace();
                    //getOfflineData();
                    return false;
                }

            }
            else {
                Log.i("DoInBackground", "GettingOffline");
                getOfflineData();
                Log.i("DoInBackground", "Got offline data");
            }
            //return null;
            return true;
        }

        @Override
        protected void onPostExecute(Boolean ok) {
            if (ok) {
                if (offlineData == null) {
                    // Spostato qui perchè dava problemi con il doInBackground
                    try {
                        request_address = new GoogleMapsGeocodingRequest().getAddressFromCoords(new LatLng(latitude, longitude));
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Log.i("OnPostExecute", "Start");
                setReqTextFields();
                Log.i("OnPostExecute", "ReqTextFieldsSet");
                setListeners();
                Log.i("OnPostExecute", "ListenersSet");
                setUpMapIfNeeded();
                Log.i("OnPostExecute", "MapSetupped");
                addPosToMap(mMap, center);
                Log.i("OnPostExecute", "PosAdded");
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(AcceptedBidRecapSupplierActivity.this);
                Log.i("OnPostExecute", "GotMap");
                setUpActionBar();
                Log.i("OnPostExecute", "ActionBarSet");
                spinnerLayout.setVisibility(View.GONE);
            } else
                finish();

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        JobbeApplication.activityPaused();
    }
}