package com.division70.jobbe.jobbe_git;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FunctionCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by giorgio on 02/11/15.
 */
public class RequestConfirmationActivity extends FragmentActivity implements OnMapReadyCallback {

    private static GoogleMap mMap; // Might be null if Google Play services APK is not available.s

    private TextView requestTitle;
    private TextView reqTime;
    private TextView reqDesc;
    private TextView address;
    private Button endRequestButton;

    private String selectedJob;
    private double latitude = 0; //Default
    private double longitude = 0; //Default

    public boolean isPositionSet = false; //Default, just to make sure that the user fills it

    private String title;
    private String description;
    private String defTime = "Il prima possibile"; //Default time option

    private ParseObject reqJob;
    private ParseObject reqJobCategory;

    private ArrayList<ParseObject> zones;

    private ImageView jobIcon;
    private String iconPath;
    private String color;
    private String jobName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.requestconfirmation_layout);

        sendScreenAnalytics();

        this.init();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setUpActionBar();
    }

    private void setUpActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        CustomActionbar customActionbar = new CustomActionbar(this);
        customActionbar.setTitle("Completa richiesta");
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
        t.setScreenName("CLIENTE - CREA RICHIESTA - CONFERMA RICHIESTA");
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish(); //No need to reopen the previous activity because already running
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void init(){
        try {
            setUpMap(); //Initialize mMap

            this.requestTitle = (TextView) findViewById(R.id.req_title);
            this.reqTime = (TextView) findViewById(R.id.req_time);
            this.reqDesc = (TextView) findViewById(R.id.req_desc);
            this.address = (TextView) findViewById(R.id.address);

            this.selectedJob = getIntent().getStringExtra("selectedJob");
            this.latitude = getIntent().getDoubleExtra("latitude", 0);
            this.longitude = getIntent().getDoubleExtra("longitude", 0);
            this.title = getIntent().getStringExtra("reqTitle");
            this.description = getIntent().getStringExtra("reqDesc");
            this.defTime = getIntent().getStringExtra("reqTime");

            this.requestTitle.setText(this.title);
            this.reqTime.setText(this.defTime);
            this.reqDesc.setText(this.description);

            String address = new GoogleMapsGeocodingRequest()
                    .getAddressForMapFromCoords(new LatLng(this.latitude, this.longitude));
            this.address.setText("nei pressi di " + address);

            this.endRequestButton = (Button) findViewById(R.id.fine_button);

            try {
                this.zones = (ArrayList<ParseObject>) ParseQuery.getQuery("Zone").whereEqualTo("active", true).find(); //Getting zones
            } catch (ParseException e) {
                ParseErrorHandler.handleParseError(e, getApplicationContext());
                e.printStackTrace();
            }

            this.jobName = getIntent().getStringExtra("selectedJob");

            setBackground();
            setListeners();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private ParseObject getCategoryFromJobName() {
        try {
            Log.i("CAT NAME:", getIntent().getStringExtra("category"));
            ArrayList<ParseObject> categories = (ArrayList<ParseObject>) ParseQuery.getQuery("CategoryList").whereEqualTo("name", getIntent().getStringExtra("category")).find();
            return categories.get(0);
        } catch (ParseException e) {
            ParseErrorHandler.handleParseError(e, getApplicationContext());
            e.printStackTrace();
        }
        return null;
    }

    private void setBackground() {
        this.reqJobCategory = getCategoryFromJobName();
        RelativeLayout reqRecapLayout = (RelativeLayout) findViewById(R.id.req_recap_layout);
        this.jobIcon = (ImageView) findViewById(R.id.job_icon);
        this.color = "#" + this.reqJobCategory.get("color").toString();
        reqRecapLayout.setBackgroundColor(Color.parseColor(this.color));

        this.iconPath = this.reqJobCategory.get("icon").toString().replace("-","_");
        setRequestPicture();
    }

    private void setRequestPicture(){
        this.iconPath = this.iconPath + "_alert";
        int resourceId = getResources().getIdentifier(iconPath, "drawable", "com.division70.jobbe.jobbe_git");
        this.jobIcon.setImageResource(resourceId);
    }

    private void setListeners(){
        this.endRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new saveRequestAsync().execute();
            }
        });
    }

    private Date getReqDate(){
        switch (this.defTime){
            case "Il prima possibile":
                return null;
            case "Da concordare/non urgente":
                return null;
            default:
                try {
                    SimpleDateFormat  format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    Log.i("Date", this.defTime);
                    return format.parse(this.defTime + " 18:00");
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                }
                return null;
        }
    }

    private String getReqType(){
        switch (this.defTime){
            case "Il prima possibile":
                return "soonAsPossible";
            case "Da concordare/non urgente":
                return "notUrgent";
            default:
                return "specific";
        }
    }

    private void getJobFromName() {
        Map<String, Object> empty = new HashMap<>();
        HashMap<String, Object> result = null; //Synchronous call in order to wait until categories retrieval
        try {
            result = ParseCloud.callFunction("getJobs", empty);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String jobTofind = this.selectedJob;
        String jobID = "";
        ArrayList<ParseObject> categorylist = (ArrayList<ParseObject>) result.get("jobCategory");
        for (ParseObject category : categorylist) {
            ArrayList<HashMap<String, String>> jobs = (ArrayList<HashMap<String, String>>) category.get("jobs");
            for (HashMap<String, String> x : jobs) {
                if (x.get("name").equals(jobTofind)) {
                    //this.reqJobCategory = category;
                    jobID = x.get("id");
                    break;
                }
            }
        }

        ArrayList<ParseObject> joblist = (ArrayList<ParseObject>) result.get("jobList");
        for(ParseObject job : joblist){
            if(job.getObjectId().equals(jobID)) {
                this.reqJob = job;
                break;
            }
        }
    }

    private void setUpMap() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                //setMapListener();
            }
        }
        else
            //Here if the user went on the next activity and came back
            //Clearing the map
            mMap.clear();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.latitude = getIntent().getDoubleExtra("latitude", 0);
        this.longitude = getIntent().getDoubleExtra("longitude", 0);

        ParseGeoPoint center = new ParseGeoPoint(this.latitude, this.longitude);
        addPosToMap(googleMap, center);
    }


    private void addPosToMap(GoogleMap googleMap, ParseGeoPoint geoPos){
        googleMap.clear(); //Delete markers & circles for further positions
        googleMap.addMarker(new MarkerOptions()
                .position(geoposToLatlng(geoPos)));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(geoposToLatlng(geoPos)));
        googleMap.addCircle(this.newCircleOptions(geoposToLatlng(geoPos)));
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(16));
    }

    private static CircleOptions newCircleOptions(LatLng coords){
        return new CircleOptions()
                .radius(70)
                .center(coords)
                .strokeColor(Color.BLUE)
                .strokeWidth(5)
                .fillColor(0x8571BED1);
    }

    private static LatLng geoposToLatlng(ParseGeoPoint point){
        return new LatLng(point.getLatitude(), point.getLongitude());
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private class saveRequestAsync extends AsyncTask<Void, Void, Boolean> {

        ParseObject provincia = null;
        ParseGeoPoint coords;
        String address = null;
        RelativeLayout spinnerLayout;

        @Override
        protected void onPreExecute() {

            spinnerLayout = (RelativeLayout) findViewById(R.id.loadingSpinnerLayout);
            spinnerLayout.setVisibility(View.VISIBLE);

            System.out.println(" Latitude ==  " + latitude + " Longitude ==  " + longitude);

            coords = new ParseGeoPoint(latitude, longitude);
            try {
                provincia = new GoogleMapsGeocodingRequest().getProvinceFromCoords(geoposToLatlng(coords), zones);
                address = new GoogleMapsGeocodingRequest().getAddressFromCoords(geoposToLatlng(coords));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Boolean doInBackground(Void... par) {

            try {
                ParseUser user = ParseUser.getCurrentUser();
                getJobFromName();
                if(provincia != null){
                    Log.i("PROVINCIAZZZZ", provincia.get("nome").toString());
                    ParseObject newRequest = new ParseObject("Request");
                    newRequest.put("title", title);
                    newRequest.put("description", description);
                    Date date = getReqDate();
                    if(date == null)
                    {} //Do nothing with the date
                    else
                        newRequest.put("dateRequest", date);
                    newRequest.put("job", reqJob);
                    newRequest.put("jobCatId", reqJobCategory);
                    newRequest.put("locationAddress", address);
                    newRequest.put("locationCords", coords);
                    newRequest.put("locationName", provincia.get("nome").toString());
                    newRequest.put("provincia", provincia.get("sigla").toString());
                    newRequest.put("state", 1); //Open request - 1
                    newRequest.put("timingType", getReqType());
                    newRequest.put("userId", user);
                    newRequest.put("zoneId", provincia);
                    newRequest.put("nReminder", 0);
                    newRequest.save(); //Saving request sinchronously

                    Map<String, Object> req = new HashMap<>();
                    req.put("reqId", newRequest.getObjectId());
                    req.put("jobId", reqJob.getObjectId());
                    req.put("zoneId", provincia.getObjectId());
                    req.put("reqTitle", title);
                    req.put("jobName", reqJob.get("name"));
                    req.put("zoneName", provincia.get("nome"));
                    ParseCloud.callFunctionInBackground("addRequestToSupplier", req, new FunctionCallback<Object>() {
                        @Override
                        public void done(Object object, ParseException e) {
                            if(e == null){
                                System.out.println("addRequestToSupplier Push Success !");
                            }else{
                                System.out.println("addRequestToSupplier Push UnSuccess !");
                            }
                        }
                    });
       //             ParseCloud.callFunctionInBackground("addRequestToSupplier", req); //Adding request to suppliers in this zone

                    //Get tracker.
                    Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
                    //Build and send an Event.
                    t.send(new HitBuilders.EventBuilder()
                            .setCategory(getString(R.string.analytics_cat_flow_client))
                            .setAction(getString(R.string.analytics_action_click))
                            .setLabel("invia_richiesta")
                            .build());
                    //Build timing stats
                    t.send(new HitBuilders.EventBuilder()
                            .setCategory(getString(R.string.analytics_cat_usability_client))
                            .setAction(getString(R.string.analytics_action_urgenza))
                            .setLabel(getReqType().toLowerCase())
                            .build());
                    //Build geolocalization stats if GPS enabled
                    if(getIntent().getBooleanExtra("gpsEnabled", false)) {
                        t.send(new HitBuilders.EventBuilder()
                                .setCategory(getString(R.string.analytics_cat_usability_client))
                                .setAction(getString(R.string.analytics_action_click))
                                .setLabel("geolocalizzato")
                                .build());
                    }
                    return true;
                }
                else
                    return false;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean state) {
            if(!state){
                Toast.makeText(getApplicationContext(),
                        "Provincia non attiva attualmente", Toast.LENGTH_LONG).show();
                finish();
            }
            else {
                spinnerLayout.setVisibility(View.GONE);
                Intent intent = new Intent(RequestConfirmationActivity.this, RelaxActivity.class);
                intent.putExtra("iconPath", iconPath);
                intent.putExtra("color", color);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
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
}
