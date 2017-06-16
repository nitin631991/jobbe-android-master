package com.division70.jobbe.jobbe_git;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.github.arturogutierrez.Badges;
import com.github.arturogutierrez.BadgesNotSupportedException;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by giorgio on 10/11/15.
 */
public class BidRecapActivity extends Activity {

    private TextView bidDetail;
    private TextView bidPrice;
    private TextView bidPriceType;
    private TextView suppName;
    private TextView suppJob;
    private TextView suppIsProfessionist;
    private Button suppBio;
    private Button chooseSupp;
    private Button refuseBid;

    private RoundedImageView userImageView;
    private TextView numberOfReviewsTextView;
    private RatingBar ratingBar;
    private RatingBar priceRateBar;
    private RatingBar timeRateBar;
    private RatingBar qualityRateBar;
    private RatingBar courtesyRateBar;
    private LinearLayout ratingsLayout;

    private String propID;
    private String reqTitle;
    private String iconName;

    private ParseObject request;
    private ParseObject proposta;
    private ParseObject supplier;
    private ParseObject supplierJob;
    private ParseFile userPhoto;

    private String professionistBacgroundColor = "#a81154";
    private String nonProfessionistBacgroundColor = "#ffffff";
    private RelativeLayout spinnerLayout;
    private RelativeLayout scrollRelative;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bidrecap_layout);

        sendScreenAnalytics();

        try {
            Badges.removeBadge(this);
            Badges.setBadge(this, 0);
        } catch (BadgesNotSupportedException badgesNotSupportedException) {
            Log.d("wydtws", badgesNotSupportedException.getMessage());
        }

        //this.init();
        new initAsync().execute();

        //setUpActionBar();
    }

    private void setUpActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        CustomActionbar customActionbar = new CustomActionbar(this);
        customActionbar.setTitle(this.request.getString("title"));
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
                Intent intent = new Intent(BidRecapActivity.this, RequestsArchiveDetailsActivity.class);
                intent.putExtra("reqId", request.getObjectId());
                startActivity(intent);
            }
        });
    }

    private void sendScreenAnalytics(){
        Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
        t.setScreenName("CLIENTE - RICHIESTA - PAGINA PROPOSTA");
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                backFunction();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
    private void init(){
        try {
            this.bidDetail = (TextView) findViewById(R.id.bid_detail);
            this.bidPrice = (TextView) findViewById(R.id.bid_price);
            this.bidPriceType = (TextView) findViewById(R.id.bid_price_type);
            this.suppName = (TextView) findViewById(R.id.supplier_name);
            this.suppJob = (TextView) findViewById(R.id.supplier_job);
            this.suppIsProfessionist = (TextView) findViewById(R.id.supplier_professionist);
            this.suppBio = (Button) findViewById(R.id.supplier_bio);
            this.chooseSupp = (Button) findViewById(R.id.choose_supplier);
            this.refuseBid = (Button) findViewById(R.id.elimina_proposta);
            this.ratingBar = (RatingBar) findViewById(R.id.ratingBar);
            this.priceRateBar = (RatingBar) findViewById(R.id.ratingBar_price);
            this.timeRateBar = (RatingBar) findViewById(R.id.ratingBar_time);
            this.qualityRateBar = (RatingBar) findViewById(R.id.ratingBar_quality);
            this.courtesyRateBar = (RatingBar) findViewById(R.id.ratingBar_courtesy);
            this.numberOfReviewsTextView = (TextView) findViewById(R.id.textView_numberofreviews);
            this.ratingsLayout = (LinearLayout) findViewById(R.id.ratings_layout_linear);

            this.propID = getIntent().getStringExtra("propID");
            this.proposta = ParseObject.createWithoutData("Proposal", this.propID);
            this.proposta.fetch();

            this.supplier = (ParseObject) this.proposta.get("supplierId");
            this.supplier.fetchIfNeeded();

            this.request = (ParseObject) this.proposta.get("requestId");
            this.request.fetchIfNeeded();

            ArrayList<ParseObject> resultJob =
                    (ArrayList<ParseObject>) ParseQuery.getQuery("Jobs").whereEqualTo("supplierId", this.supplier).find();
            this.supplierJob = resultJob.get(0);

            this.userPhoto = (ParseFile) this.supplier.get("profilePhoto");
            this.userImageView = (RoundedImageView) findViewById(R.id.supplier_pic);
            if (userPhoto != null) {
                userPhoto.getDataInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] data, ParseException e) {
                        if (e == null) {
                            userImageView.setImageDrawable(null);
                            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                            userImageView.setImageBitmap(bmp);
                        } else {
                        }
                    }
                });
            } else {
                userImageView.setImageResource(R.drawable.placeholder_client);
            }

            setLabels();
            setListeners();

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    */

    private void setRatingBar(RatingBar ratingBar){
        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();

        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.jobbe_ratings),
                PorterDuff.Mode.SRC_ATOP); // for filled stars

        stars.getDrawable(1).setColorFilter(Color.TRANSPARENT,
                PorterDuff.Mode.SRC_ATOP); // for half filled stars
    }

    private void setViews(){
        this.scrollRelative = (RelativeLayout) findViewById(R.id.scrollRelative);
        scrollRelative.setVisibility(View.GONE);
        this.bidDetail = (TextView) findViewById(R.id.bid_detail);
        this.bidPrice = (TextView) findViewById(R.id.bid_price);
        this.bidPriceType = (TextView) findViewById(R.id.bid_price_type);
        this.suppName = (TextView) findViewById(R.id.supplier_name);
        this.suppJob = (TextView) findViewById(R.id.supplier_job);
        this.suppIsProfessionist = (TextView) findViewById(R.id.supplier_professionist);
        this.suppBio = (Button) findViewById(R.id.supplier_bio);
        this.chooseSupp = (Button) findViewById(R.id.choose_supplier);
        this.refuseBid = (Button) findViewById(R.id.elimina_proposta);
        this.ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        this.priceRateBar = (RatingBar) findViewById(R.id.ratingBar_price);
        this.timeRateBar = (RatingBar) findViewById(R.id.ratingBar_time);
        this.qualityRateBar = (RatingBar) findViewById(R.id.ratingBar_quality);
        this.courtesyRateBar = (RatingBar) findViewById(R.id.ratingBar_courtesy);
        this.numberOfReviewsTextView = (TextView) findViewById(R.id.textView_numberofreviews);
        this.ratingsLayout = (LinearLayout) findViewById(R.id.ratings_layout_linear);
        this.userImageView = (RoundedImageView) findViewById(R.id.supplier_pic);

        setRatingBar(ratingBar);
        setRatingBar(priceRateBar);
        setRatingBar(timeRateBar);
        setRatingBar(qualityRateBar);
        setRatingBar(courtesyRateBar);
    }

    private boolean fetchData(){
        try {
            Log.i("Fetchdata", "start");
            this.propID = getIntent().getStringExtra("propID");
            this.proposta = ParseObject.createWithoutData("Proposal", this.propID);
            this.proposta.fetch();

            this.supplier = (ParseObject) this.proposta.get("supplierId");
            this.supplier.fetchIfNeeded();

            this.request = (ParseObject) this.proposta.get("requestId");
            this.request.fetchIfNeeded();

            ArrayList<ParseObject> resultJob =
                    (ArrayList<ParseObject>) ParseQuery.getQuery("Jobs").whereEqualTo("supplierId", this.supplier).find();
            this.supplierJob = resultJob.get(0);

            ParseObject category = this.supplierJob.getParseObject("categoryListId").fetchIfNeeded();
            this.iconName = category.getString("icon").replace("-","_");

            Log.i("Fetchdata", "photo");
            this.userPhoto = (ParseFile) this.supplier.get("profilePhoto");

            Log.i("Fetchdata", "updateunseen");
            updateUnseen();
        } catch (ParseException e){
            ParseErrorHandler.handleParseError(e, getApplicationContext());
            e.printStackTrace();
            Log.i("fetchData", "Exception caught, return false");
            return false;
        }
        return true;
    }

    private void updateUnseen() {
        boolean seen = proposta.getBoolean("seen");
        Log.i("seen", seen + "");
        if (!seen){
            proposta.put("seen", true);
            int unseen = request.getInt("unseenProposals");
            Log.i("originalUnseen", unseen + "");
            request.put("unseenProposals", unseen - 1);
            proposta.saveInBackground();
            request.saveInBackground();
        }
    }

    private void setUserPhoto(){
        if (userPhoto != null) {

            try {
                byte[] data = userPhoto.getData();
                userImageView.setImageDrawable(null);
                Log.i("data length", data.length + "");
                int scaleStep = getResources().getInteger(R.integer.photo_scale_step);
                int scaleFactor = data.length / scaleStep;
                Log.i("scaleFactor", scaleFactor + "");
                BitmapFactory.Options options=new BitmapFactory.Options();// Create object of bitmapfactory's option method for further option use
                options.inPurgeable = true; // inPurgeable is used to free up memory while required
                if(scaleFactor > 1)
                    options.inSampleSize = scaleFactor;
                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                userImageView.setImageBitmap(bmp);
            } catch (ParseException e) {
                e.printStackTrace();
                userImageView.setImageResource(R.drawable.placeholder_client);
            }


        }
        else {
            userImageView.setImageResource(R.drawable.placeholder_client);
        }
    }

    /*private void setIcon(){
        String iconName = getIntent().getStringExtra("iconName") + "_nob";
        ImageView jobCatPic = (ImageView) findViewById(R.id.job_cat_pic);

        int resourceId = getResources().getIdentifier(iconName, "drawable", "com.division70.jobbe.jobbe_git");
        jobCatPic.setImageResource(resourceId);
    }*/

    private void setIcon(){
        this.iconName = this.iconName + "_nob";
        ImageView jobCatPic = (ImageView) findViewById(R.id.job_cat_pic);

        try {
            int resourceId = getResources().getIdentifier(this.iconName, "drawable", "com.division70.jobbe.jobbe_git");
            jobCatPic.setImageResource(resourceId);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private void setLabels(){
        this.reqTitle = this.request.get("title").toString();
        Log.i("reqTitle", this.reqTitle);
        setIcon();
        this.bidPrice.setText(this.proposta.get("price").toString() + "€");
        this.bidPriceType.setText(getBidPriceType());
        this.bidDetail.setText(this.proposta.get("description").toString());
        this.suppName.setText(this.supplier.get("displayName").toString());
        this.suppJob.setText(this.supplierJob.get("name").toString());
        //setSupplierIsProfessionist();
        setSuppTypeView();

        // Set Ratings
        ArrayList<HashMap<String,Object>> results = (ArrayList<HashMap<String,Object>>) this.supplier.get("averageRatingSet");
        if (results.size() > 0) {
            HashMap<String,Object> ratings = results.get(0);
            //Log.i("ratings", ratings.get("courtesy").toString());
            double priceRating = objToDouble(ratings.get("price"));
            double timeRating = objToDouble(ratings.get("timing"));
            double qualityRating = objToDouble(ratings.get("quality"));
            double courtesyRating = objToDouble(ratings.get("courtesy"));
            Log.i("ratings", priceRating + " " + timeRating + " " + qualityRating + " " + courtesyRating + " ");

            courtesyRateBar.setRating((float) courtesyRating);
            timeRateBar.setRating((float) timeRating);
            qualityRateBar.setRating((float) qualityRating);
            priceRateBar.setRating((float) priceRating);
        }
        else{
            //Undefined
            courtesyRateBar.setRating((float) 0.0);
            timeRateBar.setRating((float) 0.0);
            qualityRateBar.setRating((float) 0.0);
            priceRateBar.setRating((float) 0.0);

            ratingBar.setVisibility(View.GONE);
            numberOfReviewsTextView.setText("nuovo fornitore");
            ratingsLayout.setVisibility(View.GONE);
        }


        int numberOfReviews = (int) this.supplier.get("numberOfReviews");
        if(numberOfReviews > 0){
            this.numberOfReviewsTextView.setText(numberOfReviews + "");
            this.ratingBar.setRating((float) this.supplier.getDouble("averageRating"));
        }
        else {
            this.numberOfReviewsTextView.setText("Nuovo fornitore");
            this.ratingBar.setVisibility(View.GONE);
        }

    }

    /*
    private void setSupplierIsProfessionist(){
        switch(this.supplierJob.get("supplierType").toString()){
            case "professionist":
                setProfessionist();
                break;
            case "hobby":
                setNonProfessionist();
                break;
        }
    }

    private void setProfessionist(){
        this.suppIsProfessionist.setText("Professionista");
        this.suppIsProfessionist.setBackgroundColor(Color.parseColor(this.professionistBacgroundColor));
    }

    private void setNonProfessionist(){
        this.suppIsProfessionist.setText("Non Professionista");
        this.suppIsProfessionist.setBackgroundColor(Color.parseColor(this.nonProfessionistBacgroundColor));
    }
    */

    public void setSuppTypeView(){
        String type = proposta.getString("type");
        switch (type){
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

    private String getBidPriceType(){
        switch(this.proposta.get("priceType").toString()){
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

                //Get tracker.
                Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
                //Build and send an Event.
                t.send(new HitBuilders.EventBuilder()
                        .setCategory(getString(R.string.analytics_cat_usability_client))
                        .setAction(getString(R.string.analytics_action_click))
                        .setLabel("biografia_fornitore")
                        .build());

                Intent intent = new Intent(BidRecapActivity.this, BiographyActivity.class);
                intent.putExtra("suppID", supplier.getObjectId());
                startActivity(intent);
            }
        });

        this.chooseSupp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(BidRecapActivity.this)
                        .setTitle("Hai scelto questo fornitore?")
                        .setMessage("Al fornitore verrà comunicato il tuo numero di telefono e " +
                                "vi potrete accordare personalmente sui dettagli dell'intervento")
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton("Conferma", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {

                                try {
                                    proposta.fetch();
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                if(proposta.getBoolean("deletedBySupplier")){
                                    Toast.makeText(getApplicationContext(), "Il fornitore ha eliminato questa proposta",
                                            Toast.LENGTH_LONG).show();
                                    backFunction();
                                }
                                else if(request.getInt("state") != 1){
                                    Toast.makeText(getApplicationContext(), "Hai già accettato una proposta per questa richiesta",
                                            Toast.LENGTH_LONG).show();
                                    backFunction();
                                }
                                else {

                                    ParseUser client = ParseUser.getCurrentUser();
                                    Map<String, Object> accept = new HashMap<>();
                                    accept.put("supplierId", supplier.getObjectId());
                                    accept.put("reqId", request.getObjectId());
                                    accept.put("proposalId", proposta.getObjectId());
                                    accept.put("clientDisplayName", client.get("displayName").toString());
                                    accept.put("reqTitle", reqTitle.toString());
                                    //ParseCloud.callFunctionInBackground("acceptProposal", accept); //Accept proposal
                                    try {
                                        ParseCloud.callFunction("acceptProposal", accept); //Accept proposal
                                    } catch (ParseException e) {
                                        ParseErrorHandler.handleParseError(e, getApplicationContext());
                                        e.printStackTrace();
                                        finish();
                                        return;
                                    }
                                    Toast.makeText(getApplicationContext(), "Hai scelto la proposta di questo fornitore",
                                            Toast.LENGTH_LONG).show();

                                    //Get tracker.
                                    Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
                                    //Build and send an Event.
                                    t.send(new HitBuilders.EventBuilder()
                                            .setCategory(getString(R.string.analytics_cat_flow_client))
                                            .setAction(getString(R.string.analytics_action_click))
                                            .setLabel("accetta_fornitore")
                                            .build());

                                    toClientHome();
                                }

                            }
                        }).create().show();
            }
        });

        this.refuseBid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(BidRecapActivity.this)
                        .setTitle("Sei sicuro di voler eliminare questa proposta?")
                        .setMessage("Le proposte eliminate non saranno più recuperabili")
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton("Elimina", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {

                                try {
                                    proposta.fetch();
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                if(proposta.getBoolean("deletedBySupplier")){
                                    Toast.makeText(getApplicationContext(), "Ilfornitore ha già eliminato questa proposta",
                                            Toast.LENGTH_LONG).show();
                                    finish();
                                }
                                else {

                                    Map<String, Object> params = new HashMap<>();
                                    params.put("reqId", request.getObjectId());
                                    params.put("proposalId", proposta.getObjectId());
                                    params.put("deleted", "YES");
                                    params.put("deletedBySupplier", "NO");
                                    ParseCloud.callFunctionInBackground("deleteProposal", params, new FunctionCallback<Object>() {
                                        @Override
                                        public void done(Object o, ParseException e) {
                                            if (e == null) {
                                                Toast.makeText(getApplicationContext(), "Proposta eliminata", Toast.LENGTH_LONG).show();

                                                toClientHome();
                                            } else
                                                e.printStackTrace();
                                        }
                                    });
                                }

                            }
                        }).create().show();
            }
        });
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
        Intent intent = new Intent(BidRecapActivity.this, HomeClientActivity.class);
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
    public void onBackPressed(){
        backFunction();
    }

    private void backFunction(){
        Boolean fromPush = getIntent().getBooleanExtra("fromPush", false);
        Log.i("fromPush", fromPush + "");
        if (!fromPush)
            finish();
        else {
            //toClientHome();
            toProposalsList();
        }
    }

    private void toProposalsList() {
        Intent intent = new Intent(BidRecapActivity.this, ProposalsListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("reqId", request.getObjectId());
        intent.putExtra("fromPush", true);
        startActivity(intent);
    }

    private class initAsync extends AsyncTask<Void, Void, Boolean> {


        @Override
        protected void onPreExecute() {
            spinnerLayout = (RelativeLayout) findViewById(R.id.loadingSpinnerLayout);
            spinnerLayout.setVisibility(View.VISIBLE);
            setViews();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return fetchData();
            //return null;
        }


        @Override
        protected void onPostExecute(Boolean ok) {
            Log.i("postExecuteOk", ok + "");
            if (ok) {
                setUpActionBar();
                setUserPhoto();
                setLabels();
                setListeners();
                scrollRelative.setVisibility(View.VISIBLE);
                spinnerLayout.setVisibility(View.GONE);
            }
            else
                finish();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("Jobbe_SP", Context.MODE_PRIVATE);
        int count = sharedPreferences.getInt("count", 0);
        if (count > 0)
            sharedPreferences.edit().putInt("count", count - 1).commit();
        JobbeApplication.activityResumed();
        Utils.deleteNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();
        JobbeApplication.activityPaused();
    }

}
