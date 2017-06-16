package com.division70.jobbe.jobbe_git;

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
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by giorgio on 22/10/15.
 */
public class EmptyCustomerHomeActivity extends Activity {
    private ParseUser user;
    private int numberOfSuppliers;
    private TextView numberTextView;
    private int minSuppliersToDisplay = 7;

    private Button newRequestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emptyhomecustomer_layout);

        this.init();

        user = ParseUser.getCurrentUser();
        if (user == null){
            Intent intent = new Intent(EmptyCustomerHomeActivity.this, FirstActivity.class);
            startActivity(intent);
            return;
        }
        Log.i("isSupplierinHome", user.get("isSupplier").toString());
        Map<String,Double> params = new HashMap<>();
        params.put("latitude",(Double) user.get("latitude"));
        params.put("latitude",(Double) user.get("latitude"));
        try{
            numberOfSuppliers = ParseCloud.callFunction("getNearSuppliers",params);
        }
        catch (ParseException e){
            numberOfSuppliers = minSuppliersToDisplay;
        }

        numberTextView = (TextView) findViewById(R.id.textView13);
        if (numberOfSuppliers <= minSuppliersToDisplay)
            numberTextView.setText(Integer.toString(minSuppliersToDisplay));
        else
            numberTextView.setText(Integer.toString(numberOfSuppliers));

    }

    private void init(){
        this.newRequestButton = (Button) findViewById(R.id.new_req_button);

        this.newRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmptyCustomerHomeActivity.this, JobChooserActivity.class);
                intent.putExtra("purpose", "requestCompletion");
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Log.i("MenuId", id + "");
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(EmptyCustomerHomeActivity.this, SettingsClientActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.switch_profile){
            switchToSupplier();
        }
        else if(id == R.id.action_faq){
            Intent intent = new Intent(EmptyCustomerHomeActivity.this, WebViewActivity.class);
            intent.putExtra("url", getClientFaq());
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private String getClientFaq(){
        return getString(R.string.web_view_endpoint) + getString(R.string.faq_cliente);
    }

    private void switchToSupplier(){
        final boolean isSupplier = ParseUser.getCurrentUser().getBoolean("isSupplier");
        Log.i("isSupplier", isSupplier + "");
        chooseSwitchRoute(isSupplier);
    }

    private void chooseSwitchRoute(boolean originalIsSupplier){
        if (originalIsSupplier){
            Log.i("isSupplier", originalIsSupplier + "");
            //L'utente è già stato supplier, lo mando alla home giusta
            user.put("type", "supplier");
            try{
                user.save();
            } catch (ParseException e){
                e.printStackTrace();
            }
            ArrayList<ParseObject> requests = (ArrayList<ParseObject>) user.get("matchingRequests");
            int reqNumber = requests.size();
            Log.i("Request fornitore", Integer.toString(reqNumber));
            if (reqNumber == 0){
                Intent intent = new Intent(EmptyCustomerHomeActivity.this, EmptySupplierHomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                //overridePendingTransition(R.anim.flip_out,R.anim.flip_in);
            }
            else {
                Intent intent = new Intent(EmptyCustomerHomeActivity.this, HomeSupplierActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }
        else {
            //L'utente non è mai stato supplier, lo mando alla registrazione del supplier
            Intent intent = new Intent(EmptyCustomerHomeActivity.this, JobChooserActivity.class);
            intent.putExtra("switchFromClient", true);
            intent.putExtra("purpose","supplierSignup");
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Attenzione")
                .setMessage("Vuoi chiudere l\'applicazione?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        finish();
                    }
                }).create().show();
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
