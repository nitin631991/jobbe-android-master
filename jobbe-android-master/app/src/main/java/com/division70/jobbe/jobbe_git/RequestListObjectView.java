package com.division70.jobbe.jobbe_git;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseObject;

/**
 * Created by osvy on 09/11/15.
 */

    /*
    private ImageView iconView;
    private TextView titleView;
    private TextView stateView;
    private TextView numProposalsView;
    private TextView unseenProposalsView;
    */

public class RequestListObjectView extends RelativeLayout{
    private ImageView iconView;
    private TextView titleView;
    private TextView stateView;
    private TextView numProposalsView;
    private TextView unseenProposalsView;
    private RelativeLayout unseenLayout;
    private ParseObject request;

    private String iconName;

    LayoutInflater mInflater;
    public RequestListObjectView(Context context) {
        super(context);
        mInflater = LayoutInflater.from(context);
        init();

    }
    public RequestListObjectView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mInflater = LayoutInflater.from(context);
        init();
    }
    public RequestListObjectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInflater = LayoutInflater.from(context);
        init();
    }

    public void init()
    {
        mInflater.inflate(R.layout.requestlistobjectview_layout, this, true);
        this.titleView = (TextView) findViewById(R.id.jobTitle_tv);
        this.stateView = (TextView) findViewById(R.id.timing_tv);
        this.numProposalsView = (TextView) findViewById(R.id.numProp_tv);
        this.unseenProposalsView = (TextView) findViewById(R.id.unseen_tv);
        this.iconView = (ImageView) findViewById(R.id.photo_iv);
        this.unseenLayout = (RelativeLayout) findViewById(R.id.unseen_layout);
    }

    public void setRequest(ParseObject request){
        this.request = request;
        setRequestTitle(request.getString("title"));
        setRequestState(request.getInt("state"));
        setNumberOfProposals(request.getInt("numberOfProposals"));
        setUnseenProposals(request.getInt("unseenProposals"));
        setRequestPicture();
    }

    private void setRequestPicture(){
        ParseObject jobCategory = (ParseObject) this.request.get("jobCatId");
        try {
            Log.i("TITLE: ", titleView.getText().toString());
            jobCategory.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.iconName = jobCategory.get("icon").toString().replace("-", "_");
        Log.i("ICON: ", this.iconName);
        int resourceId = getResources().getIdentifier(this.iconName, "drawable", "com.division70.jobbe.jobbe_git");
        this.iconView.setImageResource(resourceId);
    }

    public void setOfflineRequestPicture(String iconName){
        int resourceId = getResources().getIdentifier(iconName, "drawable", "com.division70.jobbe.jobbe_git");
        this.iconView.setImageResource(resourceId);
    }

    public void setArchiveRequest(ParseObject request){
        this.request = request;
        setRequestTitle(request.getString("title"));
        setRequestState(request.getInt("state"));
        setNumberOfProposals(0);
        setUnseenProposals(0);
        setRequestPicture();
    }

    public void setRequestTitle(String title){
        titleView.setText(title);
    }

    public String getIconName(){
        if(this.iconName != null)
            return this.iconName;
        else
            return "arte_icon"; //WRONG icon used not to create errors
    }

    public void setRequestState(int state){
        String text = "";
        switch (state) {
            case 1: text = "In attesa di proposte"; break;
            case 2: text = "Assegnata"; break;
            case 3: text = "Lavoro completato"; break;
            case 4: text = "Lavoro completato"; break;
            case 5: text = "Richiesta eliminata"; break;
            case 6: text = "Lavoro non completato"; break;
        }
        stateView.setText(text);
    }

    public void setNumberOfProposals(int numberOfProposals){
        if (numberOfProposals == 0){
            numProposalsView.setVisibility(GONE);
        }
        else {
            numProposalsView.setVisibility(VISIBLE);
            numProposalsView.setText(numberOfProposals + " proposte ricevute");
        }
    }

    public void setUnseenProposals(int unseenProposals){
        if (unseenProposals <= 0){
            unseenLayout.setVisibility(GONE);
        }
        else{
            unseenLayout.setVisibility(VISIBLE);
            unseenProposalsView.setText(unseenProposals + "");
        }
    }

}
