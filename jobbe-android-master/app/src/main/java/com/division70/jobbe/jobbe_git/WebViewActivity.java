package com.division70.jobbe.jobbe_git;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

/**
 * Created by osvy on 30/10/15.
 */
public class WebViewActivity extends Activity {

    private String url;

    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        this.url = getIntent().getStringExtra("url");

        WebView webview = new WebView(this);
        setContentView(webview);
        webview.setWebViewClient(new WebViewClient());
        webview.loadUrl(this.url);
        setUpActionBar();
    }

    private void setUpActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        CustomActionbar customActionbar = new CustomActionbar(this);
        setActionbarTitle(customActionbar);
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

    private void setActionbarTitle(CustomActionbar actionbar){

        String[] urlList = {getString(R.string.web_view_endpoint) + getString(R.string.faq_cliente),
                getString(R.string.web_view_endpoint) + getString(R.string.faq_fornitore),
                getString(R.string.web_view_endpoint) + getString(R.string.terms),
                getString(R.string.web_view_endpoint) + getString(R.string.privacy)};

        int index = -1;
        for(int i = 0; i < urlList.length; i++){
            if(this.url.equals(urlList[i])){
                index = i;
                break;
            }
        }

        switch(index){
            case 0:
                actionbar.setTitle("FAQs");
                sendScreenAnalytics("CLIENTE - FAQ");
                break;
            case 1:
                actionbar.setTitle("FAQs");
                sendScreenAnalytics("FORNITORE - FAQ");
                break;
            case 2:
                actionbar.setTitle("Terms");
                sendScreenAnalytics("GENERICHE - TERMS");
                break;
            case 3:
                actionbar.setTitle("Privacy Policy");
                sendScreenAnalytics("GENERICHE - POLICY");
                break;
            case -1:
                //actionbar.setTitle("FAQ");
                break;
        }
    }

    private void sendScreenAnalytics(String screenName){
        Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
        t.setScreenName(screenName);
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
