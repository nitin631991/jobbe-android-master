package com.division70.jobbe.jobbe_git;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.parse.FindCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
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
public class LoginPhoneActivity extends Activity {
    private EditText phoneNumberEditText;
    private Button sendCode;
    private String phonenumber;
    private RelativeLayout spinnerLayout;

    private Map<String,String> phoneinfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phonenumber_layout);

        sendScreenAnalytics();

        this.phoneNumberEditText = (EditText) findViewById(R.id.phone_number_field);
        this.phoneNumberEditText.requestFocus();
        this.spinnerLayout = (RelativeLayout) findViewById(R.id.loadingSpinnerLayout);
        this.sendCode = (Button) findViewById(R.id.send_SMS);
        this.sendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendCode.setEnabled(false);
                spinnerLayout.setVisibility(View.VISIBLE);
                new checkAsync(phoneNumberEditText.getText().toString()).execute();

/*
                if(phoneNumberEditText.getText().toString().isEmpty())
                    Toast.makeText(getApplicationContext(), getString(R.string.empty_phone_number),
                            Toast.LENGTH_LONG).show();
                else if(!userExists(phoneNumberEditText.getText().toString())){
                    Toast.makeText(getApplicationContext(), "Utente non registrato",
                            Toast.LENGTH_LONG).show();
                }
                else {
                    phonenumber = phoneNumberEditText.getText().toString();
                    phoneinfo = getPhoneInfo(phonenumber);

                    //Send SMS
                    ParseCloud.callFunctionInBackground("loginSmsAuthentication ", phoneinfo); //Disable in debug and look

                    Intent intent = new Intent(LoginPhoneActivity.this, LoginCodeActivity.class);
                    intent.putExtra("phoneNumber", phonenumber);
                    intent.putExtra("verificationCode", phoneinfo.get("verificationCode"));
                    startActivity(intent);
                    overridePendingTransition(R.anim.pushleft_in, R.anim.pushleft_out);
                }*/
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
        t.setScreenName("LOGIN - NUMERO TELEFONO");
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private boolean userExists(String username){
        Log.i("CHECK username", username);
        ParseUser x;
        try {
            x = ParseUser.getQuery().whereEqualTo("username", username).getFirst();
            if (x == null)
                return false;
        } catch (ParseException e) {
            Log.i("EXCEPTION!!!", e.getMessage());
            if (e.getCode() == ParseException.OBJECT_NOT_FOUND)  //NON ESISTE NESSUN UTENTE, MA LA QUERY È ANDATA A BUON FINE
                return false;
            e.printStackTrace();
        }
        return true;
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        JobbeApplication.activityPaused();
    }

    private void startConfirmationCode(){
        phonenumber = phoneNumberEditText.getText().toString();
        phoneinfo = getPhoneInfo(phonenumber);

        //Send SMS
        ParseCloud.callFunctionInBackground("loginSmsAuthentication ", phoneinfo); //Disable in debug and look

        Intent intent = new Intent(LoginPhoneActivity.this, LoginCodeActivity.class);
        intent.putExtra("phoneNumber", phonenumber);
        intent.putExtra("verificationCode", phoneinfo.get("verificationCode"));
        startActivity(intent);
        overridePendingTransition(R.anim.pushleft_in, R.anim.pushleft_out);
        sendCode.setEnabled(true);
        spinnerLayout.setVisibility(View.GONE);
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
            else if(!userExists(this.number))
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
                    Toast.makeText(getApplicationContext(), "Utente non registrato", Toast.LENGTH_LONG).show();
                    sendCode.setEnabled(true);
                    spinnerLayout.setVisibility(View.GONE);
                    break;
            }
        }

    }
}
