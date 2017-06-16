package com.division70.jobbe.jobbe_git;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.parse.GetDataCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by giorgio on 12/11/15.
 */
public class SupplierReviewActivity extends Activity {

    private TextView suppName;
    private TextView suppJob;
    private TextView bidPrice;
    private TextView bidPriceType;
    private ImageView userImageView;
    private Button reviewButton;

    private ParseUser user;
    private ParseObject supplier;
    private ParseObject supplierJob;
    private ParseObject request;
    private ParseObject proposta;

    private ParseFile userPhoto;

    private String reqId;

    private RatingBar priceRateBar;
    private RatingBar timeRateBar;
    private RatingBar qualityRateBar;
    private RatingBar courtesyRateBar;

    private int price, timing, quality, courtesy; //Ratings variables

    private boolean reviewSent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.supplierreview_layout);

        sendScreenAnalytics();

        this.init();
        setUpActionBar();
    }

    private void setUpActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        CustomActionbar customActionbar = new CustomActionbar(this);
        customActionbar.setTitle("Valutazione fornitore");
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
        t.setScreenName("CLIENTE - RICHIESTA _ REVIEW");
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void init(){
        this.priceRateBar = (RatingBar) findViewById(R.id.ratingBar_price);
        this.timeRateBar = (RatingBar) findViewById(R.id.ratingBar_time);
        this.qualityRateBar = (RatingBar) findViewById(R.id.ratingBar_quality);
        this.courtesyRateBar = (RatingBar) findViewById(R.id.ratingBar_courtesy);
        this.suppName = (TextView) findViewById(R.id.supplier_name);
        this.suppJob = (TextView) findViewById(R.id.supplier_job);
        this.bidPrice = (TextView) findViewById(R.id.bid_price);
        this.bidPriceType = (TextView) findViewById(R.id.bid_price_type);
        this.userImageView = (ImageView) findViewById(R.id.supplier_pic);
        this.reviewButton = (Button) findViewById(R.id.confirm_button);

        price = timing = quality = courtesy = 0; //Default values

        this.reqId = getIntent().getStringExtra("reqId");

        this.user = ParseUser.getCurrentUser();
        try {
            this.user.fetchIfNeeded();
        } catch (ParseException e) {
            ParseErrorHandler.handleParseError(e, getApplicationContext());
            e.printStackTrace();
        }

        this.request = ParseObject.createWithoutData("Request", this.reqId);
        try {
            this.request.fetch();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        this.supplier = (ParseObject) this.request.get("choosenSupplier");
        try {
            this.supplier.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        this.proposta = (ParseObject) this.request.get("choosenProposal");
        try {
            this.proposta.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(alreadyExistsReview(this.request)) {
            Toast.makeText(getApplicationContext(), "Hai già effettuato la recensione per questa richiesta", Toast.LENGTH_LONG).show();
            toClientHome();
        }

        ArrayList<ParseObject> resultJob =  null;
        try {
            resultJob = (ArrayList<ParseObject>) ParseQuery.getQuery("Jobs").whereEqualTo("supplierId", this.supplier).find();
        } catch (ParseException e) {
            ParseErrorHandler.handleParseError(e, getApplicationContext());
            e.printStackTrace();
        }
        this.supplierJob = resultJob.get(0);

        setLabels();
        setListeners();
    }

    private boolean alreadyExistsReview(ParseObject request) {
        /*try {
            ParseObject review = ParseQuery.getQuery("Reviews").whereEqualTo("reqId", request).getFirst();
            if(review == null)
                return false;
        } catch (ParseException e) {
            if (e.getCode() == ParseException.OBJECT_NOT_FOUND)  //NON ESISTE NESSUN UTENTE, MA LA QUERY È ANDATA A BUON FINE
                e.printStackTrace();
            else
                ParseErrorHandler.handleParseError(e, getApplicationContext());
            return false;
        }
        */

        int reqState = this.request.getInt("state");
        Log.i("STATE", reqState + "");

        if(reqState != 3 && reqState != 6) //State 3 -> waiting for user's review --- State 6 -> cancelled by supplier
            return true; //Request not waiting for review, AVOID CONTINUING

        return false; //Request waiting for review, OK
    }

    private void setListeners(){

        this.reviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!reviewSent) {
                    reviewSent = true;
                    price = (int) priceRateBar.getRating();
                    timing = (int) timeRateBar.getRating();
                    quality = (int) qualityRateBar.getRating();
                    courtesy = (int) courtesyRateBar.getRating();

                    if (checkRating()) {
                        Map<String, Object> review = new HashMap<>();
                        review.put("reqId", request.getObjectId());
                        review.put("supplierId", supplier.getObjectId());
                        review.put("price", price);
                        review.put("timing", timing);
                        review.put("quality", quality);
                        review.put("courtesy", courtesy);
                        try {
                            ParseCloud.callFunction("reviewRequest", review); //Send supplier review
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        //Get tracker.
                        Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
                        //Build and send an Event.
                        t.send(new HitBuilders.EventBuilder()
                                .setCategory(getString(R.string.analytics_cat_flow_client))
                                .setAction(getString(R.string.analytics_action_click))
                                .setLabel("conferma_lavoro_svolto")
                                .build());

                        toClientHome();
                    }
                    else
                        reviewSent = false;
                }
            }
        });

    }

    private boolean checkRating(){
        if(this.price == 0 ||
                this.timing == 0 ||
                this.quality == 0 ||
                this.courtesy == 0)
        {
            Toast.makeText(getApplicationContext(), "Completa le valutazioni per favore",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void setLabels(){
        this.suppName.setText(this.supplier.get("displayName").toString());
        this.suppJob.setText(this.supplierJob.get("name").toString());
        this.bidPrice.setText(this.proposta.get("price").toString() + "€");
        this.bidPriceType.setText(this.proposta.get("priceType").toString());

        this.userPhoto = (ParseFile) supplier.get("profilePhoto");
        if (userPhoto != null) {
            Picasso.with(this).load(userPhoto.getUrl()).into(userImageView);
//            userPhoto.getDataInBackground(new GetDataCallback() {
//                @Override
//                public void done(byte[] data, ParseException e) {
//                    if (e == null) {
//                        userImageView.setImageDrawable(null);
//                        Log.i("data length", data.length + "");
//                        int scaleStep = getResources().getInteger(R.integer.photo_scale_step);
//                        int scaleFactor = data.length / scaleStep;
//                        Log.i("scaleFactor", scaleFactor + "");
//                        BitmapFactory.Options options=new BitmapFactory.Options();// Create object of bitmapfactory's option method for further option use
//                        options.inPurgeable = true; // inPurgeable is used to free up memory while required
//                        if(scaleFactor > 1)
//                            options.inSampleSize = scaleFactor;
//                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
//                        userImageView.setImageBitmap(bmp);
//                    } else {
//                    }
//                }
//            });
        } else {
            // PUT PLACEHOLDER
            userImageView.setImageResource(R.drawable.placeholder_client);
        }
    }

    private void toClientHome(){
        Intent intent = new Intent(SupplierReviewActivity.this, HomeClientActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private int getUserRequestsNumber(ParseUser user) {
        List<ParseObject> list;
        ArrayList<Integer> illegalStates = new ArrayList<Integer>();
        illegalStates.add(4);
        illegalStates.add(5);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
        query.whereEqualTo("userId", user);
        query.whereNotContainedIn("state", illegalStates);
        query.whereNotEqualTo("confirmUncompleteReq", true);
        query.include("job");
        query.include("jobCatId");
        query.include("choosenProposal");
        query.include("choosenSupplier");
        query.orderByDescending("createdAt");
        try {
            list = query.find();
            return list.size();
        }
        catch (ParseException e){
            Log.i("REQUEST_LIST", "FAILED");
            Log.i("ParseException",e.toString());
            ParseErrorHandler.handleParseError(e, getApplicationContext());
        }
        return 0;
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
