package com.division70.jobbe.jobbe_git;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.location.LocationServices;
import com.parse.FunctionCallback;
import com.parse.GetDataCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by giorgio on 10/11/15.
 */
public class AcceptedBidRecapClientActivity extends Activity {

    private TextView bidDetail;
    private TextView bidPrice;
    private TextView bidPriceType;
    private TextView suppName;
    private TextView suppJob;
    private TextView suppIsProfessionist;
    private Button suppBio;
    private ImageView phonecall;

    private ImageView userImageView;
    private TextView numberOfReviewsTextView;
    private RatingBar ratingBar;
    private RatingBar priceRateBar;
    private RatingBar timeRateBar;
    private RatingBar qualityRateBar;
    private RatingBar courtesyRateBar;
    private LinearLayout ratingsLayout;

    private String propID;
    private String reqTitle;

    private ParseObject request;
    private ParseObject proposta;
    private ParseObject supplier;
    private ParseObject supplierJob;
    private ParseFile userPhoto;

    private String professionistBacgroundColor = "#a81154";
    private String nonProfessionistBacgroundColor = "#ffffff";

    private JSONObject offlineJSON;
    private String offlineData;

    private Map<String, Float> ratings;
    private String price_text;
    private String price_type;
    private String bid_desc;
    private String supp_name;
    private String supp_job;
    private int supp_num_review;
    private String supp_phone;
    private double supp_avg_rating;
    private String supp_type;
    private String iconName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acceptedbidrecapclient_layout);

        this.init();
        setUpActionBar();
    }

    private void setUpActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        CustomActionbar customActionbar = new CustomActionbar(this);
        customActionbar.setTitle(reqTitle);
        actionBar.setCustomView(customActionbar);
        actionBar.setDisplayShowCustomEnabled(true);

        ImageView back = (ImageView) customActionbar.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        RelativeLayout title = (RelativeLayout) customActionbar.findViewById(R.id.title_layout);
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.checkConnection(getApplicationContext())) {
                    Intent intent = new Intent(AcceptedBidRecapClientActivity.this, RequestsArchiveDetailsActivity.class);
                    intent.putExtra("reqId", request.getObjectId());
                    startActivity(intent);
                }
            }
        });
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

    private void init() {

        try {
            this.bidDetail = (TextView) findViewById(R.id.bid_detail);
            this.bidPrice = (TextView) findViewById(R.id.bid_price);
            this.bidPriceType = (TextView) findViewById(R.id.bid_price_type);
            this.suppName = (TextView) findViewById(R.id.supplier_name);
            this.suppJob = (TextView) findViewById(R.id.supplier_job);
            this.suppIsProfessionist = (TextView) findViewById(R.id.supplier_professionist);
            this.suppBio = (Button) findViewById(R.id.supplier_bio);
            this.ratingBar = (RatingBar) findViewById(R.id.ratingBar);
            this.priceRateBar = (RatingBar) findViewById(R.id.ratingBar_price);
            this.timeRateBar = (RatingBar) findViewById(R.id.ratingBar_time);
            this.qualityRateBar = (RatingBar) findViewById(R.id.ratingBar_quality);
            this.courtesyRateBar = (RatingBar) findViewById(R.id.ratingBar_courtesy);
            this.numberOfReviewsTextView = (TextView) findViewById(R.id.textView_numberofreviews);
            this.phonecall = (ImageView) findViewById(R.id.phonecall_image);
            this.ratingsLayout = (LinearLayout) findViewById(R.id.ratings_layout);

            setRatingBar(ratingBar);
            setRatingBar(priceRateBar);
            setRatingBar(timeRateBar);
            setRatingBar(qualityRateBar);
            setRatingBar(courtesyRateBar);

            offlineData = getIntent().getStringExtra("offlineData");
            if (offlineData == null) {
                Log.i("ONLINE RECAP", "online recap");

                this.propID = getIntent().getStringExtra("propID");
                this.proposta = ParseObject.createWithoutData("Proposal", this.propID);
                this.proposta.fetch();

                this.supplier = (ParseObject) this.proposta.get("supplierId");
                this.supplier.fetch();

                this.request = (ParseObject) this.proposta.get("requestId");
                this.request.fetch();

                try {
                    ArrayList<ParseObject> resultJob =
                            (ArrayList<ParseObject>) ParseQuery.getQuery("Jobs").whereEqualTo("supplierId", this.supplier).find();
                    this.supplierJob = resultJob.get(0);

                    ParseObject category = this.supplierJob.getParseObject("categoryListId").fetchIfNeeded();
                    this.iconName = category.getString("icon").replace("-", "_");

                } catch (ParseException e) {
                    ParseErrorHandler.handleParseError(e, getApplicationContext());
                }

                this.userPhoto = (ParseFile) this.supplier.get("profilePhoto");
                this.userImageView = (ImageView) findViewById(R.id.supplier_pic);
                if (userPhoto != null) {
                    userPhoto.getDataInBackground(new GetDataCallback() {
                        @Override
                        public void done(byte[] data, ParseException e) {
                            if (e == null) {
                                userImageView.setImageDrawable(null);
                                Log.i("data length", data.length + "");
                                int scaleFactor = data.length / 10000;
                                Log.i("scaleFactor", scaleFactor + "");
                                BitmapFactory.Options options = new BitmapFactory.Options();// Create object of bitmapfactory's option method for further option use
                                options.inPurgeable = true; // inPurgeable is used to free up memory while required
                                if (scaleFactor > 1)
                                    options.inSampleSize = scaleFactor;
                                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                                userImageView.setImageBitmap(bmp);
                            } else {
                            }
                        }
                    });
                } else {
                    userImageView.setImageResource(R.drawable.placeholder_client);
                }
                getDataOnline();
            } else {
                offlineJSON = new JSONObject(offlineData);
                Log.i("OFFLINE DATA", offlineJSON.toString());
                getDataOffline();
            }

            setLabels();
            setListeners();

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setRatingBar(RatingBar ratingBar) {
        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();

        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.jobbe_ratings),
                PorterDuff.Mode.SRC_ATOP); // for filled stars

        stars.getDrawable(1).setColorFilter(Color.TRANSPARENT,
                PorterDuff.Mode.SRC_ATOP); // for half filled stars

        stars.getDrawable(0).setColorFilter(Color.TRANSPARENT,
                PorterDuff.Mode.SRC_ATOP);
    }

    private void getDataOnline() {
        this.reqTitle = this.request.get("title").toString();
        this.price_text = this.proposta.get("price").toString() + "€";
        this.price_type = getBidPriceType();
        this.bid_desc = this.proposta.get("description").toString();
        this.supp_name = this.supplier.get("displayName").toString();
        this.supp_job = this.supplierJob.get("name").toString();
        this.supp_num_review = this.supplier.getInt("numberOfReviews");
        this.supp_avg_rating = this.supplier.getDouble("averageRating");
        this.supp_phone = supplier.getString("username");
        ratings = getRatings(supplier);
        this.supp_type = proposta.getString("type");
    }

    private void setIcon() {
        this.iconName = this.iconName + "_nob";
        ImageView jobCatPic = (ImageView) findViewById(R.id.job_cat_pic);

        try {
            int resourceId = getResources().getIdentifier(this.iconName, "drawable", "com.division70.jobbe.jobbe_git");
            jobCatPic.setImageResource(resourceId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setLabels() {
        setTitle();
        setIcon();
        this.bidPrice.setText(price_text);
        this.bidPriceType.setText(price_type);
        this.bidDetail.setText(bid_desc);
        this.suppName.setText(supp_name);
        this.suppJob.setText(supp_job);
        //setSupplierIsProfessionist();
        setSuppTypeView(supp_type);

        // Set Ratings
        courtesyRateBar.setRating(ratings.get("courtesy"));
        timeRateBar.setRating(ratings.get("timing"));
        qualityRateBar.setRating(ratings.get("quality"));
        priceRateBar.setRating(ratings.get("price"));

        if (supp_num_review > 0) {
            String numberOfReviewsText = supp_num_review + "";
            this.numberOfReviewsTextView.setText(numberOfReviewsText);
            this.ratingBar.setRating((float) supp_avg_rating);
        } else {
            ratingBar.setVisibility(View.GONE);
            numberOfReviewsTextView.setText("nuovo fornitore");
            ratingsLayout.setVisibility(View.GONE);
        }
    }

    private Map<String, Float> getRatings(ParseObject supplier) {
        Map<String, Float> ret = new HashMap<>();
        ArrayList<HashMap<String, Object>> results = (ArrayList<HashMap<String, Object>>) this.supplier.get("averageRatingSet");
        if (results.size() > 0) {
            HashMap<String, Object> ratings = results.get(0);
            //Log.i("ratings", ratings.get("courtesy").toString());
            ret.put("price", (float) objToDouble(ratings.get("price")));
            ret.put("timing", (float) objToDouble(ratings.get("timing")));
            ret.put("quality", (float) objToDouble(ratings.get("quality")));
            ret.put("courtesy", (float) objToDouble(ratings.get("courtesy")));
        } else {
            ret.put("price", (float) 0.0);
            ret.put("timing", (float) 0.0);
            ret.put("quality", (float) 0.0);
            ret.put("courtesy", (float) 0.0);
        }

        return ret;
    }

    private void getDataOffline() {
        try {
            this.propID = offlineJSON.getString("bid_id");
            this.reqTitle = offlineJSON.getString("title");
            this.price_text = offlineJSON.getDouble("bid_price") + "€";
            this.price_type = offlineJSON.getString("bid_type");
            this.bid_desc = offlineJSON.getString("bid_desc");
            this.supp_name = offlineJSON.getString("supp_name");
            this.supp_job = offlineJSON.getString("supp_job");
            this.supp_num_review = offlineJSON.getInt("supp_num_review");
            this.supp_avg_rating = offlineJSON.getDouble("supp_avg_rating");
            this.supp_phone = offlineJSON.getString("supp_phone");
            this.supp_type = offlineJSON.getString("supp_type");
            ratings = new HashMap<>();
            ratings.put("price", (float) offlineJSON.getDouble("supp_price_rating"));
            ratings.put("timing", (float) offlineJSON.getDouble("supp_timing_rating"));
            ratings.put("quality", (float) offlineJSON.getDouble("supp_quality_rating"));
            ratings.put("courtesy", (float) offlineJSON.getDouble("supp_courtesy_rating"));
            Log.i("ratings", ratings.get("price") + " " + ratings.get("timing") + " " + ratings.get("quality") + " " + ratings.get("courtesy"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setTitle() {
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(this.reqTitle);
    }

    private void setSupplierIsProfessionist() {
        switch (this.supplierJob.get("supplierType").toString()) {
            case "professionist":
                setProfessionist();
                break;
            case "hobby":
                setNonProfessionist();
                break;
        }
    }

    private void setProfessionist() {
        this.suppIsProfessionist.setText("Professionista");
        this.suppIsProfessionist.setBackgroundColor(Color.parseColor(this.professionistBacgroundColor));
    }

    private void setNonProfessionist() {
        this.suppIsProfessionist.setText("Non Professionista");
        this.suppIsProfessionist.setBackgroundColor(Color.parseColor(this.nonProfessionistBacgroundColor));
    }

    public void setSuppTypeView(String type) {

        switch (type) {
            case "professionist":
                suppIsProfessionist.setText("Professionista");
                suppIsProfessionist.setBackgroundColor(getResources().getColor(R.color.jobbe_violet));
                suppIsProfessionist.setTextColor(0xFFFFFFFF);
                suppIsProfessionist.setTypeface(null, Typeface.BOLD);
                break;
            case "hobby":
                suppIsProfessionist.setText("Non Professionista");
                //suppTypeView.setBackgroundColor(0x00000000);
                suppIsProfessionist.setBackgroundColor(Color.TRANSPARENT);
                suppIsProfessionist.setTextColor(0xFF777777);
                suppIsProfessionist.setTypeface(null, Typeface.NORMAL);
                suppIsProfessionist.setPadding(0, 0, 0, 0);
                suppIsProfessionist.setGravity(Gravity.LEFT);
                break;
        }
    }

    private String getBidPriceType() {
        switch (this.proposta.get("priceType").toString()) {
            case "corpo":
                return "a corpo";
            case "ore":
                return "a misura";
        }
        return null;
    }

    private void setListeners() {
        this.suppBio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.checkConnection(getApplicationContext())) {

                    //Get tracker.
                    Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
                    //Build and send an Event.
                    t.send(new HitBuilders.EventBuilder()
                            .setCategory(getString(R.string.analytics_cat_usability_client))
                            .setAction(getString(R.string.analytics_action_click))
                            .setLabel("biografia_fornitore")
                            .build());

                    Intent intent = new Intent(AcceptedBidRecapClientActivity.this, BiographyActivity.class);
                    intent.putExtra("suppID", supplier.getObjectId());
                    startActivity(intent);
                }
            }
        });

        this.phonecall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneCallalert();
               }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 23:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the task you need to do.

                    phoneCallalert();
                } else {
                    return;
                    // permission denied, boo! Disable the functionality that depends on this permission.
                }
                break;

        }
    }

    public void phoneCallalert(){
        {
            final AlertDialog.Builder builder = new AlertDialog.Builder(AcceptedBidRecapClientActivity.this);
            builder.setTitle(getString(R.string.phone_call_alert_title))
                    .setMessage("+39 " + supp_phone)
                    .setCancelable(false)
                    .setPositiveButton("Chiama", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            //Get tracker.
                            Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
                            //Build and send an Event.
                            t.send(new HitBuilders.EventBuilder()
                                    .setCategory(getString(R.string.analytics_cat_usability_client))
                                    .setAction(getString(R.string.analytics_action_click))
                                    .setLabel("chiamare_fornitore")
                                    .build());

                            String uri = "tel:" + supp_phone;
                            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(uri));
                            if (ActivityCompat.checkSelfPermission(AcceptedBidRecapClientActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                ActivityCompat.requestPermissions(AcceptedBidRecapClientActivity.this, new String[]{android.Manifest.permission.CALL_PHONE}, 23);

                                return;
                            }
                            startActivity(intent);

                            dialog.dismiss();

                        }
                    }).setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();

                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private double objToDouble(Object value){
        try{
            return ((double) ((Integer) value).intValue());
        }
        catch (Exception e){
            return (double) value;
        }
    }

    private void toClientHome(){
        Intent intent = new Intent(AcceptedBidRecapClientActivity.this, HomeClientActivity.class);
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
