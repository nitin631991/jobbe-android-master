package com.division70.jobbe.jobbe_git;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
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

public class ZoneRecapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ArrayList<ParseObject> zones;
    private ArrayList<String> cityList;

    private ImageView icon;
    private TextView selectedJobRecap;
    private Button fine_button;
    private Switch pIVA;
    private boolean pIVA_enabled = false; //Default
    private String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zonerecap_layout);

        sendScreenAnalytics();

        this.init();

        setUpMapIfNeeded();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_recap);
        mapFragment.getMapAsync(this);
        setUpActionBar();
    }

    private void setUpActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        CustomActionbar customActionbar = new CustomActionbar(this);
        customActionbar.setTitle("Conferma mestiere");
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
        t.setScreenName("SIGNUP - FORNITORE CONFERMA LAVORO");
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onResume() {
        super.onResume();
        JobbeApplication.activityResumed();
        Utils.deleteNotifications(getApplicationContext());
        setUpMapIfNeeded();

    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_recap))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        ParseGeoPoint center = (ParseGeoPoint) this.zones.get(0).get("centercords");
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(geoposToLatlng(center)));
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(9));

        for(ParseObject zone : this.zones) {
            ParseGeoPoint geoPos = (ParseGeoPoint) zone.get("centercords");
            googleMap.addMarker(new MarkerOptions()
                    .position(geoposToLatlng(geoPos))
                    .title(this.zones.get(0).get("nome").toString()));

            googleMap.addCircle(this.newCircleOptions(geoposToLatlng(geoPos)));
        }
    }

    private CircleOptions newCircleOptions(LatLng coords){
        return new CircleOptions()
                .radius(7000)
                .center(coords)
                .strokeColor(Color.BLUE)
                .strokeWidth(4)
                .fillColor(0x8571BED1);
    }

    private void init(){
        this.icon = (ImageView) findViewById(R.id.icon);
        this.pIVA = (Switch) findViewById(R.id.switch1);
        this.pIVA.setChecked(false);
        this.selectedJobRecap = (TextView) findViewById(R.id.selected_job_recap);
        this.selectedJobRecap.setText(getIntent().getStringExtra("selectedJob"));
        this.cityList = getIntent().getStringArrayListExtra("cityList");
        this.category = getIntent().getStringExtra("category");
        this.zones = new ArrayList<>();

        for(String city : this.cityList) {
            try {
                this.zones.add(
                        (ParseObject) ParseQuery.getQuery("Zone")
                                .whereEqualTo("active", true)
                                .whereEqualTo("nome", city).find().get(0));
                Log.i("CITY", this.zones.get(0).get("nome").toString());
            } catch (ParseException e) {
                ParseErrorHandler.handleParseError(e, getApplicationContext());
                e.printStackTrace();
            }
        }

        Log.i("CAT: ", this.category);
        setRequestPicture();

        this.fine_button = (Button) findViewById(R.id.zone_end_button);
        this.fine_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ZoneRecapActivity.this, CreateSupplierActivity.class);
                intent.putExtra("selectedJob", getIntent().getStringExtra("selectedJob"));
                intent.putExtra("pIVA_enabled", pIVA_enabled);
                intent.putExtra("cityList", cityList);
                intent.putExtra("switchFromClient", getIntent().getBooleanExtra("switchFromClient", false));
                intent.putExtra("suppLatitude", getIntent().getDoubleExtra("suppLatitude", 0));
                intent.putExtra("suppLongitude", getIntent().getDoubleExtra("suppLongitude", 0));
                startActivity(intent);
                overridePendingTransition(R.anim.pushleft_in, R.anim.pushleft_out);
            }
        });

        this.pIVA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pIVA_enabled = isChecked;
            }
        });
    }

    private void setRequestPicture(){
        try {
            ParseObject request = null;
            ArrayList<ParseObject> result = (ArrayList<ParseObject>) ParseQuery.getQuery("CategoryList").whereEqualTo("name", this.category).find();
            if(result != null) {
                request = result.get(0);
                String iconName = request.get("icon").toString().replace("-", "_");
                int resourceId = getResources().getIdentifier(iconName, "drawable", "com.division70.jobbe.jobbe_git");
                this.icon.setImageResource(resourceId);
            }
        } catch (ParseException e) {
            ParseErrorHandler.handleParseError(e, getApplicationContext());
            e.printStackTrace();
        }
    }

    private LatLng geoposToLatlng(ParseGeoPoint point){
        return new LatLng(point.getLatitude(), point.getLongitude());
    }


    @Override
    protected void onPause() {
        super.onPause();
        JobbeApplication.activityPaused();
    }
}
