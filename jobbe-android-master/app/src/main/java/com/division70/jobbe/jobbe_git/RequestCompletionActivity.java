package com.division70.jobbe.jobbe_git;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.MenuItem;
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
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.venmo.view.TooltipView;

/**
 * Created by giorgio on 02/11/15.
 */
public class RequestCompletionActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,LocationListener {

    private TextView requestJob;
    private EditText requestTitle;
    private ActionEditText requestDescription;
    private RelativeLayout dateButton;
    private RelativeLayout placeButton;
    private TextView tempisticaLabel;
    private TextView posizioneLabel;
    private Button endRequestButton;
    private RoundedImageView jobPic;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private double latitude = 0; //Default
    private double longitude = 0; //Default

    ParseGeoPoint initialPosition = null; //Default, assuming the GPS is NOT enabled or the position has not been choosen
    public boolean isPositionSet = false; //Default, just to make sure that the user fills it
    private boolean customPositionEnabled = false; //Only CustomPlaceActivity can set it to true

    private double gpsLat = 0; //Default
    private double gpsLng = 0; //Default

    private static String defTime = "Il prima possibile"; //Default time option
    private String jobIconPath;
    private TooltipView tooltipWhen;
    private TooltipView tooltipTitle;
    private TooltipView tooltipDesc;

    public static String getDefTime(){
        return defTime;
    }

