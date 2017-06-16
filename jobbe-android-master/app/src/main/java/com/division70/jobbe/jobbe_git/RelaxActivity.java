package com.division70.jobbe.jobbe_git;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.parse.ParseException;
import com.parse.ParseObject;

/**
 * Created by giorgio on 20/11/15.
 */
public class RelaxActivity extends Activity{

    private RelativeLayout mainLayout;
    private ImageView icon;
    private Button button;
    private String iconPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.relax_layout);

        sendScreenAnalytics();

        init();
        setListeners();
    }

    private void sendScreenAnalytics(){
        Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
        t.setScreenName("CLIENTE - CREA RICHIESTA - MODALE");
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void init(){
        this.mainLayout = (RelativeLayout) findViewById(R.id.main_layout);
        this.icon = (ImageView) findViewById(R.id.icon);
        this.button = (Button) findViewById(R.id.relax_button);

        this.iconPath = getIntent().getStringExtra("iconPath");
        setRequestPicture();
        setBackground();
    }

    private void setBackground(){
        this.mainLayout.setBackgroundColor(Color.parseColor(getIntent().getStringExtra("color")));
    }

    private void setListeners(){
        this.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toClientHome();
            }
        });
    }

    private void toClientHome(){
        Intent intent = new Intent(RelaxActivity.this, HomeClientActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setRequestPicture(){
        int resourceId = getResources().getIdentifier(this.iconPath, "drawable", "com.division70.jobbe.jobbe_git");
        this.icon.setImageResource(resourceId);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(RelaxActivity.this, HomeClientActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
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
