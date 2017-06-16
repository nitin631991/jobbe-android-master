package com.division70.jobbe.jobbe_git;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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
import com.parse.GetDataCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by osvy on 30/10/15.
 */
public class SettingsClientActivity extends Activity {
    private String displayName;
    private String email;
    private String phoneNumber;
    private ParseFile photo;
    private ParseUser user;

    private TextView nameTextView;
    private TextView mailTextView;
    private TextView phoneTextView;
    private ImageView imageView;

    private String rootUrl = "https://jobbe-dev.parseapp.com/";
    private String terms = "terms";
    private String privacy = "privacy";
    private RelativeLayout spinnerLayout;

    private Button switchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.settingsclient_layout);

            sendScreenAnalytics();


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

            Button logoutButton = (Button) findViewById(R.id.button6);
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
                    //share();
                }
            });

            RelativeLayout contattaciButton = (RelativeLayout) findViewById(R.id.contattaci);
            contattaciButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    contattaJobbe();
                }
            });

            this.switchButton = (Button) findViewById(R.id.button7);
            switchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switchToSupplier();
                }
            });

            RelativeLayout userDataLayout = (RelativeLayout) findViewById(R.id.userDataLayout);
            userDataLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    changeUserData();
                }
            });

            RoundedImageView userPic = (RoundedImageView) findViewById(R.id.ClientSettingsImageView);
            userPic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    changeUserData();
                }
            });

            RelativeLayout archiveButton = (RelativeLayout) findViewById(R.id.archive_layout);
            archiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goToArchive();
                }
            });

            RelativeLayout editClient = (RelativeLayout) findViewById(R.id.settings_layout);
            editClient.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    changeUserData();
                }
            });

            spinnerLayout = (RelativeLayout) findViewById(R.id.loadingSpinnerLayout);

            setUserData();
            Log.i("Continua", "Io vado avanti comunque");
            setUpActionBar();
        } catch (JobbeSessionException e){
            Log.i("Exception caught", e.getMessage());
            finish();
            return;
        }
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
                finish();
            }
        });
    }

    private void sendScreenAnalytics(){
        Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
        t.setScreenName("CLIENTE - PROFILO");
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void contattaJobbe(){
        String emailSubject = "Richiesta assistenza\nID:" + ParseUser.getCurrentUser().getObjectId();
        String chooserTitle = "Invita tramite:";
        String messageText = "inviato da Android";


        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");

        final PackageManager pm = getPackageManager();
        final List<ResolveInfo> matches = pm.queryIntentActivities(i, 0);
        ResolveInfo best = null;
        for (final ResolveInfo info : matches)
            if (info.activityInfo.packageName.endsWith(".gm") ||
                    info.activityInfo.name.toLowerCase().contains("gmail")) best = info;
        if (best != null)
            i.setClassName(best.activityInfo.packageName, best.activityInfo.name);
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{getString(R.string.jobbe_mail)});
        i.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
        i.putExtra(Intent.EXTRA_TEXT   ,messageText);
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(SettingsClientActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }

//        Intent emailIntent = new Intent();
//        emailIntent.setAction(Intent.ACTION_SEND);
//        emailIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
//        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.jobbe_mail)});
//        emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
//        emailIntent.putExtra(Intent.EXTRA_TEXT, messageText);
//        emailIntent.setType("message/rfc822");
//
//        startActivity(emailIntent);
    }

    private void startPPview(){
        String url = getString(R.string.web_view_endpoint) + getString(R.string.privacy);
        Intent intent = new Intent(SettingsClientActivity.this, WebViewActivity.class);
        intent.putExtra("url", url);
        startActivity(intent);
    }

    private void startTermsView(){
        String url = getString(R.string.web_view_endpoint) + getString(R.string.terms);
        Intent intent = new Intent(SettingsClientActivity.this, WebViewActivity.class);
        intent.putExtra("url", url);
        startActivity(intent);
    }

    private void logout(){
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                Intent intent = new Intent(SettingsClientActivity.this, FirstActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    private void share() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Io sto utilizzando Jobbe per trovare i professionisti quando ne ho bisogno. Provala gratuitamente: \nwww.google.it");
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Invita tramite"));
    }

    public void onShareClick(){
        Resources resources=getResources();
        String emailSubject = getResources().getString(R.string.invite_emailsubject);
        String chooserTitle = "Invita tramite:";
        String messageText = getResources().getString(R.string.invite_clientmessagetext) + getResources().getString(R.string.invite_downloadlink);
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

    private void changeUserData(){
        Intent intent = new Intent(SettingsClientActivity.this, EditClientUserDataActivity.class);
        startActivityForResult(intent, 6516);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //if (requestCode == 6516 && resultCode != Activity.RESULT_CANCELED) {
        Log.i("ResultCode", resultCode + "");
        Log.i("RequestCode", requestCode + "");
        if (requestCode == 6516 && resultCode == Activity.RESULT_OK) {
            Log.i("StartUpdate", "resultCode " + resultCode);
            spinnerLayout.setVisibility(View.VISIBLE);
            try {
                setUserData();
            } catch (JobbeSessionException e) {
                finish();
                return;
            }
            spinnerLayout.setVisibility(View.GONE);
        }
    }

    private void setUserData() throws JobbeSessionException {

        try {
            this.user = ParseUser.getCurrentUser().fetch();
        } catch (ParseException e) {
            //ParseErrorHandler.handleParseError(e, getApplicationContext());
            ParseErrorHandler.handleParseError(e, this);
            Log.i("INVALID SESSION", "error handled, printstacktrace");
            e.printStackTrace();
            Log.i("INVALID SESSION", "end catch");
            throw new JobbeSessionException();
            //return;
        }

        if(this.user.getBoolean("isSupplier"))
            this.switchButton.setText("Passa a fornitore");
        else
            this.switchButton.setText("Diventa fornitore");

        this.displayName = (String) user.get("displayName");
        this.email = (String) user.get("userEmail");
        this.photo = (ParseFile) user.get("profilePhoto");
        this.phoneNumber = user.getUsername();

        this.nameTextView = (TextView) findViewById(R.id.ClientSettingsNameTextView);
        nameTextView.setText(displayName);
        this.mailTextView = (TextView) findViewById(R.id.ClientSettingsMailTextView);
        mailTextView.setText(email);
        this.phoneTextView = (TextView) findViewById(R.id.ClientSettingsPhoneTextView);
        phoneTextView.setText(phoneNumber);

        this.imageView = (ImageView) findViewById(R.id.ClientSettingsImageView);
        if (photo != null) {
            Log.i("setData", "photo not null");
            Picasso.with(this).load(photo.getUrl()).into(imageView);
//            photo.getDataInBackground(new GetDataCallback() {
//                @Override
//                public void done(byte[] data, ParseException e) {
//                    if (e == null) {
//                        Log.i("setdata", "set ImageView");
//                        imageView.setImageDrawable(null);
//                        Log.i("data length", data.length + "");
//                        int scaleStep = getResources().getInteger(R.integer.photo_scale_step);
//                        int scaleFactor = data.length / scaleStep;
//                        Log.i("scaleFactor", scaleFactor + "");
//                        BitmapFactory.Options options=new BitmapFactory.Options();// Create object of bitmapfactory's option method for further option use
//                        options.inPurgeable = true; // inPurgeable is used to free up memory while required
//                        if(scaleFactor > 1)
//                            options.inSampleSize = scaleFactor;
//                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
//                        imageView.setImageBitmap(bmp);
//                    } else {
//                    }
//                }
//            });
        } else {
            // PUT PLACEHOLDER
            imageView.setImageResource(R.drawable.placeholder_client);
        }
    }

    private void switchToSupplier(){
        final boolean isSupplier = ParseUser.getCurrentUser().getBoolean("isSupplier");
        Log.i("isSupplier", isSupplier + "");
        chooseSwitchRoute(isSupplier);
    }

    private void chooseSwitchRoute(boolean originalIsSupplier){
        if (originalIsSupplier){
            Log.i("isSupplier", originalIsSupplier + "");
            //L'utente è già stato supplier, lo mando alla home giusta
                Intent intent = new Intent(SettingsClientActivity.this, HomeSupplierActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
        }
        else {
            //L'utente non è mai stato supplier, lo mando alla registrazione del supplier
            Intent intent = new Intent(SettingsClientActivity.this, JobChooserActivity.class);
            intent.putExtra("switchFromClient", true);
            intent.putExtra("purpose","supplierSignup");
            startActivity(intent);
        }
    }

    private void goToArchive(){
        Intent intent = new Intent(SettingsClientActivity.this, RequestsClientArchiveActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
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
