package com.division70.jobbe.jobbe_git;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.arturogutierrez.Badges;
import com.github.arturogutierrez.BadgesNotSupportedException;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class MainActivity extends Activity {

    private CheckBox chckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sendScreenAnalytics();
        chckBox = (CheckBox) findViewById(R.id.chckBox);


        /*
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_client_id));
        */
        Button signup = (Button) findViewById(R.id.signup_button);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chckBox.isChecked()){
                    Intent intent = new Intent(MainActivity.this, SwitchRoleActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.pushleft_in, R.anim.pushleft_out);
                }else{
                    Toast.makeText(MainActivity.this, getString(R.string.select_checkbox), Toast.LENGTH_LONG).show();
                }

            }
        });

        Button login = (Button) findViewById(R.id.login_button);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LoginPhoneActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.pushleft_in, R.anim.pushleft_out);
            }
        });

        RelativeLayout changePhoneNumber = (RelativeLayout) findViewById(R.id.change_number_layout);
        changePhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ChangePhoneNumberActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.pushleft_in, R.anim.pushleft_out);
            }
        });

        setDisclaimer();
    }

    private void setDisclaimer(){
        TextView disclaimer1 = (TextView) findViewById(R.id.disclaimer_line1);
        disclaimer1.setText(getResources().getString(R.string.first_page_disclaimer_line_1));
        TextView disclaimer2 = (TextView) findViewById(R.id.disclaimer_line2);
        disclaimer2.setMovementMethod(LinkMovementMethod.getInstance());
        //String text = getResources().getString(R.string.first_page_disclaimer_line_2);
        //String text = "nostri <a style=\"color:blue\" href=\"https://jobbe-dev.parseapp.com/terms\">Termini del servizio</a> e la nostra <a style=\"color:blue; text-decoration: none\" href=\"https://jobbe-dev.parseapp.com/privacy\">Privacy Policy</a></p>";
        String text = "nostri <a style=\"color:blue\" href=\"http://www.myjobbe.com/terms\">Termini del servizio</a> e la nostra <a style=\"color:blue; text-decoration: none\" href=\"http://www.myjobbe.com/privacy\">Privacy Policy</a></p>";
        //disclaimer2.setText(Html.fromHtml(text));

        //Rimuovo gli underline dai link
        Spannable s = (Spannable) Html.fromHtml(text);
        for (URLSpan u: s.getSpans(0, s.length(), URLSpan.class)) {
            s.setSpan(new UnderlineSpan() {
                public void updateDrawState(TextPaint tp) {
                    tp.setUnderlineText(false);
                }
            }, s.getSpanStart(u), s.getSpanEnd(u), 0);
        }
        disclaimer2.setText(s);
    }

    private void sendScreenAnalytics(){
        Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
        t.setScreenName("JOBBE - LANDING");
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }


    @Override
    protected void onResume() {
        super.onResume();
        try {
            Badges.removeBadge(this);
            Badges.setBadge(this, 0);
        } catch (BadgesNotSupportedException badgesNotSupportedException) {
            Log.d("wydtws", badgesNotSupportedException.getMessage());
        }

        JobbeApplication.activityResumed();
        Utils.deleteNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();
        JobbeApplication.activityPaused();
    }
}
