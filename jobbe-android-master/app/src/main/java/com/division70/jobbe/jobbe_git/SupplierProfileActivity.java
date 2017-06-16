package com.division70.jobbe.jobbe_git;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.LogOutCallback;
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
 * Created by giorgio on 27/10/15.
 */
public class SupplierProfileActivity extends Activity {
    private TextView nameTextView;
    private TextView mailTextView;
    private TextView phoneTextView;
    private RatingBar ratingBar;
    private TextView numberOfReviewsTextView;
    private RoundedImageView userImageView;
    private ImageView jobImage;
    private RatingBar priceRateBar;
    private RatingBar timeRateBar;
    private RatingBar qualityRateBar;
    private RatingBar courtesyRateBar;
    private TextView jobNameTextView;
    private TextView jobZonesTextView;
    private TextView toprevNumberTextView;
    private RelativeLayout ratingsLayout;


    private ParseUser user;
    private ParseFile userPhoto;
    private ParseObject job;
    private ArrayList<ParseObject> jobList;

    private String rootUrl = "https://jobbe-dev.parseapp.com/";
    private String terms = "terms";
    private String privacy = "privacy";
    private RelativeLayout spinnerLayout;
    private RelativeLayout modifyJobLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.supplierprofile_layout);

        sendScreenAnalytics();

        try {
            Badges.removeBadge(this);
            Badges.setBadge(this, 0);
        } catch (BadgesNotSupportedException badgesNotSupportedException) {
            Log.d("wydtws", badgesNotSupportedException.getMessage());
        }


        setViews();

        Button switchButton = (Button) findViewById(R.id.button14);
        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchToClient();
            }
        });

        RelativeLayout ppButton = (RelativeLayout) findViewById(R.id.privacy_layout);
        ppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPPview();
            }
        });

        RelativeLayout tcButton = (RelativeLayout) findViewById(R.id.terms_layout);
        tcButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTermsView();
            }
        });

        Button logoutButton = (Button) findViewById(R.id.button13);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

        RelativeLayout shareButton = (RelativeLayout) findViewById(R.id.share_layout);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onShareClick();
            }
        });

        RelativeLayout contattaciButton = (RelativeLayout) findViewById(R.id.contattaci);
        contattaciButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contattaJobbe();
            }
        });

        RelativeLayout modifyDataButton = (RelativeLayout) findViewById(R.id.settings_layout);
        modifyDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modifyData();
            }
        });


        modifyJobLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modifyJob();
            }
        });

        TextView modifyJobLink = (TextView) findViewById(R.id.modifica);
        modifyJobLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modifyJob();
            }
        });

        RelativeLayout archiveButton = (RelativeLayout) findViewById(R.id.archive_layout);
        archiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openArchive();
            }
        });

        LinearLayout userDataLayout = (LinearLayout) findViewById(R.id.userDataLayout);
        userDataLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modifyData();
            }
        });

        RoundedImageView userPic = (RoundedImageView) findViewById(R.id.SuppSettingsImageView);
        userPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modifyData();
            }
        });

        spinnerLayout = (RelativeLayout) findViewById(R.id.loadingSpinnerLayout);

        setUpActionBar();
    }

    private void setUpActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        CustomActionbar customActionbar = new CustomActionbar(this);
        customActionbar.setTitle("Profilo");
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

    private void contattaJobbe(){
        String emailSubject = "Richiesta assistenza\nID:" + ParseUser.getCurrentUser().getObjectId();
        String chooserTitle = "Invita tramite:";
        String messageText = "inviato da Android";

        Intent emailIntent = new Intent();
        emailIntent.setAction(Intent.ACTION_SEND);

        final PackageManager pm = getPackageManager();
        final List<ResolveInfo> matches = pm.queryIntentActivities(emailIntent, 0);
        ResolveInfo best = null;
        for (final ResolveInfo info : matches)
            if (info.activityInfo.packageName.endsWith(".gm") ||
                    info.activityInfo.name.toLowerCase().contains("gmail")) best = info;
        if (best != null)
            emailIntent.setClassName(best.activityInfo.packageName, best.activityInfo.name);
      //  emailIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.jobbe_mail)});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, messageText);
        emailIntent.setType("message/rfc822");

        startActivity(emailIntent);
    }

    private void sendScreenAnalytics(){
        Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
        t.setScreenName("FORNITORE - PROFILO");
        t.send(new HitBuilders.ScreenViewBuilder().build());
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

    private void switchToClient(){
        // Cambio il tipo di utente a client
        user.put("type", "client");
        try{
            user.save();
        }
        catch (ParseException e){
            Toast.makeText(getApplicationContext(),"C\'è stato un errore durante il salvataggio.", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(SupplierProfileActivity.this, HomeClientActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void setRatingBar(RatingBar ratingBar){
        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();

        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.jobbe_ratings),
                PorterDuff.Mode.SRC_ATOP); // for filled stars

        stars.getDrawable(1).setColorFilter(Color.TRANSPARENT,
                PorterDuff.Mode.SRC_ATOP); // for half filled stars
    }

    private void setViews(){
        nameTextView = (TextView) findViewById(R.id.textView39);
        mailTextView = (TextView) findViewById(R.id.supplier_job);
        phoneTextView = (TextView) findViewById(R.id.phonenumber_text);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        priceRateBar = (RatingBar) findViewById(R.id.ratingBar_price);
        timeRateBar = (RatingBar) findViewById(R.id.ratingBar_time);
        qualityRateBar = (RatingBar) findViewById(R.id.ratingBar_quality);
        courtesyRateBar = (RatingBar) findViewById(R.id.ratingBar_courtesy);
        ratingsLayout = (RelativeLayout) findViewById(R.id.ratings_rl);

        setRatingBar(ratingBar);
        setRatingBar(priceRateBar);
        setRatingBar(timeRateBar);
        setRatingBar(qualityRateBar);
        setRatingBar(courtesyRateBar);

        modifyJobLayout = (RelativeLayout) findViewById(R.id.jobData_rl);
        jobNameTextView = (TextView) findViewById(R.id.textView42);
        jobZonesTextView = (TextView) findViewById(R.id.textView43);
        jobImage = (ImageView) findViewById(R.id.job_image);
        toprevNumberTextView = (TextView) findViewById(R.id.ratnumber_tv);

        numberOfReviewsTextView = (TextView) findViewById(R.id.textView_numberofreviews);

        try {
            user = ParseUser.getCurrentUser().fetch();
        } catch (ParseException e) {
            ParseErrorHandler.handleParseError(e, getApplicationContext());
            e.printStackTrace();
            finish();
            return;
        }

        //Set Anagrafica
        nameTextView.setText(user.get("displayName").toString());
        mailTextView.setText(user.get("userEmail").toString());
        phoneTextView.setText("+39 " + user.getUsername());

        this.userPhoto = (ParseFile) user.get("profilePhoto");
        this.userImageView = (RoundedImageView) findViewById(R.id.SuppSettingsImageView);
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


        // Set Job
        modifyJobLayout.setClickable(false);
        ParseQuery query = ParseQuery.getQuery("Jobs");
        query.whereEqualTo("supplierId", user);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List list, ParseException e) {
                if(e != null){
                    ParseErrorHandler.handleParseError(e, getApplicationContext());
                    e.printStackTrace();
                    finish();
                    return;
                }
                else if (list.size() > 0) {
                    jobList = (ArrayList<ParseObject>) list;
                    updateJobView(jobList);
                }
            }
        });

        //setJobPicture();

        // Set Ratings
        ArrayList<HashMap<String, Object>> results = (ArrayList<HashMap<String, Object>>) user.get("averageRatingSet");
        int revNumber = user.getInt("numberOfReviews");
        if (revNumber > 0) {
            ratingBar.setRating((float) user.getDouble("averageRating"));
            toprevNumberTextView.setText(user.getInt("numberOfReviews") + "");

            HashMap<String,Object> ratings = results.get(0);
            double priceRating = objToDouble(ratings.get("price"));
            double timeRating = objToDouble(ratings.get("timing"));
            double qualityRating = objToDouble(ratings.get("quality"));
            double courtesyRating = objToDouble(ratings.get("courtesy"));

            courtesyRateBar.setRating((float) courtesyRating);
            timeRateBar.setRating((float) timeRating);
            qualityRateBar.setRating((float) qualityRating);
            priceRateBar.setRating((float) priceRating);
        }
        else{
            ratingBar.setVisibility(View.GONE);
            toprevNumberTextView.setText("nuovo fornitore");
            ratingsLayout.setVisibility(View.GONE);
        }

        int numberOfReviews = (int) user.get("numberOfReviews");
        int numberOfCompletedWorks = (int) user.get("numberOfWorksCompleted");
        String numberOfReviewsText = Integer.toString(numberOfReviews) + "/" + Integer.toString(numberOfCompletedWorks);
        numberOfReviewsTextView.setText(numberOfReviewsText);

    }

    private void setJobPicture(String iconName){
        /*
        ParseObject jobCategory = (ParseObject) this.jobList.get(0).get("categoryListId");

        try {
            jobCategory.fetch();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String iconName = jobCategory.get("icon").toString().replace("-", "_");
        */
        //String iconName = "arte_icon";
        iconName = iconName.replace("-", "_");
        int resourceId = getResources().getIdentifier(iconName, "drawable", "com.division70.jobbe.jobbe_git");
        this.jobImage.setImageResource(resourceId);
    }

    private void startPPview(){
        String url = getString(R.string.web_view_endpoint) + getString(R.string.privacy);
        Intent intent = new Intent(SupplierProfileActivity.this, WebViewActivity.class);
        intent.putExtra("url",url);
        startActivity(intent);
    }

    private void startTermsView(){
        String url = getString(R.string.web_view_endpoint) + getString(R.string.terms);
        Intent intent = new Intent(SupplierProfileActivity.this, WebViewActivity.class);
        intent.putExtra("url",url);
        startActivity(intent);
    }

    private void logout(){
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                Intent intent = new Intent(SupplierProfileActivity.this, FirstActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    public void onShareClick(){
        Resources resources= getResources();
        String emailSubject = getResources().getString(R.string.invite_emailsubject);
        String chooserTitle = "Invita tramite:";
        String messageText = getResources().getString(R.string.invite_suppliermessagetext) + getResources().getString(R.string.invite_downloadlink);

        Intent emailIntent=new Intent();
        emailIntent.setAction(Intent.ACTION_SEND);
        // Native email client doesn't currently support HTML, but it doesn't hurt to try in case they fix it
        emailIntent.putExtra(Intent.EXTRA_TEXT, messageText);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
        emailIntent.setType("message/rfc822");

        PackageManager pm=getPackageManager();
        Intent sendIntent=new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");


        Intent openInChooser=Intent.createChooser(emailIntent,chooserTitle);

        List<ResolveInfo> resInfo=pm.queryIntentActivities(sendIntent,0);
        List<LabeledIntent> intentList=new ArrayList<LabeledIntent>();
        for(int i=0; i<resInfo.size(); i++) {
            // Extract the label, append it, and repackage it in a LabeledIntent
            ResolveInfo ri = resInfo.get(i);
            String packageName = ri.activityInfo.packageName;
            if (packageName.contains("android.email")) {
                emailIntent.setPackage(packageName);
            } else if (packageName.contains("twitter") || packageName.contains("facebook") || packageName.contains("mms") || packageName.contains("android.gm") || packageName.contains("whatsapp")) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                if (packageName.contains("twitter")) {
                    intent.putExtra(Intent.EXTRA_TEXT, messageText);
                } else if (packageName.contains("facebook")) {
                    // Warning: Facebook IGNORES our text. They say "These fields are intended for users to express themselves. Pre-filling these fields erodes the authenticity of the user voice."
                    // One workaround is to use the Facebook SDK to post, but that doesn't allow the user to choose how they want to share. We can also make a custom landing page, and the link
                    // will show the <meta content ="..."> text from that page with our link in Facebook.
                    intent.putExtra(Intent.EXTRA_TEXT, messageText);
                } else if (packageName.contains("mms")) {
                    intent.putExtra(Intent.EXTRA_TEXT, messageText);
                } else if (packageName.contains("android.gm")) { // If Gmail shows up twice, try removing this else-if clause and the reference to "android.gm" above
                    intent.putExtra(Intent.EXTRA_TEXT, messageText);
                    intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
                    intent.setType("message/rfc822");
                } else if (packageName.contains("whatsapp")) {
                    intent.putExtra(Intent.EXTRA_TEXT, messageText);
                }

                intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
            }
        }


        // convert intentList to array
        LabeledIntent[]extraIntents=intentList.toArray(new LabeledIntent[intentList.size()]);

        openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS,extraIntents);
        startActivity(openInChooser);
    }

    public void modifyData(){
        Intent intent = new Intent(SupplierProfileActivity.this, EditSupplierUserDataActivity.class);
        startActivityForResult(intent, 6516);
        overridePendingTransition(R.anim.pushleft_in, R.anim.pushleft_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 6516) {
            //Ritorno da edit dati anagrafici
            setUserData();
        }
        else if (requestCode == 9234) {
            // Ritorno da modifica job
            if (resultCode == Activity.RESULT_OK) {
                spinnerLayout.setVisibility(View.VISIBLE);
                new saveJobsAsync().execute(data);
                /*
                boolean modifiedPiva = data.getBooleanExtra("modifiedPiva", true);
                String piva = data.getStringExtra("piva");
                Boolean has_piva = data.getBooleanExtra("has_piva", false);
                ArrayList<String> cityList = data.getStringArrayListExtra("cityList");
                ArrayList<ParseObject> zoneList = new ArrayList<>();
                for (String city : cityList) {
                    try {
                        zoneList.add(
                                (ParseObject) ParseQuery.getQuery("Zone")
                                        .whereEqualTo("active", true)
                                        .whereEqualTo("nome", city).find().get(0));
                        Log.i("CITY", city);
                    } catch (ParseException exc) {
                        exc.printStackTrace();
                    }
                }

                ArrayList<ParseObject> toBeRemoved = new ArrayList<>(); //Necessario perchè non posso eliminare elementi dalla lista mentre la sto scorrendo, quindi lo faccio dopo
                for (ParseObject y : jobList) {
                    if (!zoneList.contains((ParseObject) y.get("zoneId"))) {
                        Log.i("Deleting Job","Deleting job in " + ((ParseObject) y.get("zoneId")).getString("nome"));
                        try {
                            y.delete();
                        } catch (ParseException e){
                            e.printStackTrace();
                        }
                        toBeRemoved.add(y);
                    }
                    else{
                        Log.i("Updating Job","Updating job in " + ((ParseObject) y.get("zoneId")).getString("nome"));
                        if (modifiedPiva)
                            if (has_piva) {
                                y.put("piva", piva);
                                y.put("supplierType", "professionist");
                            }
                            else {
                                y.put("piva", "");
                                y.put("supplierType", "hobby");
                            }
                            try {
                                y.save();
                            } catch (ParseException ex2) {
                                ex2.printStackTrace();
                            }
                    }
                }

                for (ParseObject x : toBeRemoved){
                    jobList.remove(x);
                }

                boolean exists;
                for (ParseObject x : zoneList){
                    exists = false;
                    for (ParseObject y : jobList)
                        if (x.equals(y.get("zoneId"))){
                            exists = true;
                            break;
                        }
                    if (!exists) {
                        // Se ho una zona ma non il suo job nella lista, lo devo creare
                        Log.i("Creating Job","Creating job in " + (x.getString("nome")));
                        String PIVA;
                        if (has_piva)
                            PIVA = piva;
                        else
                            PIVA = "";
                        createNewJob(x, PIVA);
                    }
                }



                job = jobList.get(0);

                ParseObject jobListObject = (ParseObject) job.get("jobListId");
                try {
                    jobListObject.fetch();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Map<String,Object> params = new HashMap<>();
                ArrayList<String> zonesId = new ArrayList<>();
                for (ParseObject zone : zoneList) zonesId.add(zone.getObjectId());
                params.put("zones", zonesId);
                params.put("jobId", jobListObject.getObjectId());
                params.put("userId", user.getObjectId());
                try {
                    ParseCloud.callFunction("matchRequestsWithJob", params);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                */

                //updateJobView(jobList);
            }
        }
    }

    private void createNewJob(ParseObject x, String PIVA){
        ParseObject newJob = new ParseObject("Jobs");
        newJob.put("name", job.getString("name"));
        newJob.put("supplierId", user);

        if (!PIVA.equals("")){
            newJob.put("piva", PIVA);
            newJob.put("supplierType", "professionist");
        }
        else
            newJob.put("supplierType", "hobby");

        newJob.put("zoneId", x);
        newJob.put("categoryListId", job.get("categoryListId"));
        newJob.put("jobListId", job.get("jobListId"));
        try{
            newJob.save();
        } catch (ParseException e){
            e.printStackTrace();
        }
        jobList.add(newJob);
    }

    private void setUserData(){
        this.user = ParseUser.getCurrentUser();
        nameTextView.setText((String) user.get("displayName"));
        mailTextView.setText((String) user.get("userEmail"));
        phoneTextView.setText("+39 " + user.getUsername());

        userPhoto = (ParseFile) user.get("profilePhoto");
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
            // PUT PLACEHOLDER
            userImageView.setImageResource(R.drawable.placeholder_client);
        }
    }

    private void modifyJob(){
        ArrayList<String> cityList = new ArrayList<String>();
        for (ParseObject x : jobList){
            try {
                cityList.add(((ParseObject) x.get("zoneId")).fetchIfNeeded().getString("nome"));
            } catch (ParseException e){}
        }
        Intent intent = new Intent(SupplierProfileActivity.this, EditJobActivity.class);
        intent.putExtra("jobName", job.getString("name"));
        intent.putExtra("PIVA", job.getString("piva"));
        intent.putExtra("citylist", cityList);
        startActivityForResult(intent, 9234);
    }

    private void updateJobView(ArrayList<ParseObject> list){
        job = list.get(0);
        try {
            ParseObject jobListObject = ((ParseObject) job.get("jobListId")).fetch();
            String JobName = jobListObject.get("name").toString();
            int numberOfZones = list.size();

            ParseObject category = ((ParseObject) job.get("categoryListId")).fetch();
            String categoryIconName = category.get("icon").toString();

            jobNameTextView.setText(JobName);
            String zoneLabel;
            if (numberOfZones < 2) zoneLabel = " zona selezionata";
            else zoneLabel = " zone selezionate";
            jobZonesTextView.setText(numberOfZones + zoneLabel);
            setJobPicture(categoryIconName);
            modifyJobLayout.setClickable(true);
        }
        catch (ParseException e2){
            e2.printStackTrace();
        }

    }

    private void openArchive(){
        Intent intent = new Intent(SupplierProfileActivity.this, RequestsArchiveActivity.class);
        startActivity(intent);
    }

    private double objToDouble(Object value){
        try{
            return ((double) ((Integer) value).intValue());
        }
        catch (Exception e){
            return (double) value;
        }
    }

    @Override
    public void onBackPressed(){
        backFunction();
    }

    private void backFunction(){
        Boolean fromPush = getIntent().getBooleanExtra("fromPush", false);
        if (!fromPush)
            finish();
        else
            toSupplierHome();
    }

    private void toSupplierHome(){
        Intent intent = new Intent(SupplierProfileActivity.this, HomeSupplierActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private class saveJobsAsync extends AsyncTask<Intent, Void, Void> {

        @Override
        protected Void doInBackground(Intent... par) {
            Intent data = par[0];
            boolean modifiedPiva = data.getBooleanExtra("modifiedPiva", true);
            String piva = data.getStringExtra("piva");
            Boolean has_piva = data.getBooleanExtra("has_piva", false);
            ArrayList<String> cityList = data.getStringArrayListExtra("cityList");
            ArrayList<ParseObject> zoneList = new ArrayList<>();
            for (String city : cityList) {
                try {
                    zoneList.add(
                            (ParseObject) ParseQuery.getQuery("Zone")
                                    .whereEqualTo("active", true)
                                    .whereEqualTo("nome", city).find().get(0));
                    Log.i("CITY", city);
                } catch (ParseException exc) {
                    ParseErrorHandler.handleParseError(exc, getApplicationContext());
                    exc.printStackTrace();

                }
            }

            ArrayList<ParseObject> toBeRemoved = new ArrayList<>(); //Necessario perchè non posso eliminare elementi dalla lista mentre la sto scorrendo, quindi lo faccio dopo
            for (ParseObject y : jobList) {
                if (!zoneList.contains((ParseObject) y.get("zoneId"))) {
                    Log.i("Deleting Job", "Deleting job in " + ((ParseObject) y.get("zoneId")).getString("nome"));
                    try {
                        y.delete();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    toBeRemoved.add(y);
                } else {
                    Log.i("Updating Job", "Updating job in " + ((ParseObject) y.get("zoneId")).getString("nome"));
                    if (modifiedPiva)
                        if (has_piva) {
                            y.put("piva", piva);
                            y.put("supplierType", "professionist");
                        } else {
                            y.put("piva", "");
                            y.put("supplierType", "hobby");
                        }
                    try {
                        y.save();
                    } catch (ParseException ex2) {
                        ex2.printStackTrace();
                    }
                }
            }

            for (ParseObject x : toBeRemoved) {
                jobList.remove(x);
            }

            boolean exists;
            for (ParseObject x : zoneList) {
                exists = false;
                for (ParseObject y : jobList)
                    if (x.equals(y.get("zoneId"))) {
                        exists = true;
                        break;
                    }
                if (!exists) {
                    // Se ho una zona ma non il suo job nella lista, lo devo creare
                    Log.i("Creating Job", "Creating job in " + (x.getString("nome")));
                    String PIVA;
                    if (has_piva)
                        PIVA = piva;
                    else
                        PIVA = "";
                    createNewJob(x, PIVA);
                }
            }


            job = jobList.get(0);

            ParseObject jobListObject = (ParseObject) job.get("jobListId");
            try {
                jobListObject.fetch();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Map<String, Object> params = new HashMap<>();
            ArrayList<String> zonesId = new ArrayList<>();
            for (ParseObject zone : zoneList) zonesId.add(zone.getObjectId());
            params.put("zones", zonesId);
            params.put("jobId", jobListObject.getObjectId());
            params.put("userId", user.getObjectId());
            try {
                ParseCloud.callFunction("matchRequestsWithJob", params);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(Void v) {
            modifyJobLayout.setClickable(false);
            updateJobView(jobList);
            spinnerLayout.setVisibility(View.GONE);
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
