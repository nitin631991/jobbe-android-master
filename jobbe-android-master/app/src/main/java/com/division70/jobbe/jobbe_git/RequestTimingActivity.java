package com.division70.jobbe.jobbe_git;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;

/**
 * Created by giorgio on 03/11/15.
 */
public class RequestTimingActivity extends Activity {

    private Button customDateButton;
    private String defTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.requesttiming_layout);

        sendScreenAnalytics();

        this.init();
        setUpActionBar();
    }

    private void setUpActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        CustomActionbar customActionbar = new CustomActionbar(this);
        customActionbar.setTitle("Quando");
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
        t.setScreenName("CLIENTE - CREA RICHIESTA - SELEZIONE URGENZA");
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void init(){
        this.customDateButton = (Button) findViewById(R.id.custom_date_button);
        this.defTime = getIntent().getStringExtra("defTime");
        setListeners();
    }

    private void setListeners(){
        this.customDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RequestTimingActivity.this, CustomDateActivity.class);
                intent.putExtra("defTime", defTime);
                startActivityForResult(intent, 8888);
                overridePendingTransition(R.anim.pushleft_in, R.anim.pushleft_out);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == 8888 && resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK, data);
            finish();
        }
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
