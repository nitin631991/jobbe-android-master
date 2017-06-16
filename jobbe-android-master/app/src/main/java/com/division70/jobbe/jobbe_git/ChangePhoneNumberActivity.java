package com.division70.jobbe.jobbe_git;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by giorgio on 29/11/15.
 */
public class ChangePhoneNumberActivity extends Activity {

    private EditText oldNumber;
    private EditText newNumber;
    private Button endButton;

    private String webURL = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changephonenumber_layout);

        init();

        setUpActionBar();
    }

    private void setUpActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        CustomActionbar customActionbar = new CustomActionbar(this);
        customActionbar.setTitle("Recupera account");
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

    private void init(){
        this.webURL = getString(R.string.web_view_endpoint);
        this.oldNumber = (EditText) findViewById(R.id.old_phonenumber);
        this.newNumber = (EditText) findViewById(R.id.new_phonenumber);
        this.endButton = (Button) findViewById(R.id.end_button);

        this.oldNumber.requestFocus();

        this.endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePhoneNumber();
            }
        });
    }

    private void changePhoneNumber(){

        String oldNumber = this.oldNumber.getText().toString();
        String newNumber = this.newNumber.getText().toString();

        HashMap<String, Object> request = new HashMap<>();
        request.put("oldPhoneNumber", oldNumber);
        request.put("newPhoneNumber", newNumber);
        request.put("webURL", this.webURL);
        ParseCloud.callFunctionInBackground("userRecovery", request, new FunctionCallback() {
            @Override
            public void done(Object o, Throwable throwable) {
                if (throwable != null) {
                    throwable.printStackTrace();
                    new AlertDialog.Builder(ChangePhoneNumberActivity.this)
                            .setTitle("Errore")
                            .setMessage("Si è verificato un errore, riprova.")
                            .setPositiveButton("Ok", null).create().show();
                } else {
                    Intent intent = new Intent(ChangePhoneNumberActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }

            @Override
            public void done(Object o, ParseException e) {
                if (e != null) {
                    e.printStackTrace();
                    new AlertDialog.Builder(ChangePhoneNumberActivity.this)
                            .setTitle("Errore")
                            .setMessage("Si è verificato un errore, riprova.")
                            .setPositiveButton("Ok", null).create().show();
                }
                else {
                    Intent intent = new Intent(ChangePhoneNumberActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        });
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
