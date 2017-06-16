package com.division70.jobbe.jobbe_git;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
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
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.LogInCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by giorgio on 22/10/15.
 */
public class ConfirmationCodeActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener {

    private String verificationCode;
    private EditText codeText;
    private Button confirmButton;
    private TextView timerTextView;
    private TextView sendagainTextview;
    private TextView phoneRecapView;
    private TextView modifyPhoneTextView;
    private RelativeLayout spinnerLayout;

    private String phoneNumber;
    private String displayName;
    private String name;
    private String cognome;
    private String email;
    private String anno;
    private String gender = "male"; //Default value
    private String type; //Client SignUp activity
    private boolean isSupplier;
    private ParseFile imageFile;
    private String jobName;
    private String suppPIVA;
    private String suppCF;
    private String suppDitta;
    private String suppDesc;
    ArrayList<String> cityList;

    private CountDownTimer timer;
    private int secs;
    private int mins;
    private boolean ImageFileExists = false;

    private GoogleApiClient mGoogleApiClient;
    private boolean hasCoords;
    private double latitude;
    private double longitude;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirmationcode_layout);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("jobbe_sms_received"));

        sendScreenAnalytics();

        this.spinnerLayout = (RelativeLayout) findViewById(R.id.loadingSpinnerLayout);

        //Retrieving the client's info from previous activity
        this.phoneNumber = getIntent().getStringExtra("phoneNumber");
        this.verificationCode = getIntent().getStringExtra("verificationCode");
        this.displayName = getIntent().getStringExtra("displayName");
        this.email = getIntent().getStringExtra("email");
        this.gender = getIntent().getStringExtra("gender");
        this.anno = getIntent().getStringExtra("anno");
        this.type = getIntent().getStringExtra("type");
        this.name = this.displayName.split(" ")[0];
        this.cognome = this.displayName.split(" ")[1];
    //    this.imageFile = new ParseFile(new File(Environment.getExternalStorageDirectory() + getResources().getString(R.string.pic_path)));

        File file = new File(Environment.getExternalStorageDirectory() + getResources().getString(R.string.pic_path));
        if(file.exists()){
            ImageFileExists = true;
            this.imageFile = new ParseFile("userpic.jpg", ImageUtils.compressImage(Environment.getExternalStorageDirectory() + getResources().getString(R.string.pic_path)));

        }else{
            ImageFileExists = false;
        }






        this.isSupplier = (this.type.equals("client")) ? false : true;
        Log.i("isSupplier_CCBeginning", isSupplier + "");

        this.jobName = getIntent().getStringExtra("job");
        this.suppPIVA = getIntent().getStringExtra("suppPIVA");
        this.suppCF = getIntent().getStringExtra("suppCF");
        this.suppDitta = getIntent().getStringExtra("suppDitta");
        this.suppDesc = getIntent().getStringExtra("suppDesc");
        this.cityList = getIntent().getStringArrayListExtra("cityList");

        this.hasCoords = getIntent().getBooleanExtra("hasCoords", false);
        this.latitude = getIntent().getDoubleExtra("latitude", 0.0);
        this.longitude = getIntent().getDoubleExtra("longitude", 0.0);

        Log.i("VER_CODE", verificationCode);

        timerSetup();

        this.codeText = (EditText) findViewById(R.id.code_field);
        this.codeText.requestFocus();
        this.confirmButton = (Button) findViewById(R.id.confirm);
        this.confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (codeText.getText().toString().isEmpty())
                    Toast.makeText(getApplicationContext(), getString(R.string.empty_code_number),
                            Toast.LENGTH_LONG).show();
                else if (!codeText.getText().toString().equals(verificationCode))
                    Toast.makeText(getApplicationContext(), getString(R.string.wrong_code_number),
                            Toast.LENGTH_LONG).show();
                else {
                    spinnerLayout.setVisibility(View.VISIBLE);
                    new saveAsync().execute();

                    /*
                    if (type.equals("client")) {
                        //Parto con procedura geolocalizzazione
                        Intent intent = new Intent(ConfirmationCodeActivity.this, GpsAlertActivity.class);
                        intent.putExtra("isClient", true);
                        startActivityForResult(intent, 6943);

                    } else {
                        // Il fornitore ha già fatto la geolocalizzazione, faccio il createUser
                        Map<String, Object> userInfo = getUserInfo(hasCoords);
                        //Create User
                        ParseCloud.callFunctionInBackground("createUser ", userInfo, new FunctionCallback<HashMap<String, String>>() {
                            @Override
                            public void done(HashMap<String, String> stringStringHashMap, ParseException e) {
                                String usernametxt = phoneNumber;
                                String passwordtxt = verificationCode;
                                login(usernametxt, passwordtxt);
                            }


                        });
                    }
                    */
                }
            }
        });

        sendagainTextview = (TextView) findViewById(R.id.sendagain_textView);
        sendagainTextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCodeAgain();
            }
        });

        this.phoneRecapView = (TextView) findViewById(R.id.phone_textview);
        phoneRecapView.setText("+39 " + phoneNumber);

        modifyPhoneTextView = (TextView) findViewById(R.id.modify_link);
        modifyPhoneTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        setUpActionBar();
    }

    private void setUpActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        CustomActionbar customActionbar = new CustomActionbar(this);
        customActionbar.setTitle("Inserisci codice di conferma");
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

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String code = intent.getStringExtra("user_code");

            if(!code.isEmpty())
                codeText.setText(code);
        }
    };

    private void sendScreenAnalytics(){
        Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
        t.setScreenName("SIGNUP - CODICE VERIFICA");
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void startClientHome() {
        spinnerLayout.setVisibility(View.GONE);
        Intent intent = new Intent(ConfirmationCodeActivity.this, HomeClientActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        ConfirmationCodeActivity.this.finish();
        //spinnerLayout.setVisibility(View.GONE);
    }

    private void startSupplierHome() {
        Intent intent = new Intent(ConfirmationCodeActivity.this, HomeSupplierActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        //spinnerLayout.setVisibility(View.GONE);
    }

    private Map<String, Object> getUserInfo(Boolean hasCoords) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("phoneNumber", this.phoneNumber);
        userInfo.put("verificationCode", this.verificationCode);
        userInfo.put("email", this.email);
        userInfo.put("name", this.name);
        userInfo.put("cognome", this.cognome);
        if (this.gender != null) userInfo.put("gender", this.gender);
        userInfo.put("displayName", this.displayName);
        if (this.anno != null && !this.anno.isEmpty()) userInfo.put("anno", Integer.parseInt(this.anno));
        userInfo.put("type", this.type);
        if (this.suppDesc != null) userInfo.put("desc", this.suppDesc);
        userInfo.put("isSupplier", this.isSupplier);

        if (hasCoords) {
            Log.i("latitude getInfo", this.latitude + " " + latitude);
            Log.i("longitude getInfo", this.longitude + " " + longitude);
            userInfo.put("latitude", this.latitude);
            userInfo.put("longitude", this.longitude);
        }
        return userInfo;
    }

    private void login(String usernametxt, String passwordtxt) {
       // System.out.println("login parse Called !!!!!!!!!11");
        try{

            ParseUser.logInInBackground(usernametxt, passwordtxt,
                    new LogInCallback() {
                        public void done(final ParseUser user, ParseException e) {

                            if (user != null) {

                                //Set user initial location
                               // System.out.println("logInInBackground   done  ");
                                ParseGeoPoint initialPosition;
                                //System.out.println("logInInBackground   done  1");
                                double supplatitude = getIntent().getDoubleExtra("suppLatitude", 0);
                                double supplongitude = getIntent().getDoubleExtra("suppLongitude", 0);
                                if(supplatitude!=0){
                                    latitude = supplatitude;
                                    longitude = supplongitude;
                                }
                                //System.out.println(" Lat == "+latitude+"    lng  ==  "+longitude);
                                initialPosition = new ParseGeoPoint(latitude, longitude);

                                if (suppCF != null) user.put("codFiscale", suppCF);
                                if (suppDitta != null) user.put("ditta", suppDitta);

                                user.put("locationCords", initialPosition);

                                //Set user profile picture
                                if(ImageFileExists)
                                user.put("profilePhoto", imageFile);
                                try {
                      //              user.save();
                                    user.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            setParseInstallation();
                                      //      System.out.println("Just After user setParseInstallation !!!");
                                            Log.i("USERTYPE", type);

                                            if (type.equals("client")) {
                                                startClientHome();
                                            } else {
                                                //Check supplier request
                                                //Create Job
                                                ArrayList<ParseObject> zones = new ArrayList<>();
                                                ArrayList<String> jobArray = new ArrayList<>();


                                                for (String city : cityList) {
                                                    try {
                                                        zones.add(
                                                                (ParseObject) ParseQuery.getQuery("Zone")
                                                                        .whereEqualTo("active", true)
                                                                        .whereEqualTo("nome", city).find().get(0));
                                                        Log.i("CITY", zones.get(0).get("nome").toString());
                                                    } catch (ParseException exc) {
                                                        ParseErrorHandler.handleParseError(exc, getApplicationContext());
                                                        exc.printStackTrace();
                                                    }
                                                }

                                                for (ParseObject zone : zones) {
                                                    ParseObject job = new ParseObject("Jobs");
                                                    job.put("name", jobName);
                                                    job.put("supplierId", user);
                                                    if (!suppPIVA.equals("")) {
                                                        job.put("piva", suppPIVA);
                                                        job.put("supplierType", "professionist");
                                                    } else
                                                        job.put("supplierType", "hobby");
                                                    job.put("zoneId", zone);

                                                    Map<String, Object> empty = new HashMap<String, Object>() {
                                                    };
                                                    try {
                                                        HashMap<String, Object> result = ParseCloud.callFunction("getJobs", empty); //Synchronous call in order to wait until categories retrieval

                                                        String jobTofind = jobName;
                                                        ArrayList<ParseObject> categorylist = (ArrayList<ParseObject>) result.get("jobCategory");
                                                        ArrayList<ParseObject> joblist = (ArrayList<ParseObject>) result.get("jobList");
                                                        for (ParseObject category : categorylist) {
                                                            ArrayList<HashMap<String, String>> jobs = (ArrayList<HashMap<String, String>>) category.get("jobs");
                                                            for (HashMap<String, String> x : jobs) {
                                                                String jobName = x.get("name");
                                                                if (jobName.equals(jobTofind)) {
                                                                    Log.i("JOB FOUND", "Job name: " + x.get("name") + " Job Id: " + x.get("id") + " Category Name: " + (String) category.get("name") + " Category Id: " + category.getObjectId());
                                                                    jobArray.add(x.get("id"));
                                                                    job.put("categoryListId", category);
                                                                    for (ParseObject y : joblist) {
                                                                        if (y.getObjectId().equals(x.get("id"))) {
                                                                            job.put("jobListId", y);
                                                                        }
                                                                    }

                                                                    job.save();

                                                                    break;
                                                                }
                                                            }
                                                        }
                                                    } catch (ParseException e1) {
                                                        Log.e("ParseException", e1.toString());
                                                    }
                                                }

                                                matchNewSupplierRequestsAPIcall(zones, jobArray);

                                                try {
                                                    user.fetch();
                                                } catch (ParseException e1) {
                                                    e1.printStackTrace();
                                                }

                                                startSupplierHome();

                                            }
                                        }
                                    });
                               //     System.out.println("Just After user.save function !!!");
                                } catch (Exception ex) {
                                    e.printStackTrace();
                                }


                            /*
                            Toast.makeText(getApplicationContext(),
                                    "Login completato",
                                    Toast.LENGTH_LONG).show();
                            finish();
                            */
                            } else {
                                Toast.makeText(
                                        getApplicationContext(),
                                        "Questo numero di telefono è gia stato registrato, per favore utilizza il login per accedere all'applicazione",
                                        Toast.LENGTH_LONG).show();
                                //spinnerLayout.setVisibility(View.GONE);
                            }
                        }
                    });
        }catch(Exception e){

        }

    }

    private String codeGenerator() {
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            numbers.add(i);
        }

        Collections.shuffle(numbers);

        String result = "";
        for (int i = 0; i < 4; i++) {
            result += numbers.get(i).toString();
        }

        return result;
    }

    private void timerSetup() {
        //Timer setup (3 mins)
        timerTextView = (TextView) findViewById(R.id.timer_textView);
        this.timer = new CountDownTimer(180000, 1000) {
            @Override
            public void onTick(long l) {
                //Cambia label timer
                secs = (int) l / 1000;
                mins = secs / 60;
                secs = secs % 60;
                timerTextView.setText(Integer.toString(mins) + ":" + String.format("%02d", secs));
            }

            @Override
            public void onFinish() {
                //Annulla codice precedente, possibile invio nuovo codice
                timerTextView.setText("Terminato");
                verificationCode = null;

                showSendAgainLink();

            }
        };
        this.timer.start();
    }

    private void showSendAgainLink(){
        this.timerTextView.setVisibility(View.GONE);
        this.sendagainTextview.setVisibility(View.VISIBLE);
    }

    private void hideSendAgainLink(){
        this.timerTextView.setVisibility(View.VISIBLE);
        this.sendagainTextview.setVisibility(View.GONE);
    }

    private void sendCodeAgain() {
        verificationCode = codeGenerator();
        Map<String, String> phoneInfo = new HashMap<>();
        phoneInfo.put("phoneNumber", phoneNumber);
        phoneInfo.put("verificationCode", verificationCode);
        ParseCloud.callFunctionInBackground("sendSmsVerification ", phoneInfo, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e != null || o == null) {
                    Toast.makeText(getApplicationContext(), "Il numero inserito risulta già presente, fai il login per accedere al tuo profilo Jobbe", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "La richiesta di un nuovo codice è stata inviata", Toast.LENGTH_LONG).show();
                    Log.i("VER_CODE", verificationCode);
                    timer.cancel();
                    timer.start();
                    hideSendAgainLink();
                }
            }
        });
        //Toast.makeText(getApplicationContext(), "La richiesta di un nuovo codice è stata inviata", Toast.LENGTH_LONG).show();
        //Log.i("VER_CODE", verificationCode);
        //timer.cancel();
        //timer.start();
        //hideSendAgainLink();
    }

    private Map<String, String> getPhoneInfo(String phoneNumber) {
        Map<String, String> phoneInfo = new HashMap<>();
        phoneInfo.put("phoneNumber", phoneNumber);
        phoneInfo.put("verificationCode", codeGenerator());

        return phoneInfo;
    }

    //private void getPosition(){
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    //}


    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
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
                        Log.i("LocationStatus", "SUCCESS");
                        if (ActivityCompat.checkSelfPermission(ConfirmationCodeActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ConfirmationCodeActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            ActivityCompat.requestPermissions(ConfirmationCodeActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 23);

                            return;
                        }
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, ConfirmationCodeActivity.this);
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        Log.i("LocationStatus", "RESOLUTION_REQUIRED");
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    ConfirmationCodeActivity.this, 3456);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        Log.i("LocationStatus", "CHANGE_UNAVAILABLE");

                        Map<String, Object> userInfo = getUserInfo(false);
                        ParseCloud.callFunctionInBackground("createUser ", userInfo, new FunctionCallback<Object>() {
                            @Override
                            public void done(Object o, ParseException e) {
                                String usernametxt = phoneNumber;
                                String passwordtxt = verificationCode;
                                login(usernametxt, passwordtxt);
                            }

                        });
                        break;
                }
            }
        });

        //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i("", "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i("", "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 3456) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Log.i("GPS_Request", "OK");
                    if (ActivityCompat.checkSelfPermission(ConfirmationCodeActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ConfirmationCodeActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        ActivityCompat.requestPermissions(ConfirmationCodeActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 23);

                        return;
                    }
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, ConfirmationCodeActivity.this);
                    break;
                case Activity.RESULT_CANCELED:
                    Log.i("GPS_Request", "Annullato");
                    Map<String, Object> userInfo = getUserInfo(false);
                    ParseCloud.callFunctionInBackground("createUser ", userInfo, new FunctionCallback<Object>() {
                        @Override
                        public void done(Object o, ParseException e) {
                            String usernametxt = phoneNumber;
                            String passwordtxt = verificationCode;
                            login(usernametxt, passwordtxt);
                            spinnerLayout.setVisibility(View.GONE);

                        }

                    });
                    break;
            }
        }

        else if (requestCode == 6943) {
            switch (resultCode) {
                case Activity.RESULT_OK:
         //           System.out.println("callGPS() !!!!!!!!!");
                    callGPS();
                    break;
                case Activity.RESULT_CANCELED:
                    spinnerLayout.setVisibility(View.GONE);
                    break;
            }
        }
    }

    protected void callGPS() {
        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }


    @Override
    public void onLocationChanged(Location l) {
     //   Log.i("Coords", Double.toString(l.getLatitude()) + ", " + Double.toString(l.getLongitude()));
        latitude = l.getLatitude();
        longitude = l.getLongitude();


        Map<String, Object> userInfo = getUserInfo(true);

        //Create User
        ParseCloud.callFunctionInBackground("createUser ", userInfo, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                String usernametxt = phoneNumber;
                String passwordtxt = verificationCode;
                login(usernametxt, passwordtxt);
            }
        });
    }

    private void setParseInstallation() {

        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        ParseUser user = ParseUser.getCurrentUser();
        if (user != null){
            installation.put("userId", user);
        }
        installation.saveInBackground();
    }

    private void matchNewSupplierRequestsAPIcall(ArrayList<ParseObject> zones, ArrayList<String> jobsId) {
        ArrayList<String> zonesId = new ArrayList<>();
        //ArrayList<String> jobsId = new ArrayList<>();
        Map<String, Object> params = new HashMap<String, Object>();
        for (ParseObject z : zones)
            zonesId.add(z.getObjectId());
        //for (ParseObject j : jobs)
        //    jobsId.add(j.getObjectId());

        params.put("jobs", jobsId);
        params.put("zones", zonesId);
        try{
            params.put("userId", ParseUser.getCurrentUser().getObjectId());
            ParseCloud.callFunction("matchNewSupplierRequests", params);
        } catch (ParseException e) {
            ParseErrorHandler.handleParseError(e, getApplicationContext());
            e.printStackTrace();
        }
    }

    private class saveAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            if (type.equals("client")) {
                //Parto con procedura geolocalizzazione
               // System.out.println("GPS Alert Activity Called !!!!!!!!!11");
                Intent intent = new Intent(ConfirmationCodeActivity.this, GpsAlertActivity.class);
                intent.putExtra("isClient", true);
                startActivityForResult(intent, 6943);

            } else {
               // System.out.println("Else ........      GPS Alert Activity Called !!!!!!!!!11");
                // Il fornitore ha già fatto la geolocalizzazione, faccio il createUser
                Map<String, Object> userInfo = getUserInfo(hasCoords);
                //Create User
                try {
                    ParseCloud.callFunction("createUser ", userInfo);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String usernametxt = phoneNumber;
                String passwordtxt = verificationCode;
                login(usernametxt, passwordtxt);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            //spinnerLayout.setVisibility(View.GONE);
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
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, ConfirmationCodeActivity.this);

                } else {
                    return;
                    // permission denied, boo! Disable the functionality that depends on this permission.
                }
                break;

        }
    }

}
