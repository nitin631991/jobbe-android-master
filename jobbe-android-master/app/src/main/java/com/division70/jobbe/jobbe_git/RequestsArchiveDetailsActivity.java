package com.division70.jobbe.jobbe_git;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by giorgio on 09/11/15.
 */
public class RequestsArchiveDetailsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private TextView clientName;
    private TextView reqAddress;
    private TextView reqTiming;
    private TextView description;
    private TextView bidPrice;
    private TextView bidDetails;
    private TextView address;
    private RelativeLayout bidDetailsLayout;

    private String reqId;
    private Boolean hasChoosenProposal;

    private ParseObject request;
    private ParseObject proposta;
    private ParseObject client;
    private ParseUser reqUser;

    private ParseGeoPoint center;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.requestsarchivedetailsactivity_layout);

        sendScreenAnalytics();

        this.init();
        setUpMapIfNeeded();
        addPosToMap(mMap, this.center);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setUpActionBar();
    }

    private void setUpActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        CustomActionbar customActionbar = new CustomActionbar(this);
        customActionbar.setTitle(this.request.getString("title"));
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
        t.setScreenName("CLIENTE - RICHIESTA - RECAP");
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
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {}

    private CircleOptions newCircleOptions(LatLng coords){
        return new CircleOptions()
                .radius(70)
                .center(coords)
                .strokeColor(Color.BLUE)
                .strokeWidth(4)
                .fillColor(0x8571BED1);
    }

    private void init(){
        try {
            this.clientName = (TextView) findViewById(R.id.client_name);
            this.reqAddress = (TextView) findViewById(R.id.req_address);
            this.reqTiming = (TextView) findViewById(R.id.req_timing);
            this.description = (TextView) findViewById(R.id.description);
            this.bidPrice = (TextView) findViewById(R.id.bid_price);
            this.bidDetails = (TextView) findViewById(R.id.bid_details);
            this.bidDetailsLayout = (RelativeLayout) findViewById(R.id.details_layout);
            this.address = (TextView) findViewById(R.id.address);

            this.reqId = getIntent().getStringExtra("reqId");
            this.request = ParseObject.createWithoutData("Request", this.reqId);
            this.request.fetch();

            this.proposta = (ParseObject) this.request.get("choosenProposal");
            if (this.proposta == null)
                hasChoosenProposal = false;
            else {
                hasChoosenProposal = true;
                this.proposta.fetchIfNeeded();
            }

            this.client = (ParseObject) this.request.get("userId");
            this.client.fetchIfNeeded();

            setReqTextFields();

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    private void setReqTextFields(){
        try {
            this.reqUser = (ParseUser) this.request.get("userId");
            this.reqUser.fetchIfNeeded();
            this.clientName.setText(reqUser.get("displayName").toString());
            this.center = (ParseGeoPoint) this.request.get("locationCords");
            this.latitude = this.center.getLatitude();
            this.longitude = this.center.getLongitude();

            String address = new GoogleMapsGeocodingRequest()
                    .getAddressForMapFromCoords(new LatLng(this.latitude, this.longitude));
            this.reqAddress.setText(address);
            this.address.setText("nei pressi di " + address);

            this.reqTiming.setText(getTimingFromTimingType());
            this.description.setText(this.request.get("description").toString());

            if(hasChoosenProposal) {
                this.bidPrice.setText(this.proposta.get("price").toString() + "€ " +
                        this.proposta.get("priceType").toString());
                this.bidDetails.setText(this.proposta.get("description").toString());
            }
            else
                bidDetailsLayout.setVisibility(View.GONE);

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public String getTimingFromTimingType(){
        String timing = this.request.getString("timingType");
        String text = "";
        switch (timing) {
            case "notUrgent": text = "Da concordare/non urgente"; break;
            case "soonAsPossible": text = "Il prima possibile"; break;
            case "specific":
                Date date = this.request.getDate("dateRequest");
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

    @Override
    protected void onPause() {
        super.onPause();
        JobbeApplication.activityPaused();
    }
}