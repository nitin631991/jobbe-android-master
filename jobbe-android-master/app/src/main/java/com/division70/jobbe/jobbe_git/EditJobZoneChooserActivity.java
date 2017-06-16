package com.division70.jobbe.jobbe_git;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
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

import java.util.ArrayList;

public class EditJobZoneChooserActivity extends FragmentActivity implements OnMapReadyCallback {

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
    ArrayList<String> initialCityList;
    ArrayList<ParseObject> initialZoneList;
    private RelativeLayout spinnerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zonechooser_layout);

        sendScreenAnalytics();

        //this.init();
        new initAsync().execute();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setUpActionBar();
    }

    private void setUpActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        CustomActionbar customActionbar = new CustomActionbar(this);
        customActionbar.setTitle("Modifica zone");
        customActionbar.disableSubtitle();
        actionBar.setCustomView(customActionbar);
        actionBar.setDisplayShowCustomEnabled(true);

        ImageView back = (ImageView) customActionbar.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean canReturn = returnResults();
                if (canReturn)
                    finish();
            }
        });
    }

    private void sendScreenAnalytics(){
        Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
        t.setScreenName("FORNITORE - PROFILO - AGGIORNA ZONA LAVORO");
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

    private void setUpMap() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setMapListener();
            }
        }
        else
            //Here if the user went on the next activity and came back
            //Clearing the map
            mMap.clear();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Show GPS Permission PopUp
        //showGPSPopup();
        Log.i("GOOGLE", "MAP READY");
        /*
        try {
            setMainZone(googleMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }

    private void setMainZone() throws Exception {
        ArrayList<ParseObject> provinces = new ArrayList<>();
        for (ParseObject x : initialZoneList) {
            for (ParseObject y : this.zones) {
                Log.i("CONFRONTO", x.getString("nome") + " , " + y.getString("nome"));
                if (x.getString("nome").equals(y.getString("nome"))) {
                    Log.i("FOUND", x.getString("nome"));
                    provinces.add(y);
                    break;
                }
            }
        }

        Log.i("provincesSize",provinces.size() + "");
        for (ParseObject province : initialZoneList){
            Log.i("PROVINCE", province.getString("nome"));
            if (province != null) {
                this.initialPosition = (ParseGeoPoint) province.get("centercords");
                this.mMap.moveCamera(CameraUpdateFactory.newLatLng(geoposToLatlng(this.initialPosition)));
                this.mMap.moveCamera(CameraUpdateFactory.zoomTo(9));

                //Update List
                //int listPos = getListPosFromZoneName(province.getString("nome"));
                int listPos = this.zones.indexOf(province);
                Log.i("LISTPOS", listPos + "");
                enableRow(getViewByPosition(listPos, this.zonesList), listPos);
            }
        }

        spinnerLayout.setVisibility(View.GONE);
    }

    private int getListPosFromZoneName(String zoneName){
        int pos = 0;
        for(ParseObject zone : this.zones)
            if(zone.get("nome").toString().equals(zoneName))
                return pos;
            else pos++;
        return -1; //Default error value
    }

    private void setVoidMap(GoogleMap googleMap, ParseGeoPoint geoPos){
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(geoposToLatlng(geoPos)));
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(9));
    }

    private void addPosToMap(GoogleMap googleMap, ParseGeoPoint geoPos){
        this.locations.add(geoPos);
        this.markers.add(googleMap.addMarker(new MarkerOptions()
                .position(geoposToLatlng(geoPos))));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(geoposToLatlng(geoPos)));
        this.circles.add(googleMap.addCircle(this.newCircleOptions(geoposToLatlng(geoPos))));
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(9));
    }

    private static CircleOptions newCircleOptions(LatLng coords){
        return new CircleOptions()
                .radius(7000)
                .center(coords)
                .strokeColor(Color.BLUE)
                .strokeWidth(5)
                .fillColor(0x8571BED1);
    }

    /*
    private void init(){
        this.spinnerLayout = (RelativeLayout) findViewById(R.id.loadingSpinnerLayout);
        setUpMap(); //Initialize mMap

        this.markers = new ArrayList<>();
        this.circles = new ArrayList<>();
        this.locations = new ArrayList<>();
        this.zones = new ArrayList<>();
        this.confirmZone = (Button) findViewById(R.id.confirm_zone_button);
        this.zonesList = (ListView) findViewById(R.id.zones_list);

        this.cityList = new ArrayList<>();

        this.initialCityList = getIntent().getStringArrayListExtra("cityList");
        this.initialZoneList = new ArrayList<>();

        for (String city : initialCityList) {
            try {
                initialZoneList.add(
                        ParseQuery.getQuery("Zone")
                                .whereEqualTo("active", true)
                                .whereEqualTo("nome", city).find().get(0));
                Log.i("CITY", initialZoneList.get(0).get("nome").toString());
            } catch (ParseException exc) {
                exc.printStackTrace();
            }
        }

        try {
            this.zones = (ArrayList<ParseObject>) ParseQuery.getQuery("Zone").whereEqualTo("active", true).find(); //Getting zones
        } catch (ParseException e) {
            e.printStackTrace();
        }

        renderList();
        setUpList();
        setListeners();
    }
    */

    private void renderList(){
        ArrayList<String> values = new ArrayList<>();
        for(ParseObject zone : this.zones)
            values.add(zone.get("nome").toString());
        CustomArrayAdapterEditZones adapter = new CustomArrayAdapterEditZones(this, R.layout.rowlayout, R.id.label, values);
        this.zonesList.setAdapter(adapter);
    }

    private void setUpList(){
        this.zonesList.post(new Runnable() {
            @Override
            public void run() {
                Log.i("zones.size()", zones.size() + "");
                for (int i = 0; i < zones.size(); i++)
                    listPainter(getViewByPosition(i, zonesList), false);

                try {
                    setMainZone();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setListeners(){
        //Button listener
        this.confirmZone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean canReturn = returnResults();
                if (canReturn) finish();
            }
        });

        this.zonesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                checkRow(view, position);
            }
        });
    }

    private void checkRow(View view, int position){
        if (!view.isEnabled())
            enableRow(view, position);
        else
            disableRow(view, position);
    }

    private void listPainter(View view, boolean enabled){
        TextView text = (TextView) view.findViewById(R.id.label);
        ImageView image = (ImageView) view.findViewById(R.id.icon);
        if(enabled){
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

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    private boolean checkSelectedZone(View view, ParseObject zone){
        ParseGeoPoint geoPos = (ParseGeoPoint) zone.get("centercords");

        if(!view.isEnabled()) { //Adding new Marker
            locations.add(geoPos);
            markers.add(mMap.addMarker(new MarkerOptions()
                    .position(geoposToLatlng(geoPos))
                    .title(zone.get("nome").toString())));
            circles.add(mMap.addCircle(newCircleOptions(geoposToLatlng(geoPos))));
            cityList.add(zone.get("nome").toString());
            mMap.animateCamera(CameraUpdateFactory.newLatLng(geoposToLatlng(geoPos)));
            return true;
        }
        else{ //Removing Zone previously selected
            markers.get(locations.indexOf(geoPos)).remove(); //Remove marker from the Map
            markers.remove(locations.indexOf(geoPos)); //Remove marker from markers list
            circles.get(locations.indexOf(geoPos)).remove(); //Remove circle from the Map
            circles.remove(locations.indexOf(geoPos)); //Remove circle from markers list

            locations.remove(locations.indexOf(geoPos));
            cityList.remove(cityList.indexOf(zone.get("nome")));
            return false;
        }
    }

    private void setMapListener(){
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

    private static LatLng geoposToLatlng(ParseGeoPoint point){
        return new LatLng(point.getLatitude(), point.getLongitude());
    }

    @Override
    public void onBackPressed(){
        boolean canReturn = returnResults();
        if(canReturn)
            super.onBackPressed();
    }

    private boolean returnResults(){
        for (String x : cityList)
            Log.i("citylist", x);
        if (cityList.size() < 1) {
            Toast.makeText(getApplicationContext(), "È necessario selezionare almeno una zona", Toast.LENGTH_LONG).show();
            return false;
        }
        Intent intent = new Intent();
        intent.putExtra("selectedJob", getIntent().getStringExtra("selectedJob"));
        intent.putExtra("cityList", cityList);
        intent.putExtra("switchFromClient", getIntent().getBooleanExtra("switchFromClient", false));
        setResult(7321, intent);
        return true;
        //finish();
    }

    private class initAsync extends AsyncTask<Void, Void, Void> {


        @Override
        protected void onPreExecute() {
            spinnerLayout = (RelativeLayout) findViewById(R.id.loadingSpinnerLayout);
            setUpMap(); //Initialize mMap
            confirmZone = (Button) findViewById(R.id.confirm_zone_button);
            zonesList = (ListView) findViewById(R.id.zones_list);
            initialCityList = getIntent().getStringArrayListExtra("cityList");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            markers = new ArrayList<>();
            circles = new ArrayList<>();
            locations = new ArrayList<>();
            zones = new ArrayList<>();

            cityList = new ArrayList<>();

            initialZoneList = new ArrayList<>();

            for (String city : initialCityList) {
                try {
                    initialZoneList.add(
                            ParseQuery.getQuery("Zone")
                                    .whereEqualTo("active", true)
                                    .whereEqualTo("nome", city).find().get(0));
                    Log.i("CITY", initialZoneList.get(0).get("nome").toString());
                } catch (ParseException exc) {
                    ParseErrorHandler.handleParseError(exc, getApplicationContext());
                    exc.printStackTrace();
                }
            }

            try {
                zones = (ArrayList<ParseObject>) ParseQuery.getQuery("Zone").whereEqualTo("active", true).find(); //Getting zones
            } catch (ParseException e) {
                ParseErrorHandler.handleParseError(e, getApplicationContext());
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void v) {
            renderList();
            setUpList();
            setListeners();
            spinnerLayout.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        JobbeApplication.activityPaused();
    }
}