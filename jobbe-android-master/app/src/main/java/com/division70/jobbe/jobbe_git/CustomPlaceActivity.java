package com.division70.jobbe.jobbe_git;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
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
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.parse.ParseGeoPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by giorgio on 04/11/15.
 */
public class CustomPlaceActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, LocationListener {

    private static GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private GoogleApiClient mGoogleApiClient;
    private GoogleApiClient mGoogleGPSApiClient;
    private LocationRequest mLocationRequest;

    private EditText newPlace;
    private EditText newAddress;
    private Button endButton;
    private ListView list;
    private ListView googleList;

    private boolean customPositionEnabled = false; //Default
    private boolean isGPSEnabled = false; //Default
    private boolean isPositionChosen = false; //Default
    private double latitude;
    private double longitude;
    private double gpsLat;
    private double gpsLng;
    private ArrayList<String> listItems;
    private RelativeLayout viaLayout;
    private boolean newPlaceBoolean = true; //Default
    private boolean newAddressBoolean = true; //Default

    private String newAddressToReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customplace_layout);

        sendScreenAnalytics();

        this.init();

        //SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        //mapFragment.getMapAsync(this);
        setUpActionBar();
    }

    private void setUpActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        CustomActionbar customActionbar = new CustomActionbar(this);
        customActionbar.setTitle("Dove");
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
        t.setScreenName("CLIENTE - CREA RICHIESTA - SELEZIONE POSIZIONE");
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onPause() {
        super.onPause();
        JobbeApplication.activityPaused();
        mMap = null;
    }

    @Override
    protected void onResume(){
        super.onResume();
        JobbeApplication.activityResumed();
        Utils.deleteNotifications(getApplicationContext());
        setUpMap(); //Initialize mMap
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void init(){
        this.listItems = new ArrayList<>();

        this.list = (ListView) findViewById(R.id.listView);
        this.googleList = (ListView) findViewById(R.id.google_list);
        this.newPlace = (EditText) findViewById(R.id.newplace_txt);
        this.newAddress = (EditText) findViewById(R.id.newaddress_txt);
        this.endButton = (Button) findViewById(R.id.fatto_button);
        this.viaLayout = (RelativeLayout) findViewById(R.id.via_layout);

        this.customPositionEnabled = getIntent().getBooleanExtra("customPosition", false);
        this.isGPSEnabled = getIntent().getBooleanExtra("isPositionSet", false);
        this.latitude = getIntent().getDoubleExtra("latitude", 0);
        this.longitude = getIntent().getDoubleExtra("longitude", 0);

        setUpPlaceClient();
        renderList();
        setUpList();
        setListeners();
        setUpMap(); //Initialize mMap
    }

    private void setUpPlaceClient(){
        this.mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, 0, this)
                .build();
    }

    private void setUpList(){
        this.list.post(new Runnable() {
            @Override
            public void run() {
                if (customPositionEnabled) {
                    switchListItem(1);
                    try {
                        String address = new GoogleMapsGeocodingRequest().getAddressForMapFromCoords(new LatLng(latitude, longitude));
                        if (address != null && address.split(" - ").length > 1) {
                            String[] addressObj = address.split(" - ");
                            String via = addressObj[0];
                            String paese = addressObj[1];

                            newPlaceBoolean = false;
                            newAddress.setText(via);
                            newAddress.setSelection(newAddress.getText().length());
                            newPlace.setText(paese);
                            newPlace.setSelection(newPlace.getText().length());
                            newPlaceBoolean = true;
                            isPositionChosen = true;
                            hideGoogleList();
                        }
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (isGPSEnabled && (latitude != 0 && longitude != 0)) { //Set enabled the first row
                    switchListItem(0);
                    isPositionChosen = true;
                } else if (!isGPSEnabled) {
                    switchListItem(1);
                }
            }
        });
    }

    private void renderGoogleList(ArrayList<String> placesList){
        if(placesList.size() > 0)
            enableGoogleList(placesList);
        else
            hideGoogleList();

    }

    private void enableGoogleList(ArrayList<String> placesList){
        ArrayAdapter adapter =
                new ArrayAdapter<>(this, R.layout.zone_listview_layout, R.id.label, placesList);
        this.googleList.setAdapter(adapter);
        showGoogleList(placesList.size());
        adapter.notifyDataSetChanged();
    }

    private void disableGoogleList(){
        this.googleList.setVisibility(View.INVISIBLE);
        this.viaLayout.setVisibility(View.VISIBLE);
    }

    private void renderList(){
        this.listItems.add("Posizione attuale");
        this.listItems.add("Altro indirizzo");
        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.rowlayout, R.id.label, this.listItems);
        this.list.setAdapter(adapter);
        //this.list.getAdapter().getView(0,null,null).setEnabled(false);
    }

    class DoneOnEditorActionListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                String address = newAddress.getText().toString() + "," + newPlace.getText().toString();

                HashMap<String, Object> location =
                        null;
                try {
                    location = new GoogleMapsGeocodingRequest().getLatLngFromPlace(address.replace(" ", "%20"));
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(location == null){
                    Toast.makeText(getApplicationContext(), "L'indirizzo inserito non è valido",
                            Toast.LENGTH_LONG).show();
                }
                else{
                    addPosToMap(mMap, new ParseGeoPoint((double) location.get("latitude"), (double) location.get("longitude")));
                }

                return true;
            }
            return false;
        }
    }

    private void setListeners() {

        this.endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkPositionBeforeEnd()) {
                } else {
                    if (getViewByPosition(0, list).isEnabled()) {
                        Intent intent = new Intent();
                        intent.putExtra("latitude", latitude);
                        intent.putExtra("longitude", longitude);
                        intent.putExtra("posizione", "Posizione attuale");
                        intent.putExtra("customAddress", false);
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    } else {
                        String address = "";
                        if (newAddress.getText().toString().isEmpty())
                            address = newPlace.getText().toString();
                        else
                            address = newAddress.getText().toString() + "," + newPlace.getText().toString();
                        try {
                            HashMap<String, Object> location =
                                    new GoogleMapsGeocodingRequest().getLatLngFromPlace(address.replace(" ", "%20"));

                            if(location == null){
                                Toast.makeText(getApplicationContext(), "L'indirizzo inserito non è valido",
                                        Toast.LENGTH_LONG).show();
                            }
                            else {
                                Intent intent = new Intent();
                                intent.putExtra("latitude", (double) location.get("latitude"));
                                intent.putExtra("longitude", (double) location.get("longitude"));
                                intent.putExtra("posizione", location.get("fullAddress").toString());
                                intent.putExtra("customAddress", true);
                                setResult(Activity.RESULT_OK, intent);
                                finish();
                            }
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        this.newPlace.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                CustomPlaceActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED);
                return false;
            }
        });

        this.newPlace.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    switchListItem(1);
                }
            }
        });

        this.newAddress.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    switchListItem(1);
                }
            }
        });

        this.newPlace.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    googleList.setVisibility(View.INVISIBLE);
                    if (list.getChildAt(1).isEnabled())
                        isPositionChosen = false;
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().isEmpty()) {
                    googleList.setVisibility(View.INVISIBLE);
                    if (list.getChildAt(1).isEnabled())
                        isPositionChosen = false;
                } else if (newPlaceBoolean && s.length() > 2) {
                    displayPredictiveResults(s.toString()); //Displaying places suggestions
                } else {
                    hideGoogleList(); //Hide suggestions
                }
            }
        });

        this.newAddress.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    if (newPlace.getText().toString().isEmpty() && list.getChildAt(1).isEnabled())
                        isPositionChosen = false;
                }
            }
        });

        this.newAddress.setOnEditorActionListener(new DoneOnEditorActionListener());

        this.googleList.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setLocation(position);
            }
        });

        this.list.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isGPSEnabled) {
                    checkRow(view, position);
                } else {
                    if (position != 0) {
                        checkRow(view, position);
                    } else
                        callGPS();
                }
            }
        });
    }

    private boolean checkPositionBeforeEnd(){
        /*if(!this.isPositionChosen){
            if(!newPlace.getText().toString().isEmpty())
                Toast.makeText(getApplicationContext(), "L'indirizzo inserito non è valido",
                        Toast.LENGTH_LONG).show();
            else
                Toast.makeText(getApplicationContext(), "Seleziona una posizione",
                        Toast.LENGTH_LONG).show();
            return false;
        }*/


        if(list.getChildAt(0).isEnabled()){
            if(!this.isPositionChosen){ //Should not be here!
                Toast.makeText(getApplicationContext(), "Errore in fase di localizzazione",
                        Toast.LENGTH_LONG).show();
                return false;
            }
        }

        else if(list.getChildAt(1).isEnabled()){
            if(newPlace.getText().toString().isEmpty() || newAddress.getText().toString().isEmpty()){
                Toast.makeText(getApplicationContext(), "Completa tutti i campi dell'indirizzo",
                        Toast.LENGTH_LONG).show();
                return false;
            }
        }

        return true; //Everything ok
    }

    private void setLocation(int position){
        this.newPlaceBoolean = false;
        this.newPlace.setText(this.googleList.getItemAtPosition(position).toString());
        this.newPlace.setSelection(this.newPlace.getText().length());
        this.newPlaceBoolean = true;

        hideGoogleList();
        this.newAddress.requestFocus();

        try {
            HashMap<String, Object> newPlace = new GoogleMapsGeocodingRequest().getLatLngFromPlace(this.newPlace.getText().toString());
            this.latitude = (double) newPlace.get("latitude");
            this.longitude = (double) newPlace.get("longitude");
            addPosToMap(this.mMap, new ParseGeoPoint(this.latitude, this.longitude));
            isPositionChosen = true;
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void showGoogleList(int itemsNumber){
        this.googleList.setVisibility(View.VISIBLE);
        this.viaLayout.setVisibility(View.GONE);
        this.endButton.setVisibility(View.GONE);
        int height = (int) getResources().getDimension(R.dimen.list_height);

        this.googleList.getLayoutParams().height = itemsNumber * height;

        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
        scrollView.smoothScrollTo(0, scrollView.getBottom());
    }

    private void hideGoogleList(){
        this.googleList.setVisibility(View.GONE);
        this.viaLayout.setVisibility(View.VISIBLE);
        this.endButton.setVisibility(View.VISIBLE);
    }

    private void checkRow(View view, int position){
        if(this.list.getChildAt(0) == view)
            callGPS();
        else
            switchListItem(position);
    }

    private void checkGPSonStart(){
        if (getViewByPosition(0, this.list).isEnabled()) {
            this.gpsLat = getIntent().getDoubleExtra("gpsLat", 0);
            this.gpsLng = getIntent().getDoubleExtra("gpsLng", 0);
            if(new LatLng(this.gpsLat, this.gpsLng) != new LatLng(this.latitude, this.longitude)) {
                addPosToMap(this.mMap, new ParseGeoPoint(this.gpsLat, this.gpsLng));
                this.latitude = this.gpsLat;
                this.longitude = this.gpsLng;
            }
        }
    }

    private void enableNewPlaceInputFields(){
        this.newPlace.setEnabled(true);
        this.newAddress.setEnabled(true);
    }

    private void disableNewPlaceInputFields(){
        this.newPlace.setText("");
        this.newAddress.setText("");
        this.newPlace.setEnabled(false);
        this.newAddress.setEnabled(false);
    }

    private void listPainter(View view, boolean enabled){
        TextView text = (TextView) view.findViewById(R.id.label);
        ImageView image = (ImageView) view.findViewById(R.id.icon);
        if(enabled) {
            text.setTextColor(Color.BLACK);
            image.setImageResource(R.drawable.tick);
            view.setEnabled(true);
        }
        else {
            text.setTextColor(Color.GRAY);
            image.setImageResource(R.drawable.notick);
            view.setEnabled(false);
        }
    }

    private void switchListItem(int position){
        if(position == 0){
            enableRow(getViewByPosition(0, this.list));
            disableRow(getViewByPosition(1, this.list));
            this.newPlace.setText("");
            this.newAddress.setText("");
            hideGoogleList();
        }
        else if(position == 1){
            enableRow(getViewByPosition(1, this.list));
            disableRow(getViewByPosition(0, this.list));
        }
    }

    private void enableRow(View view) {
        listPainter(view, true);
    }

    private void disableRow(View view){
        listPainter(view, false);
    }

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
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
        this.isGPSEnabled = getIntent().getBooleanExtra("isPositionSet", false);
        this.latitude = getIntent().getDoubleExtra("latitude", 0);
        this.longitude = getIntent().getDoubleExtra("longitude", 0);

        ParseGeoPoint center = new ParseGeoPoint(this.latitude, this.longitude);
        //Map centered in user's location / (0,0)
        //Marking anyway
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

    private void displayPredictiveResults(String query)
    {
        //Filter: https://developers.google.com/places/supported_types#table3
        /*List<Integer> filterTypes = new ArrayList<Integer>();
        filterTypes.add(Place.TYPE_STREET_ADDRESS);
        AutocompleteFilter filter = AutocompleteFilter.create(filterTypes);
        AutocompleteFilter.create(Arrays.asList(Place.TYPE_LOCALITY));*/

        AutocompleteFilter filter = null;

        VisibleRegion visibleRegion = mMap.getProjection().getVisibleRegion();
        LatLngBounds bounds = visibleRegion.latLngBounds;

        Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient, query, bounds, filter)
                .setResultCallback(
                        new ResultCallback<AutocompletePredictionBuffer>() {
                            @Override
                            public void onResult(AutocompletePredictionBuffer buffer) {

                                /*Log.i("StatusCODE", buffer.getStatus().getStatusCode() + "");
                                Log.i("StatusMESSAGE: ", buffer.getStatus().getStatusMessage());
                                if (buffer.getStatus().getResolution() != null)
                                    Log.i("StatusRESOLUTION", buffer.getStatus().getResolution().toString());
                                    */

                                if (buffer == null) {
                                    return;
                                }

                                Log.i("Status_code", buffer.getStatus().getStatusCode() + "");
                                Log.i("Status_message", buffer.getStatus().getStatusMessage() + "");

                                if (buffer.getStatus().isSuccess()) {
                                    renderGoogleList(getPlacesList(buffer));

                                    //placeGetter(buffer.get(0).getPlaceId());
                                }

                                //Prevent memory leak by releasing buffer
                                buffer.release();
                            }
                        }, 60, TimeUnit.SECONDS);
    }

    private LatLngBounds createLatLngBounds(){
        return new LatLngBounds(new LatLng(latitude, longitude), new LatLng(latitude, longitude));
    }

    private ArrayList<String> getPlacesList(AutocompletePredictionBuffer buffer){
        ArrayList<String> tmp = new ArrayList<>();
        for(AutocompletePrediction place : buffer) {
            //placeGetter(place.getPlaceId());
            if(place.getFullText(null).toString().split(",").length == 3) {
                if(!tmp.contains(place.getFullText(null).toString())) {
                    tmp.add(place.getFullText(null).toString());
                }
            }
        }

        return tmp;
    }

    private String createFormattedAddress(Place place){
        int commaPos = place.getAddress().toString().indexOf(",");
        return place.getName() + ", " + place.getAddress().toString().subSequence(commaPos, commaPos + 2);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if( mGoogleApiClient != null )
            mGoogleApiClient.connect();

        if( mGoogleGPSApiClient != null )
            mGoogleGPSApiClient.connect();
    }

    @Override
    protected void onStop() {
        if( mGoogleApiClient != null && mGoogleApiClient.isConnected() ) {
            mGoogleApiClient.disconnect();
        }

        if( mGoogleGPSApiClient != null && mGoogleGPSApiClient.isConnected() ) {
            mGoogleGPSApiClient.disconnect();
        }

        super.onStop();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("FAIL", "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());
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
                LocationServices.SettingsApi.checkLocationSettings(mGoogleGPSApiClient, builder.build());
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
                        if (ActivityCompat.checkSelfPermission(CustomPlaceActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(CustomPlaceActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            ActivityCompat.requestPermissions(CustomPlaceActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 23);

                            return;
                        }
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleGPSApiClient, mLocationRequest, CustomPlaceActivity.this);
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i("GPS", "could be connected");
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(CustomPlaceActivity.this, 3456);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }

                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i("GPS", "not connected");
                        Toast.makeText(getApplicationContext(), "Localizzazione GPS non attiva",
                                Toast.LENGTH_LONG).show();

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
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        addPosToMap(this.mMap, new ParseGeoPoint(this.latitude, this.longitude));
        switchListItem(0);
        isPositionChosen = true;
    }

    protected void callGPS(){
        buildGoogleApiClient();
        mGoogleGPSApiClient.connect();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleGPSApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 3456) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Log.i("GPS_Request","OK");
                    if (ActivityCompat.checkSelfPermission(CustomPlaceActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(CustomPlaceActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        ActivityCompat.requestPermissions(CustomPlaceActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 23);

                        return;
                    }
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleGPSApiClient, mLocationRequest, CustomPlaceActivity.this);
                    break;
            }
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
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, CustomPlaceActivity.this);

                } else {
                    return;
                    // permission denied, boo! Disable the functionality that depends on this permission.
                }
                break;

        }
    }

}
