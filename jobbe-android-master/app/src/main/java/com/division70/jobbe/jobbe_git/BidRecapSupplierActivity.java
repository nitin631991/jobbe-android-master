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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by giorgio on 09/11/15.
 */
public class BidRecapSupplierActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private TextView clientName;
    private TextView reqAddress;
    private TextView reqTiming;
    private TextView description;
    private TextView bidPrice;
    private TextView bidDetails;
    private TextView address;
    private Button eliminaPreventivo;

    private String reqId;

    private ParseUser supplier;
    private ParseObject request;
    private ParseObject proposta;
    private ParseUser reqUser;

    private ParseGeoPoint center;
    private double latitude;
    private double longitude;
    private RelativeLayout spinnerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bidrecapsupplier_layout);

        this.init();
        /*
        setUpMapIfNeeded();
        addPosToMap(mMap, this.center);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setUpActionBar();
        */
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
            this.eliminaPreventivo = (Button) findViewById(R.id.elimina_preventivo_button);

            this.reqId = getIntent().getStringExtra("reqId");
            Log.i("reqID", this.reqId + "");
            this.request = ParseObject.createWithoutData("Request", this.reqId);
            this.request.fetch();

            this.supplier = ParseUser.getCurrentUser();
            supplier.fetchIfNeeded();
            ArrayList<ParseObject> result = (ArrayList<ParseObject>) ParseQuery.getQuery("Proposal")
                    .whereEqualTo("requestId", this.request)
                    .whereEqualTo("supplierId", this.supplier).find();
            this.proposta = result.get(0);
            this.proposta.fetchIfNeeded();

            setReqTextFields();

            setListeners();

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    */

    private void init(){
        new initAsync().execute();
    }

    private void setListeners(){

        this.eliminaPreventivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(BidRecapSupplierActivity.this);
                builder.setTitle(getString(R.string.proposal_delition_alert_title))
                        .setMessage(getString(R.string.proposal_delition_alert_desc))
                        .setCancelable(false)
                        .setPositiveButton("Conferma", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new deleteBidAsync().execute();
                                /*
                                Map<String, Object> delete = new HashMap<>();
                                delete.put("reqId", request.getObjectId());
                                delete.put("proposalId", proposta.getObjectId());
                                delete.put("deleted", "NO");
                                delete.put("deletedBySupplier", "YES");
                                try {
                                    ParseCloud.callFunction("deleteProposal", delete); //Delete proposal by supplier
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                removeFromMatchingRequests();

                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Cancellazione preventivo",
                                        Toast.LENGTH_LONG).show();

                                toSupplierHome();
                                */
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
    }

    private void setReqTextFields(){
        try {
            this.reqUser = (ParseUser) this.request.get("userId");
            this.reqUser.fetchIfNeeded();
            this.clientName.setText(reqUser.get("displayName").toString());
            this.center = (ParseGeoPoint) this.request.get("locationCords");
            this.latitude = this.center.getLatitude();
            this.longitude = this.center.getLongitude();
            this.reqTiming.setText(getTimingFromTimingType());
            this.description.setText(this.request.get("description").toString());
            this.bidPrice.setText(this.proposta.get("price").toString() + " " +
                    this.proposta.get("priceType").toString());
            this.bidDetails.setText(this.proposta.get("description").toString());

            String address = new GoogleMapsGeocodingRequest()
                    .getAddressForMapFromCoords(new LatLng(this.latitude, this.longitude));
            this.reqAddress.setText(address);
            this.address.setText("nei pressi di " + address);

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
        String dayOfTheWeek = (String) android.text.format.DateFormat.format("EEEE", date);//Thursday
        String stringMonth = (String) android.text.format.DateFormat.format("MMM", date); //Jun
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
        Intent intent = new Intent(BidRecapSupplierActivity.this, HomeSupplierActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void removeFromMatchingRequests() {
        try {
            supplier.fetch();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ArrayList<String> requestsId = (ArrayList<String>) supplier.get("matchingRequests");
        for (String x : requestsId)
            if (x.equals(request.getObjectId())) {
                requestsId.remove(x);
                break;
            }
        supplier.put("matchingRequests", requestsId);
        try {
            supplier.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private class initAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            clientName = (TextView) findViewById(R.id.client_name);
            reqAddress = (TextView) findViewById(R.id.req_address);
            reqTiming = (TextView) findViewById(R.id.req_timing);
            description = (TextView) findViewById(R.id.description);
            bidPrice = (TextView) findViewById(R.id.bid_price);
            bidDetails = (TextView) findViewById(R.id.bid_details);
            address = (TextView) findViewById(R.id.address);
            eliminaPreventivo = (Button) findViewById(R.id.elimina_preventivo_button);
            spinnerLayout = (RelativeLayout) findViewById(R.id.loadingSpinnerLayout);
            spinnerLayout.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... par) {
            try {
                reqId = getIntent().getStringExtra("reqId");
                Log.i("reqID", reqId + "");
                request = ParseObject.createWithoutData("Request", reqId);
                request.fetch();

                supplier = ParseUser.getCurrentUser();
                supplier.fetchIfNeeded();
                proposta = ParseQuery.getQuery("Proposal")
                        .whereEqualTo("requestId", request)
                        .whereEqualTo("supplierId", supplier).getFirst();

            } catch (ParseException e) {
                ParseErrorHandler.handleParseError(e, getApplicationContext());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            setReqTextFields();
            setListeners();

            setUpMapIfNeeded();
            addPosToMap(mMap, center);
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(BidRecapSupplierActivity.this);

            setUpActionBar();
            spinnerLayout.setVisibility(View.GONE);
        }

    }




    private class deleteBidAsync extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            spinnerLayout.setVisibility(View.VISIBLE);
        }
        @Override
        protected Boolean doInBackground(Void... par) {
            try {
                request.fetch();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            ParseObject assigned = request.getParseObject("choosenProposal");
            if (assigned == proposta){
                return false;
            }
            else {
                Map<String, Object> delete = new HashMap<>();
                delete.put("reqId", request.getObjectId());
                delete.put("proposalId", proposta.getObjectId());
                delete.put("deleted", "NO");
                delete.put("deletedBySupplier", "YES");
                try {
                    ParseCloud.callFunction("deleteProposal", delete); //Delete proposal by supplier
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                removeFromMatchingRequests();


                return true;
            }
        }


        @Override
        protected void onPostExecute(Boolean deleted) {
            if(deleted)
                Toast.makeText(getApplicationContext(), "Proposta eliminata",
                    Toast.LENGTH_LONG).show();

            else
                Toast.makeText(getApplicationContext(), "La tua proposta è già stata selezionata dal cliente",
                        Toast.LENGTH_LONG).show();

            toSupplierHome();
            spinnerLayout.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        JobbeApplication.activityPaused();
    }


}