    private boolean firstTimeWhen = false;
    private boolean firstTimeTitle = false;
    private boolean firstTimeDesc = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.requestcompletion_layout);

        sendScreenAnalytics();

        this.init();
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
        t.setScreenName("CLIENTE - CREA RICHIESTA - FORM");
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
        this.defTime = "Il prima possibile"; //Default time option
        //checkUserRegisteredLocation(); //Check user's registered location
        callGPS(); //Check whether current position is available or not
        this.requestJob = (TextView) findViewById(R.id.request_job);
        this.requestTitle = (EditText) findViewById(R.id.request_title);
        this.requestDescription = (ActionEditText) findViewById(R.id.request_description);
        this.requestJob.setText(getIntent().getStringExtra("selectedJob"));
        this.dateButton = (RelativeLayout) findViewById(R.id.tim_layout);
        this.placeButton = (RelativeLayout) findViewById(R.id.loc_layout);
        this.endRequestButton = (Button) findViewById(R.id.end_request);
        this.posizioneLabel = (TextView) findViewById(R.id.posizione_label);
        this.tempisticaLabel = (TextView) findViewById(R.id.tempistica_label);
        this.jobPic = (RoundedImageView) findViewById(R.id.job_pic);
        this.tempisticaLabel.setText(this.defTime);
        this.tooltipWhen = (TooltipView) findViewById(R.id.tooltipWhen);
        this.tooltipTitle = (TooltipView) findViewById(R.id.tooltipTitle);
        this.tooltipDesc = (TooltipView) findViewById(R.id.tooltipDesc);

        requestDescription.setHorizontallyScrolling(false);
        requestDescription.setLines(1);
        requestDescription.setMaxLines(6);


        this.jobIconPath = getIntent().getStringExtra("jobIconPath");
        int resourceId = getResources().getIdentifier(jobIconPath, "drawable", "com.division70.jobbe.jobbe_git");
        this.jobPic.setImageResource(resourceId);

        if(checkIfFirstTime("When")){
            firstTimeWhen = true;
            enableTooltip("When");
        }

        if(checkIfFirstTime("Title")){
            firstTimeTitle = true;
            enableTooltip("Title");
        }

        if(checkIfFirstTime("Desc")){
            firstTimeDesc = true;
            enableTooltip("Desc");
        }

        setListeners();
    }

    private void checkUserRegisteredLocation(){
        ParseUser currentUser = ParseUser.getCurrentUser();
        currentUser.getUsername();

        if(currentUser != null){
            ParseGeoPoint geoPos = (ParseGeoPoint) currentUser.get("locationCords");
            if(geoPos != null) {
                isPositionSet = true;
                latitude = geoPos.getLatitude();
                longitude = geoPos.getLongitude();
            }
        }
    }

    private void setListeners(){

        this.dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firstTimeWhen){
                    disableTooltip("When");
                    unsetFirstTime("When");
                }
                Intent intent = new Intent(RequestCompletionActivity.this, RequestTimingActivity.class);
                intent.putExtra("defTime", defTime);
                startActivityForResult(intent, 7777);
            }
        });

        this.placeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPositionSet) { //Check if the user set a default location at signup
                    //checkUserRegisteredLocation(); //Check user's registered location
                }

                Intent intent = new Intent(RequestCompletionActivity.this, CustomPlaceActivity.class);
                intent.putExtra("customPosition", customPositionEnabled);
                intent.putExtra("isPositionSet", isPositionSet);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                intent.putExtra("gpsLat", gpsLat);
                intent.putExtra("gpsLng", gpsLng);
                startActivityForResult(intent, 9999);
            }
        });

        this.endRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isInputCorrect()) {
                    //Toast.makeText(getApplicationContext(), "Completare tutti i campi della richiesta",
                    //Toast.LENGTH_LONG).show();
                } else {
                    unsetFirstTime("When");
                    unsetFirstTime("Title");
                    unsetFirstTime("Desc");

                    Intent intent = new Intent(RequestCompletionActivity.this, RequestConfirmationActivity.class);
                    intent.putExtra("selectedJob", getIntent().getStringExtra("selectedJob"));
                    intent.putExtra("selectedJob", requestJob.getText().toString());
                    intent.putExtra("reqTitle", requestTitle.getText().toString());
                    intent.putExtra("reqDesc", requestDescription.getText().toString());
                    intent.putExtra("reqTime", defTime);
                    intent.putExtra("latitude", latitude);
                    intent.putExtra("longitude", longitude);
                    intent.putExtra("jobIconPath", jobIconPath);
                    intent.putExtra("category", getIntent().getStringExtra("category"));
                    if (posizioneLabel.getText().toString().equals("Posizione attuale"))
                        intent.putExtra("gpsEnabled", true);
                    else
                        intent.putExtra("gpsEnabled", false);
                    startActivity(intent);
                    overridePendingTransition(R.anim.pushleft_in, R.anim.pushleft_out);
                }
            }
        });

        this.requestTitle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (firstTimeTitle) {
                    firstTimeTitle = true;
                    disableTooltip("Title");
                    unsetFirstTime("Title");
                }
                return false;
            }
        });

        this.requestDescription.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (firstTimeDesc){
                    firstTimeDesc = true;
                    disableTooltip("Desc");
                    unsetFirstTime("Desc");
                }
                return false;
            }
        });

        this.requestDescription.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    if (firstTimeDesc) {
                        firstTimeDesc = true;
                        disableTooltip("Desc");
                        unsetFirstTime("Desc");
                    }
                }
            }
        });
    }

    private boolean isInputCorrect(){

        if((this.latitude == 0 && this.longitude == 0)) {
            Toast.makeText(getApplicationContext(), "Scegli una posizione geografica",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        else if(this.requestTitle.getText().toString().isEmpty()){
            Toast.makeText(getApplicationContext(), "Inserisci un titolo per la tua richiesta",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        else if(this.requestDescription.getText().toString().isEmpty()){
            Toast.makeText(getApplicationContext(), "Inserisci una descrizione per la tua richiesta",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        else if(!new RegexValidator(getApplicationContext()).validate(this.requestDescription.getText().toString()))
            return false;
        //Timing is always set in a way
        return true; //Valid input
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == 7777 && resultCode == Activity.RESULT_OK) {
            boolean customDate = data.getBooleanExtra("customDate", false);

            if(customDate){
                this.defTime = data.getStringExtra("defTime").toString();
                this.tempisticaLabel.setText(this.defTime);
            }
            else {
                this.defTime = data.getStringExtra("defTime").toString();
                this.tempisticaLabel.setText(this.defTime);
            }
        }

        if(requestCode == 9999 && resultCode == Activity.RESULT_OK) {
            //Setting location coordinates
            this.latitude = data.getDoubleExtra("latitude", 0);
            this.longitude = data.getDoubleExtra("longitude", 0);
            this.posizioneLabel.setText(data.getStringExtra("posizione"));
            this.customPositionEnabled = data.getBooleanExtra("customAddress", false); //Enable it here
            this.isPositionSet = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 23:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the task you need to do.
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, RequestCompletionActivity.this);

                } else {
                    return;
                    // permission denied, boo! Disable the functionality that depends on this permission.
                }
                break;

        }
    }
    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setNumUpdates(1);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        builder.setAlwaysShow(true); //this is the key ingredient

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        Log.i("GPS", "connected");
                        if (ActivityCompat.checkSelfPermission(RequestCompletionActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(RequestCompletionActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            ActivityCompat.requestPermissions(RequestCompletionActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 23);

                            return;
                        }
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, RequestCompletionActivity.this);
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i("GPS", "could be connected");
                        //DO NOTHING

                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i("GPS", "not connected");
                        //DO NOTHING

                        break;
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        this.initialPosition = new ParseGeoPoint(location.getLatitude(), location.getLongitude()); //Initialize position

        this.posizioneLabel.setText("Posizione attuale");
        this.isPositionSet = true;
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.gpsLat = location.getLatitude();
        this.gpsLng = location.getLongitude();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    protected void callGPS(){
        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onBackPressed() {
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

    private void enableTooltip(String type) {
        switch (type){
            case "When": tooltipWhen.setVisibility(View.VISIBLE); break;
            case "Title": tooltipTitle.setVisibility(View.VISIBLE); break;
            case "Desc": tooltipDesc.setVisibility(View.VISIBLE); break;
        }

    }

    private void disableTooltip(String type) {
        switch (type){
            case "When": tooltipWhen.setVisibility(View.GONE); break;
            case "Title": tooltipTitle.setVisibility(View.GONE); break;
            case "Desc": tooltipDesc.setVisibility(View.GONE); break;
        }
    }

    private boolean checkIfFirstTime(String type){
        SharedPreferences settings = getPreferences(0);
        boolean first = settings.getBoolean("isFirstTime" + type, true);
        return first;
    }

    private void unsetFirstTime(String type){
        SharedPreferences settings = getPreferences(0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("isFirstTime" + type, false);
        editor.commit();
    }
}
