package com.division70.jobbe.jobbe_git;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;

public class ZoneChooserActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ArrayList<Marker> markers;
    private ArrayList<ParseGeoPoint> locations;
    private ArrayList<Circle> circles;
    private ArrayList<ParseObject> zones;
    private ListView zonesList;

    public static ArrayList<String> cityList;

    //private ListView zoneList;
    private Button confirmZone;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    ParseGeoPoint initialPosition = null; //Default, assuming the GPS is NOT enabled

    private double suppLatitude = 0;
    private double suppLongitude = 0;

    private RelativeLayout spinnerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zonechooser_layout);

        sendScreenAnalytics();

        this.init();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setUpActionBar();
    }

    private void setUpActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        CustomActionbar customActionbar = new CustomActionbar(this);
        customActionbar.setTitle("Zone di intervento");
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

    private void sendScreenAnalytics() {
        Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
        t.setScreenName("SIGNUP - FORNITORE SELEZIONE ZONA");
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onResume() {
        super.onResume();
        JobbeApplication.activityResumed();
        Utils.deleteNotifications(getApplicationContext());
        //this.init();
        //SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        //mapFragment.getMapAsync(this);
    }

/*    private void setUpMap() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setMapListener();
            }
        } else
            //Here if the user went on the next activity and came back
            //Clearing the map
            mMap.clear();
    }*/

    private void setUpMap() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setMapListener();
            } else {
                spinnerLayout.setVisibility(View.GONE);
               /* Toast.makeText(ZoneChooserActivity.this, getString(R.string.alert_play_services_missing), Toast.LENGTH_LONG).show();
                System.out.println(" setUpMap in else condition !!!!!!!!! ");
                ZoneChooserActivity.this.finish();*/
            }
        } else {
//Here if the user went on the next activity and came back
            //Clearing the map
            mMap.clear();

        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap.getUiSettings().setMapToolbarEnabled(false);

        //Show GPS Permission PopUp
        showGPSPopup();

        try {
            //setMainZone(googleMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setMainZone(GoogleMap googleMap) throws Exception {
        if (this.initialPosition != null) {
            ParseObject province = new GoogleMapsGeocodingRequest().getProvinceFromCoords(geoposToLatlng(this.initialPosition), this.zones);
            Log.i("PROV: ", "" + province);

            if (province != null) {
                this.initialPosition = (ParseGeoPoint) province.get("centercords");
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(geoposToLatlng(this.initialPosition)));
                googleMap.moveCamera(CameraUpdateFactory.zoomTo(9));

                //Update List
                int listPos = this.zones.indexOf(province);
                enableRow(getViewByPosition(listPos, this.zonesList), listPos);
            }
        } else {
            setVoidMap(googleMap, (ParseGeoPoint) this.zones.get(0).get("centercords")); //Loading map on the first zone on the list
        }

        spinnerLayout.setVisibility(View.GONE);
    }

    private void setVoidMap(GoogleMap googleMap, ParseGeoPoint geoPos) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(geoposToLatlng(geoPos)));
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(9));
    }

    private void addPosToMap(GoogleMap googleMap, ParseGeoPoint geoPos) {
        this.locations.add(geoPos);
        this.markers.add(googleMap.addMarker(new MarkerOptions()
                .position(geoposToLatlng(geoPos))));
        this.circles.add(googleMap.addCircle(this.newCircleOptions(geoposToLatlng(geoPos))));
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(9));
    }

    private static CircleOptions newCircleOptions(LatLng coords) {
        return new CircleOptions()
                .radius(7000)
                .center(coords)
                .strokeColor(Color.BLUE)
                .strokeWidth(5)
                .fillColor(0x8571BED1);
    }

    private void init() {
        this.spinnerLayout = (RelativeLayout) findViewById(R.id.loadingSpinnerLayout);

        setUpMap(); //Initialize mMap

        this.markers = new ArrayList<>();
        this.circles = new ArrayList<>();
        this.locations = new ArrayList<>();
        this.zones = new ArrayList<>();
        this.confirmZone = (Button) findViewById(R.id.confirm_zone_button);
        this.zonesList = (ListView) findViewById(R.id.zones_list);

        this.cityList = new ArrayList<>();


        try {
            this.zones = (ArrayList<ParseObject>) ParseQuery.getQuery("Zone").whereEqualTo("active", true).find(); //Getting zones
        } catch (ParseException e) {
            ParseErrorHandler.handleParseError(e, getApplicationContext());
            e.printStackTrace();
        }

        renderList();
        setUpList();
        setListeners();
    }

    private void renderList() {
        ArrayList<String> values = new ArrayList<>();
        for (ParseObject zone : this.zones)
            values.add(zone.get("nome").toString());
        CustomArrayAdapterZones adapter = new CustomArrayAdapterZones(this, R.layout.rowlayout, R.id.label, values);
        this.zonesList.setAdapter(adapter);
    }

    private void setUpList() {
        this.zonesList.post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < zones.size(); i++)
                    listPainter(getViewByPosition(i, zonesList), false);
            }
        });
    }

    private void setListeners() {
        //Button listener
        this.confirmZone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cityList.size() == 0)
                    Toast.makeText(getApplicationContext(), "Seleziona almeno una zona",
                            Toast.LENGTH_LONG).show();
                else {
                    Intent intent = new Intent(ZoneChooserActivity.this, ZoneRecapActivity.class);
                    intent.putExtra("selectedJob", getIntent().getStringExtra("selectedJob"));
                    intent.putExtra("cityList", cityList);
                    intent.putExtra("switchFromClient", getIntent().getBooleanExtra("switchFromClient", false));
                    intent.putExtra("category", getIntent().getStringExtra("category"));
                    intent.putExtra("suppLatitude", suppLatitude);
                    intent.putExtra("suppLongitude", suppLongitude);
                    startActivity(intent);
                    overridePendingTransition(R.anim.pushleft_in, R.anim.pushleft_out);
                }
            }
        });

        this.zonesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                checkRow(view, position);
            }
        });
    }

    private void checkRow(View view, int position) {
        if (!view.isEnabled())
            enableRow(view, position);
        else
            disableRow(view, position);
    }

    private void listPainter(View view, boolean enabled) {
        TextView text = (TextView) view.findViewById(R.id.label);
        ImageView image = (ImageView) view.findViewById(R.id.icon);
        if (enabled) {
            text.setTextColor(Color.BLACK);
            image.setImageResource(R.drawable.tick);
            view.setEnabled(true);
        } else {
            text.setTextColor(Color.GRAY);
            image.setImageResource(R.drawable.notick);
            view.setEnabled(false);
        }
    }

    private void enableRow(View view, int position) {
        checkSelectedZone(view, this.zones.get(position));
        listPainter(view, true);
    }

    private void disableRow(View view, int position) {
        checkSelectedZone(view, this.zones.get(position));
        listPainter(view, false);
    }

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    private boolean checkSelectedZone(View view, ParseObject zone) {
        ParseGeoPoint geoPos = (ParseGeoPoint) zone.get("centercords");

        if (!view.isEnabled()) { //Adding new Marker
            locations.add(geoPos);
            markers.add(mMap.addMarker(new MarkerOptions()
                    .position(geoposToLatlng(geoPos))
                    .title(zone.get("nome").toString())));
            circles.add(mMap.addCircle(newCircleOptions(geoposToLatlng(geoPos))));
            cityList.add(zone.get("nome").toString());
            mMap.animateCamera(CameraUpdateFactory.newLatLng(geoposToLatlng(geoPos)));
            return true;
        } else { //Removing Zone previously selected
            markers.get(locations.indexOf(geoPos)).remove(); //Remove marker from the Map
            markers.remove(locations.indexOf(geoPos)); //Remove marker from markers list
            circles.get(locations.indexOf(geoPos)).remove(); //Remove circle from the Map
            circles.remove(locations.indexOf(geoPos)); //Remove circle from markers list

            locations.remove(locations.indexOf(geoPos));
            cityList.remove(cityList.indexOf(zone.get("nome")));
            return false;
        }
    }

    private void setMapListener() {
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng coords) {
                try {
                    ParseObject province = new GoogleMapsGeocodingRequest().getProvinceFromCoords(coords, zones);

                    if (province != null) { //Add/Remove zone to the map
                        int listPos = zones.indexOf(province);
                        checkRow(getViewByPosition(listPos, zonesList), listPos);
                    } else
                        Toast.makeText(getApplicationContext(), "Provincia non presente",
                                Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static LatLng geoposToLatlng(ParseGeoPoint point) {
        return new LatLng(point.getLatitude(), point.getLongitude());
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
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
                        if (ActivityCompat.checkSelfPermission(ZoneChooserActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ZoneChooserActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            ActivityCompat.requestPermissions(ZoneChooserActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 23);

                            return;
                        }
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, ZoneChooserActivity.this);
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(ZoneChooserActivity.this, 3456);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.

                        try {
                            setMainZone(mMap); //Initialize map
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

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
        Log.i("POSITION FOUND: ", Double.toString(location.getLatitude()) + ", " + Double.toString(location.getLongitude()));

        this.initialPosition = new ParseGeoPoint(location.getLatitude(), location.getLongitude()); //Initialize position

        try {
            setMainZone(mMap); //Initialize map
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Setto la posizione per l'utente se è un fornitore che si sta registrando
        try {
            ParseUser user = ParseUser.getCurrentUser();
            if (user != null) {
                user.fetch();
                if (user.getParseGeoPoint("locationCords") == null)
                    user.put("locationCords", this.initialPosition);
            } else {
                this.suppLatitude = location.getLatitude();
                this.suppLongitude = location.getLongitude();
            }
        } catch (ParseException e) {
            ParseErrorHandler.handleParseError(e, getApplicationContext());
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

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
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, ZoneChooserActivity.this);

                } else {
                        return;
                    // permission denied, boo! Disable the functionality that depends on this permission.
                }
            break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 3456) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Log.i("GPS_Request","OK");
                    if (ActivityCompat.checkSelfPermission(ZoneChooserActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ZoneChooserActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        ActivityCompat.requestPermissions(ZoneChooserActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 23);

                        return;
                    }
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, ZoneChooserActivity.this);
                    break;
                case Activity.RESULT_CANCELED:

                    try {
                        setMainZone(mMap); //Initialize map
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;
            }
        }
        else if (requestCode == 6943) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    callGPS();
                    break;
                case Activity.RESULT_CANCELED:
                    finish();
                    break;
            }
        }
    }

    private void showGPSPopup(){
        Intent intent = new Intent(ZoneChooserActivity.this, GpsAlertActivity.class);
        intent.putExtra("isClient", false);
        startActivityForResult(intent, 6943);
    }

    protected void callGPS(){
        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        JobbeApplication.activityPaused();
    }

}
