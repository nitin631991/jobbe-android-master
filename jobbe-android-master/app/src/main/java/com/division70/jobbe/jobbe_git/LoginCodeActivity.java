package com.division70.jobbe.jobbe_git;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.parse.LogInCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by osvy on 28/10/15.
 */
public class LoginCodeActivity extends Activity {
    private String phoneNumber;
    private EditText codeEditText;
    private Button confirmButton;
    private String verificationCode;
    private String inputVerificationCode;

    private TextView sendagainTextview;
    private TextView timerTextView;
    private TextView phoneRecapView;
    private TextView modifyPhoneTextView;
    private CountDownTimer timer;
    private int secs;
    private int mins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirmationcode_layout);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("jobbe_sms_received"));

        sendScreenAnalytics();

        /*
        //Tolgo il timer dal layout
        TextView timerTitle = (TextView) findViewById(R.id.textView39);
        timerTitle.setVisibility(View.GONE);
        TextView timerTextView = (TextView) findViewById(R.id.timer_textView);
        timerTextView.setVisibility(View.GONE);
        */

        timerSetup();

        phoneNumber = getIntent().getStringExtra("phoneNumber");
        verificationCode = getIntent().getStringExtra("verificationCode");
        Log.i("verificationCode", verificationCode);

        this.phoneRecapView = (TextView) findViewById(R.id.phone_textview);
        phoneRecapView.setText("+39 " + phoneNumber);

        this.codeEditText = (EditText) findViewById(R.id.code_field);
        this.codeEditText.requestFocus();

        this.confirmButton = (Button) findViewById(R.id.confirm);
        this.confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (codeEditText.getText().toString().isEmpty())
                    Toast.makeText(getApplicationContext(), getString(R.string.empty_code_number),
                            Toast.LENGTH_LONG).show();
                else {
                    inputVerificationCode = codeEditText.getText().toString();
                    if (inputVerificationCode == verificationCode){}
                    ParseUser.logInInBackground(phoneNumber, inputVerificationCode, new LogInCallback() {
                        public void done(ParseUser user, ParseException e) {
                            if (user != null) {
                                //LOGGATO
                                setParseInstallation();
                                Intent intent = selectIntent(user);
                                startActivity(intent);
                            } else {
                                //NON LOGGATO
                                Toast.makeText(getApplicationContext(), "Login errato",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

        sendagainTextview = (TextView) findViewById(R.id.sendagain_textView);
        sendagainTextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCodeAgain();
            }
        });

        modifyPhoneTextView = (TextView) findViewById(R.id.modify_link);
        modifyPhoneTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        setUpActionBar();
    }

    private void setUpActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        CustomActionbar customActionbar = new CustomActionbar(this);
        customActionbar.setTitle("Inserisci codice di conferma");
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

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String code = intent.getStringExtra("user_code");

            if(!code.isEmpty())
                codeEditText.setText(code);
        }
    };

    private void sendScreenAnalytics(){
        Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
        t.setScreenName("LOGIN - CODICE VERIFICA");
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private String codeGenerator(){
        List<Integer> numbers = new ArrayList<>();
        for(int i = 0; i < 10; i++){
            numbers.add(i);
        }

        Collections.shuffle(numbers);

        String result = "";
        for(int i = 0; i < 4; i++){
            result += numbers.get(i).toString();
        }

        return result;
    }

    private Intent selectIntent(ParseUser user){
        String type = (String) user.get("type");
        Intent intent = null;
        Log.i("type", type);
        if (type.equals("supplier")){
            intent = new Intent(LoginCodeActivity.this, HomeSupplierActivity.class);
        }
        else {
            intent = new Intent(LoginCodeActivity.this, HomeClientActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
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
            ParseErrorHandler.handleParseError(e, getApplicationContext());
        }
        return 0;
    }

    private void timerSetup() {
        //Timer setup (3 mins)
        timerTextView = (TextView) findViewById(R.id.timer_textView);
        this.timer = new CountDownTimer(180000, 1000) {
            @Override
            public void onTick(long l) {
                //Cambia label timer
                secs = (int) l / 1000;
                mins = secs / 60;
                secs = secs % 60;
                timerTextView.setText(Integer.toString(mins) + ":" + String.format("%02d", secs));
            }

            @Override
            public void onFinish() {
                //Annulla codice precedente, possibile invio nuovo codice
                timerTextView.setText("Terminato");
                verificationCode = null;

                showSendAgainLink();

            }
        };
        this.timer.start();
    }

    private void showSendAgainLink(){
        this.timerTextView.setVisibility(View.GONE);
        this.sendagainTextview.setVisibility(View.VISIBLE);
    }

    private void hideSendAgainLink(){
        this.timerTextView.setVisibility(View.VISIBLE);
        this.sendagainTextview.setVisibility(View.GONE);
    }

    private void sendCodeAgain(){
        verificationCode = codeGenerator();
        Map<String, String> phoneInfo = new HashMap<>();
        phoneInfo.put("phoneNumber", phoneNumber);
        phoneInfo.put("verificationCode", verificationCode);
        ParseCloud.callFunctionInBackground("loginSmsAuthentication ", phoneInfo);
        Toast.makeText(getApplicationContext(),"La richiesta di un nuovo codice è stata inviata",Toast.LENGTH_LONG).show();
        Log.i("VER_CODE", verificationCode);
        this.timer.cancel();
        this.timer.start();
        hideSendAgainLink();
    }

    private void setParseInstallation(){
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        ParseUser user = ParseUser.getCurrentUser();
        if(user!=null) installation.put("userId",user);
        installation.saveInBackground();
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
