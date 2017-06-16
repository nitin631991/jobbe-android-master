package com.division70.jobbe.jobbe_git;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by osvy on 24/11/15.
 */
public class GpsAlertActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gpsalert_layout);

        sendScreenAnalytics();

        Button confirmButton = (Button) findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(Activity.RESULT_OK);
                finish();
            }
        });

        TextView title = (TextView) findViewById(R.id.request_title);
        TextView desc = (TextView) findViewById(R.id.gps_desc);

        Boolean isClient = getIntent().getBooleanExtra("isClient", true);
        if (isClient){
            title.setText(R.string.gpsalert_client_title);
            desc.setText(R.string.gpsalert_client_desc);
        }
        else {
            title.setText(R.string.gpsalert_supplier_title);
            desc.setText(R.string.gpsalert_supplier_desc);
        }
    }

    private void sendScreenAnalytics(){
        Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
        t.setScreenName("SIGNUP - ALERT LOCALIZZAZIONE");
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

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

}
