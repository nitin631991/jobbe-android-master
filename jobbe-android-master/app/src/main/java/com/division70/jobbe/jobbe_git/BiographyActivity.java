package com.division70.jobbe.jobbe_git;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.Log;
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
import com.parse.FunctionCallback;
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
 * Created by giorgio on 10/11/15.
 */
public class BiographyActivity extends Activity {

    private TextView suppDesc;
    private TextView suppName;

    private RoundedImageView userImageView;
    private TextView numberOfReviewsTextView;
    private RatingBar ratingBar;

    private ParseObject supplier;
    private ParseFile userPhoto;
    private String suppID;
    private Button closeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.biography_layout);

        sendScreenAnalytics();

        this.init();

        setUpActionBar();
    }

    private void setUpActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        CustomActionbar customActionbar = new CustomActionbar(this);
        customActionbar.setTitle("Biografia");
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

    private void setRatingBar(RatingBar ratingBar){
        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();

        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.jobbe_ratings),
                PorterDuff.Mode.SRC_ATOP); // for filled stars

        stars.getDrawable(1).setColorFilter(Color.TRANSPARENT,
                PorterDuff.Mode.SRC_ATOP); // for half filled stars
    }

    private void sendScreenAnalytics(){
        Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
        t.setScreenName("CLIENTE - RICHIESTA - BIOGRAFIA FORNITORE");
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void init(){
        try {
            this.suppDesc = (TextView) findViewById(R.id.supp_desc);
            this.suppName = (TextView) findViewById(R.id.supplier_name);
            this.ratingBar = (RatingBar) findViewById(R.id.ratingBar);
            this.numberOfReviewsTextView = (TextView) findViewById(R.id.textView_numberofreviews);
            this.closeButton = (Button) findViewById(R.id.close_button);

            setRatingBar(ratingBar);

            this.suppID = getIntent().getStringExtra("suppID");
            Log.i("SuppID", suppID);
            this.supplier = ParseUser.getQuery().get(suppID);
            Log.i("CHECK", "1");
            this.supplier.fetch();
            Log.i("CHECK", "2");

            this.userPhoto = (ParseFile) this.supplier.get("profilePhoto");
            this.userImageView = (RoundedImageView) findViewById(R.id.supplier_pic);
            if (userPhoto != null) {
                Picasso.with(this).load(userPhoto.getUrl()).into(userImageView);
//                userPhoto.getDataInBackground(new GetDataCallback() {
//                    @Override
//                    public void done(byte[] data, ParseException e) {
//                        if (e == null) {
//                            userImageView.setImageDrawable(null);
//                            Log.i("data length", data.length + "");
//                            int scaleStep = getResources().getInteger(R.integer.photo_scale_step);
//                            int scaleFactor = data.length / scaleStep;
//                            Log.i("scaleFactor", scaleFactor + "");
//                            BitmapFactory.Options options=new BitmapFactory.Options();// Create object of bitmapfactory's option method for further option use
//                            options.inPurgeable = true; // inPurgeable is used to free up memory while required
//                            if(scaleFactor > 1)
//                                options.inSampleSize = scaleFactor;
//                            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
//                            userImageView.setImageBitmap(bmp);
//                        } else {
//                        }
//                    }
//                });
            } else {
                userImageView.setImageResource(R.drawable.placeholder_client);
            }

            setLabels();
            setListeners();

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void setLabels(){
        String name = this.supplier.getString("displayName");
        String desc = this.supplier.getString("desc");
        if (desc == null){
            desc = "Biografia non presente";
        }
        this.suppName.setText(name);
        this.suppDesc.setText(desc);
        //setSupplierIsProfessionist();


        int numberOfReviews = (int) this.supplier.get("numberOfReviews");
        if(numberOfReviews > 0){
            this.numberOfReviewsTextView.setText(numberOfReviews + "");
            this.ratingBar.setRating((float) supplier.getDouble("averageRating"));
        }
        else {
            this.numberOfReviewsTextView.setText("Nuovo fornitore");
            this.ratingBar.setVisibility(View.GONE);
        }

    }

    private void setListeners(){
        this.closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onBackPressed(){
        finish();
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
