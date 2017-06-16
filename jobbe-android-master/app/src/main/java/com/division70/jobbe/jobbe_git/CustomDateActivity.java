package com.division70.jobbe.jobbe_git;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by giorgio on 03/11/15.
 */
public class CustomDateActivity extends FragmentActivity {

    private TimePicker timePicker;
    private DatePicker datePicker;
    private Button fine;

    private String defTime;

    private boolean dateChanged = false; //Default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customdate_layout);

        sendScreenAnalytics();

        this.init();
        setUpActionBar();
    }

    private void setUpActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        CustomActionbar customActionbar = new CustomActionbar(this);
        customActionbar.setTitle("Specifica data e ora");
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
        t.setScreenName("CLIENTE - CREA RICHIESTA - SELEZIONE DATA SPECIFICA");
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void init(){
        this.datePicker = (DatePicker) findViewById(R.id.datePicker);
        this.timePicker = (TimePicker) findViewById(R.id.timePicker);
        this.timePicker.setIs24HourView(true); //Setting 24h layout
        this.fine = (Button) findViewById(R.id.customdate_fine);

        this.defTime = getIntent().getStringExtra("defTime");

        this.datePicker.init(this.datePicker.getYear(),
                this.datePicker.getMonth(),
                this.datePicker.getDayOfMonth(), new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker arg0, int arg1, int arg2, int arg3) {
                        dateChanged = true;
                    }
                });

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {

            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                dateChanged = true;
            }
        });

        setListeners();
    }

    private void setListeners() {

        this.fine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date now = new Date();
                Date newDate = getDateFromDatePicker(datePicker, timePicker);

                DateFormat nowTime = DateFormat.getDateTimeInstance();

                if (!dateChanged) { //No change done, return
                    Intent intent = new Intent();
                    intent.putExtra("customDate", false);
                    intent.putExtra("defTime", defTime);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                } else if (newDate.before(now)){
                    Toast.makeText(getApplicationContext(), "La data inserita non è corretta",
                            Toast.LENGTH_LONG).show();
                } else if (DateUtils.isToday(newDate.getTime()) && timeBeforeNow(timePicker, nowTime)){
                    Toast.makeText(getApplicationContext(), "L'orario inserito non è corretto",
                            Toast.LENGTH_LONG).show();
                }
                else {
                    Intent intent = new Intent();
                    intent.putExtra("customDate", true);
                    intent.putExtra("defTime", dateToString(newDate));
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    private boolean timeBeforeNow(TimePicker timePicker, DateFormat nowTime){

        Log.i("Picker", "HOUR: "+timePicker.getCurrentHour());
        Log.i("Picker", "MINS: "+timePicker.getCurrentMinute());

        Log.i("NowTime", "HOUR: "+nowTime.getCalendar().getTime().getHours());
        Log.i("NowTime", "MINS: "+nowTime.getCalendar().getTime().getMinutes());

        if (Build.VERSION.SDK_INT >= 23 ){
            if (timePicker.getHour() < nowTime.getCalendar().getTime().getHours())
                return true;
            else if (timePicker.getHour() == nowTime.getCalendar().getTime().getHours()) {
                if (timePicker.getMinute() <= nowTime.getCalendar().getTime().getMinutes())
                    return true;
            }
            return false;
        }
        else {
            if (timePicker.getCurrentHour() < nowTime.getCalendar().getTime().getHours())
                return true;
            else if (timePicker.getCurrentHour() == nowTime.getCalendar().getTime().getHours()) {
                if (timePicker.getCurrentMinute() <= nowTime.getCalendar().getTime().getMinutes())
                    return true;
            }
            return false;
        }
    }

    public static Date getDateFromDatePicker(DatePicker datePicker, TimePicker timePicker){
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year =  datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        return calendar.getTime();
    }

    private String dateToString(Date now){
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
        return dateFormat.format(now);
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
