package com.division70.jobbe.jobbe_git;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by giorgio on 22/10/15.
 */
public class SwitchRoleActivity extends Activity {

    private RelativeLayout customer_button_signup;
    private RelativeLayout supplier_button_signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.switchrole_layout);

        sendScreenAnalytics();

        this.customer_button_signup = (RelativeLayout) findViewById(R.id.client_layout);
        this.supplier_button_signup = (RelativeLayout) findViewById(R.id.supplier_layout);

        this.customer_button_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SwitchRoleActivity.this, CreateCustomerActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.pushleft_in, R.anim.pushleft_out);
            }
        });

        this.supplier_button_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SwitchRoleActivity.this, JobChooserActivity.class);
                intent.putExtra("purpose", "supplierSignup");
                startActivity(intent);
                overridePendingTransition(R.anim.pushleft_in, R.anim.pushleft_out);
            }
        });

        setUpActionBar();
        alertTemporary();
    }

    private void alertTemporary(){
        new AlertDialog.Builder(SwitchRoleActivity.this)
                .setTitle("Attenzione")
                .setCancelable(false)
                .setMessage("Jobbe è attualmente attivo solo nella Zona di Parma. Ti avviseremo quando saranno attivate altre zone")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void setUpActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        CustomActionbar customActionbar = new CustomActionbar(this);
        customActionbar.setTitle("");
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
        t.setScreenName("SIGNUP - SWITCH");
        t.send(new HitBuilders.ScreenViewBuilder().build());
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
