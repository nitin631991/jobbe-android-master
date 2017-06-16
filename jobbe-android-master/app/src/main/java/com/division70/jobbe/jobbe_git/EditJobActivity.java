package com.division70.jobbe.jobbe_git;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;

public class EditJobActivity extends Activity {
    private Switch pIVA_switch;
    private EditText pIVA;
    private Button backbutton;
    private ArrayList<String> cityList;
    private TextView editjobzones;
    private TextView editjobmodifyzones;
    private boolean modifiedPiva = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editjob_layout);

        sendScreenAnalytics("FORNITORE - PROFILO - AGGIORNA LAVORO");

        this.pIVA = (EditText) findViewById(R.id.editjob_piva);
        this.pIVA_switch = (Switch) findViewById(R.id.editjob_switch);
        String piva = getIntent().getStringExtra("PIVA");
        if (piva == null) this.pIVA_switch.setChecked(false);
        else {
            this.pIVA_switch.setChecked(true);
            pIVA.setText(piva);
        }
        this.resetFields(this.pIVA_switch.isChecked());


        this.cityList = getIntent().getStringArrayListExtra("citylist");
        editjobzones = (TextView) findViewById(R.id.editjobzones);
        setZonesTextView();


        editjobmodifyzones = (TextView) findViewById(R.id.editjobmodifyzones);
        editjobmodifyzones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EditJobActivity.this, EditJobZoneChooserActivity.class);
                intent.putExtra("cityList",cityList);
                startActivityForResult(intent, 7321);
            }
        });

        this.pIVA_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //Reset fields depending on user's P.IVA enabling
                resetFields(isChecked);
                modifiedPiva = true;
            }
        });

        this.pIVA.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {
                modifiedPiva = true;
            }
        });

        this.backbutton = (Button) findViewById(R.id.editjob_backbutton);
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (modifiedPiva)
                    if(pIVA_switch.isChecked() && !ValidPIVA(pIVA.getText().toString())){
                        Toast.makeText(getApplicationContext(), "La partita iva inserita non è valida", Toast.LENGTH_LONG).show();
                        return;
                    }

                Intent returnIntent = new Intent();
                returnIntent.putExtra("modifiedPiva", modifiedPiva);
                returnIntent.putExtra("piva", pIVA.getText().toString());
                returnIntent.putExtra("has_piva", pIVA_switch.isChecked());
                returnIntent.putExtra("cityList", cityList);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });

        setUpActionBar();
    }

    private void setUpActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        CustomActionbar customActionbar = new CustomActionbar(this);
        customActionbar.setTitle("Modifica professione");
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

    private void sendScreenAnalytics(String screenName){
        Tracker t = ((JobbeApplication) getApplication()).getDefaultTracker();
        t.setScreenName(screenName);
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void resetFields(boolean isChecked){
        if(!isChecked){
            this.pIVA.setVisibility(View.GONE);
            this.pIVA.setText("");
        }
        else{
            this.pIVA.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 7321){
            cityList = data.getStringArrayListExtra("cityList");
            for(String x : cityList)
                Log.i("citylist", x);
            setZonesTextView();
        }
    }

    private void setZonesTextView(){
        String cityListString = "";
        for(String x : cityList){
            cityListString += ", " + x.toString();
        }
        cityListString = "Nelle vicinanze di " + cityListString.substring(2);
        editjobzones.setText(cityListString);
    }

    static boolean ValidPIVA(String pi)
    {
        Log.i("VALID PIVA", "Into");
        int i, c, s;
        if( pi.length() == 0 )
            return false;
        Log.i("VALID PIVA", "Checkpoint 1");
        if( pi.length() != 11 )
            return false;
        Log.i("VALID PIVA", "Checkpoint 2");
        for( i=0; i<11; i++ ){
            if( pi.charAt(i) < '0' || pi.charAt(i) > '9' )
                return false;
        }
        Log.i("VALID PIVA", "Checkpoint 3");

        s = 0;
        for( i=0; i<=9; i+=2 )
            s += pi.charAt(i) - '0';
        for( i=1; i<=9; i+=2 ){
            c = 2*( pi.charAt(i) - '0' );
            if( c > 9 )  c = c - 9;
            s += c;
        }
        if( ( 10 - s%10 )%10 != pi.charAt(10) - '0' )
            return false;

        Log.i("VALID PIVA", "Seems valid: " + pi);
        return true;
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
