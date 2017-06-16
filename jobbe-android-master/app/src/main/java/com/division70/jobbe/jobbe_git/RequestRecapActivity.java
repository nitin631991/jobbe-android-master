package com.division70.jobbe.jobbe_git;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;


/**
 * Created by giorgio on 09/11/15.
 */
public class RequestRecapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private TextView clientName;
    private TextView reqAddress;
    private TextView reqTiming;
    private TextView description;
    private TextView address;
    private Button deleteRequest;
    private Button preventivo_button;
    private RelativeLayout spinnerLayout;

    private String reqId;

    private ParseObject request;
    private ParseUser reqUser;
    private ParseUser user;

    private ParseGeoPoint center;
    private double latitude;
    private double longitude;

    private boolean fromPush = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.requestrecap_layout);

        try {
            Badges.removeBadge(this);
            Badges.setBadge(this, 0);
        } catch (BadgesNotSupportedException badgesNotSupportedException) {
            Log.d("wydtws", badgesNotSupportedException.getMessage());
        }

        sendScreenAnalytics();

        this.init();
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
        customActionbar.setTitle(this.request.getString("title"));
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
    }

    private void sendScreenAnalytics() {
        Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
        t.setScreenName("FORNITORE - PAGINA RICHIESTA");
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

    /*
    private void init(){
        try {
            this.clientName = (TextView) findViewById(R.id.client_name);
            this.reqAddress = (TextView) findViewById(R.id.req_address);
            this.reqTiming = (TextView) findViewById(R.id.req_timing);
            this.description = (TextView) findViewById(R.id.description);
            this.deleteRequest = (Button) findViewById(R.id.delete_request);
            this.address = (TextView) findViewById(R.id.address);
            this.spinnerLayout = (RelativeLayout) findViewById(R.id.loadingSpinnerLayout);
            this.preventivo_button = (Button) findViewById(R.id.preventivo_button);

            this.user = ParseUser.getCurrentUser().fetchIfNeeded();

            this.reqId = getIntent().getStringExtra("reqId");
            ArrayList<ParseObject> request = (ArrayList<ParseObject>) ParseQuery.getQuery("Request").whereEqualTo("objectId", this.reqId).find();
            this.request = request.get(0);

            setReqTextFields();

            setListeners();
            spinnerLayout.setVisibility(View.GONE);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    */
    private void init() {
        new initAsync().execute();
    }

    private void setListeners() {

        this.deleteRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(RequestRecapActivity.this);
                builder.setTitle(getString(R.string.request_delition_alert_title))
                        .setMessage(getString(R.string.request_delition_alert_desc))
                        .setCancelable(false)
                        .setPositiveButton("Conferma", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                /*
                                Map<String, Object> delete = new HashMap<>();
                                delete.put("reqId", request.getObjectId());
                                ParseCloud.callFunctionInBackground("deleteRequest", delete); //Delete request
                                */
                                removeFromMatchingRequests();

                                dialog.dismiss();
                                toSupplierHome();
                            }
                        }).setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Annulla cancellazione",
                                Toast.LENGTH_LONG).show();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        this.preventivo_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(RequestRecapActivity.this, BidCreationActivity.class);
                intent.putExtra("name", clientName.getText().toString());
                intent.putExtra("address", reqAddress.getText().toString());
                intent.putExtra("timing", reqTiming.getText().toString());
                intent.putExtra("reqId", request.getObjectId().toString());
                intent.putExtra("title", request.getString("title"));
                startActivity(intent);
                overridePendingTransition(R.anim.pushleft_in, R.anim.pushleft_out);
            }
        });
    }

    private void setReqTextFields() {
        try {
            this.reqUser = (ParseUser) this.request.get("userId");
            this.reqUser.fetchIfNeeded();
            setTitle();
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

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public String getTimingFromTimingType() {
        String timing = this.request.getString("timingType");
        String text = "";
        switch (timing) {
            case "notUrgent":
                text = "Da concordare/non urgente";
                break;
            case "soonAsPossible":
                text = "Il prima possibile";
                break;
            case "specific":
                Date date = this.request.getDate("dateRequest");
                if (date == null) {
                    Log.i("Error", "date cannot be null with specific timing");
                } else {
                    text = createDateString(date);
                }
                break;
        }
        return text;
    }

    public String createDateString(Date date) {
        String intMonth = (String) android.text.format.DateFormat.format("MM", date); //06
        String year = (String) android.text.format.DateFormat.format("yyyy", date); //2013
        String day = (String) android.text.format.DateFormat.format("dd", date); //20
        return day + "/" + intMonth + "/" + year;
    }

    private void setTitle() {
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(request.get("title").toString());
    }

    private void addPosToMap(GoogleMap googleMap, ParseGeoPoint geoPos) {
        googleMap.clear(); //Delete markers & circles for further positions
        googleMap.addMarker(new MarkerOptions()
                .position(geoposToLatlng(geoPos)));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(geoposToLatlng(geoPos)));
        googleMap.addCircle(this.newCircleOptions(geoposToLatlng(geoPos)));
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(16));
    }

    private LatLng geoposToLatlng(ParseGeoPoint point) {
        return new LatLng(point.getLatitude(), point.getLongitude());
    }

    private void toSupplierHome() {
        Intent intent = new Intent(RequestRecapActivity.this, HomeSupplierActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void removeFromMatchingRequests() {
        try {
            user.fetch();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ArrayList<String> requestsId = (ArrayList<String>) user.get("matchingRequests");
        for (String x : requestsId)
            if (x.equals(request.getObjectId())) {
                requestsId.remove(x);
                break;
            }
        user.put("matchingRequests", requestsId);
        try {
            user.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        backFunction();
    }

    private void backFunction() {
        this.fromPush = getIntent().getBooleanExtra("fromPush", false);
        if (!fromPush)
            finish();
        else
            toSupplierHome();
    }


    private class initAsync extends AsyncTask<Void, Void, Boolean> {


        @Override
        protected void onPreExecute() {
            clientName = (TextView) findViewById(R.id.client_name);
            reqAddress = (TextView) findViewById(R.id.req_address);
            reqTiming = (TextView) findViewById(R.id.req_timing);
            description = (TextView) findViewById(R.id.description);
            deleteRequest = (Button) findViewById(R.id.delete_request);
            address = (TextView) findViewById(R.id.address);
            spinnerLayout = (RelativeLayout) findViewById(R.id.loadingSpinnerLayout);
            preventivo_button = (Button) findViewById(R.id.preventivo_button);
            spinnerLayout.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... par) {

            try {
                user = ParseUser.getCurrentUser().fetch();
                reqId = getIntent().getStringExtra("reqId");
                request = ParseQuery.getQuery("Request").whereEqualTo("objectId", reqId).getFirst();
            } catch (ParseException e) {
                ParseErrorHandler.handleParseError(e, getApplicationContext());
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean ok) {
            if (ok) {
                setReqTextFields();
                setListeners();

                setUpMapIfNeeded();
                addPosToMap(mMap, center);
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(RequestRecapActivity.this);

                setUpActionBar();
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