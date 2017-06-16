package com.division70.jobbe.jobbe_git;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

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

public class SupplierHomeListObjectView extends RelativeLayout {
    private RoundedImageView photoView;
    private TextView titleView;
    private TextView timingView;
    private ParseObject request;
    private RelativeLayout disableOverlay;
    private ParseFile photo;
    private Context context;


    LayoutInflater mInflater;

    public SupplierHomeListObjectView(Context context) {
        super(context);
        this.context = context;
        mInflater = LayoutInflater.from(context);
        init();

    }

    public SupplierHomeListObjectView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mInflater = LayoutInflater.from(context);
        init();
    }

    public SupplierHomeListObjectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInflater = LayoutInflater.from(context);
        init();
    }

    public void init() {
        mInflater.inflate(R.layout.supplierhomelistobjectview_layout, this, true);
        this.titleView = (TextView) findViewById(R.id.jobTitle_tv);
        this.timingView = (TextView) findViewById(R.id.timing_tv);
        this.photoView = (RoundedImageView) findViewById(R.id.photo_iv);
        this.disableOverlay = (RelativeLayout) this.findViewById(R.id.disable_overlay);
        this.disableOverlay.setVisibility(GONE);
    }

    public void setRequest(ParseObject request) {
        this.request = request;
        this.setRequestTitle(request.getString("title"));
        String timingType = request.getString("timingType");
        Date date = new Date();
        if (timingType.equals("specific")) date = (Date) request.get("dateRequest");
        this.setRequestTiming(timingType, date);
        setRequestPicture();
    }

    private void setRequestPicture() {
        ParseObject user = (ParseObject) this.request.get("userId");
        try {
            user.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (user.containsKey("profilePhoto")) {
            this.photo = (ParseFile) user.get("profilePhoto");
            if (photo != null) {
                Log.e("PhotoUrl",""+photo.getUrl());
                Picasso.with(context).load(photo.getUrl()).into(photoView);
//            photo.getDataInBackground(new GetDataCallback() {
//                @Override
//                public void done(byte[] data, ParseException e) {.
//                    if (e == null) {
////                        BitmapFactory.Options options=new BitmapFactory.Options();
////                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
////                        Picasso.with(context).load(bmp).into(photoView);
////                        photoView.setImageDrawable(null);
////                        Log.i("data length", data.length + "");
////                        int scaleStep = 100;
////                        int scaleFactor = data.length / scaleStep;
////                        Log.i("scaleFactor", scaleFactor + "");
////                        BitmapFactory.Options options=new BitmapFactory.Options();// Create object of bitmapfactory's option method for further option use
////                        options.inPurgeable = true; // inPurgeable is used to free up memory while required
////                        if(scaleFactor > 1)
////                            options.inSampleSize = scaleFactor;
////                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
////                        photoView.setImageDrawable(null);
////                        photoView.setImageBitmap(bmp);
//                    } else {
//                    }
//                }
//            });
            } //Else the placeholder remains
        }
    }

    public void setRequestTitle(String title) {
        titleView.setText(title);
    }

    public void setRequestTiming(String timing, Date date) {
        String text = "";
        switch (timing) {
            case "notUrgent":
                text = "Da concordare/non urgente";
                break;
            case "soonAsPossible":
                text = "Il prima possibile";
                break;
            case "specific":
                if (date == null) {
                    Log.i("Error", "date cannot be null with specific timing");
                } else {
                    text = createDateString(date);
                }
                break;
        }
        setTimingText(text);
    }

    public void setTimingText(String text) {
        timingView.setText(text);
    }

    public String createDateString(Date date) {
        String dayOfTheWeek = (String) android.text.format.DateFormat.format("EEEE", date);//Thursday
        String stringMonth = (String) android.text.format.DateFormat.format("MMM", date); //Jun
        String intMonth = (String) android.text.format.DateFormat.format("MM", date); //06
        String year = (String) android.text.format.DateFormat.format("yyyy", date); //2013
        String day = (String) android.text.format.DateFormat.format("dd", date); //20
        return day + "/" + intMonth + "/" + year;
    }

    public void disable() {
        disableOverlay.setVisibility(VISIBLE);
    }

    public void setRetired() {
        setTimingText("Richiesta Eliminata");
        timingView.setTextColor(getResources().getColor(R.color.jobbe_red));
        disable();
    }

    public void setExpired() {
        setTimingText("Richiesta Eliminata");
        //setTimingText("Richiesta ritirata");
        timingView.setTextColor(getResources().getColor(R.color.jobbe_red));
        disable();
    }
}
