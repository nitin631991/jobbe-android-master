package com.division70.jobbe.jobbe_git;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by giorgio on 22/10/15.
 */
public class PhoneNumberActivity extends Activity {

    private EditText phoneNumber;
    private Button sendCode;
    private RelativeLayout spinnerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phonenumber_layout);

        sendScreenAnalytics();

        this.phoneNumber = (EditText) findViewById(R.id.phone_number_field);
        this.phoneNumber.requestFocus();
        this.sendCode = (Button) findViewById(R.id.send_SMS);
        this.spinnerLayout = (RelativeLayout) findViewById(R.id.loadingSpinnerLayout);
        this.sendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendCode.setEnabled(false);
                spinnerLayout.setVisibility(View.VISIBLE);
                new checkAsync(phoneNumber.getText().toString()).execute();
            }
        });

        setUpActionBar();
    }

    private void setUpActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        CustomActionbar customActionbar = new CustomActionbar(this);
        customActionbar.setTitle("Inserisci numero di telefono");
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
        t.setScreenName("SIGNUP - NUMERO TELEFONICO");
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private boolean userExists(String username){
        Log.i("CHECK username", username);
        ParseUser x;
        try {
            x = ParseUser.getQuery().whereEqualTo("username", username).getFirst();
            if (x == null){
                //Toast.makeText(getApplicationContext(), "Utente già registrato",
                //        Toast.LENGTH_LONG).show();
                return false;
            }
        } catch (ParseException e) {
            Log.i("EXCEPTION!!!", e.getMessage());
            if (e.getCode() == ParseException.OBJECT_NOT_FOUND)
                return false;
            e.printStackTrace();
        }
        Log.i("userExists", "true");
        //Toast.makeText(getApplicationContext(), "Il numero inserito risulta già presente, fai il login per accedere al tuo profilo Jobbe", Toast.LENGTH_LONG).show();
        return true;
    }

    private void startConfirmationCode(){
        Log.i("startConfirmationCode", "started");
        Map<String, String> phoneInfo = new HashMap<>();
        phoneInfo = getPhoneInfo(phoneNumber.getText().toString());

        //Send SMS
        final Map<String, String> finalPhoneInfo = phoneInfo;
        ParseCloud.callFunctionInBackground("sendSmsVerification ", phoneInfo, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e != null || o == null) {
                    Log.i("e", e.getMessage());
                    if(e.getMessage().contains("not a valid phone number"))
                        Toast.makeText(getApplicationContext(), "Il numero inserito non è valido", Toast.LENGTH_LONG).show();
                    sendCode.setEnabled(true);
                    spinnerLayout.setVisibility(View.GONE);
                    return; //Disabilita per inserire numeri "falsi"
                    //Toast.makeText(getApplicationContext(), "Il numero inserito risulta già presente, fai il login per accedere al tuo profilo Jobbe", Toast.LENGTH_LONG).show();
                }

                //else {
                    //Toast.makeText(getApplicationContext(), "Codice \"segreto\": " + phoneInfo.get("verificationCode"), Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(PhoneNumberActivity.this, ConfirmationCodeActivity.class);
                    intent.putExtra("phoneNumber", finalPhoneInfo.get("phoneNumber"));
                    intent.putExtra("verificationCode", finalPhoneInfo.get("verificationCode"));
                    intent.putExtra("displayName", getIntent().getStringExtra("displayName"));
                    intent.putExtra("email", getIntent().getStringExtra("email"));
                    intent.putExtra("anno", getIntent().getStringExtra("anno"));
                    intent.putExtra("gender", getIntent().getStringExtra("gender"));
                    intent.putExtra("type", getIntent().getStringExtra("type"));
                    intent.putExtra("job", getIntent().getStringExtra("job"));
                    intent.putExtra("suppPIVA", getIntent().getStringExtra("suppPIVA"));
                    intent.putExtra("suppCF", getIntent().getStringExtra("suppCF"));
                    intent.putExtra("suppDitta", getIntent().getStringExtra("suppDitta"));
                    intent.putExtra("suppDesc", getIntent().getStringExtra("suppDesc"));
                    intent.putExtra("cityList", getIntent().getStringArrayListExtra("cityList"));
                    intent.putExtra("suppLatitude", getIntent().getDoubleExtra("suppLatitude", 0));
                    intent.putExtra("suppLongitude", getIntent().getDoubleExtra("suppLongitude", 0));
                    startActivity(intent);
                    overridePendingTransition(R.anim.pushleft_in, R.anim.pushleft_out);
                    spinnerLayout.setVisibility(View.GONE);
                //}
            }

            ;
        });
    }

    private Map<String, String> getPhoneInfo(String phoneNumber){
        Map<String, String> phoneInfo = new HashMap<>();
        phoneInfo.put("phoneNumber", phoneNumber);
        phoneInfo.put("verificationCode", codeGenerator());

        return phoneInfo;
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


    @Override
    protected void onResume() {
        super.onResume();
        JobbeApplication.activityResumed();
        Utils.deleteNotifications(getApplicationContext());
        sendCode.setEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        JobbeApplication.activityPaused();
    }

    private class checkAsync extends AsyncTask<Void, Void, Integer> {

        private String number;

        public checkAsync(String number){
            this.number = number;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            if (this.number.isEmpty())
                return 1;
            else if (!Utils.checkConnection(getApplicationContext(), true))
                return 2;
            else if(userExists(this.number))
                return 3;
            return 0; //OK
        }

        @Override
        protected void onPostExecute(Integer code) {
            switch(code){
                case 0:
                    startConfirmationCode();
                    break;
                case 1:
                    Toast.makeText(getApplicationContext(), getString(R.string.empty_phone_number),
                            Toast.LENGTH_LONG).show();
                    sendCode.setEnabled(true);
                    spinnerLayout.setVisibility(View.GONE);
                    break;
                case 2:
                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.offline_default_alert), Toast.LENGTH_SHORT).show();
                    sendCode.setEnabled(true);
                    spinnerLayout.setVisibility(View.GONE);
                    break;
                case 3:
                    Toast.makeText(getApplicationContext(), "Il numero inserito risulta già presente, fai il login per accedere al tuo profilo Jobbe", Toast.LENGTH_LONG).show();
                    sendCode.setEnabled(true);
                    spinnerLayout.setVisibility(View.GONE);
                    break;
            }
        }

    }
}
