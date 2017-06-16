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
public class EmptySupplierHomeActivity extends Activity {
    private ParseUser user;
    private int numberOfClients;
    private TextView numberTextView;
    private TextView textToBeRemoved;
    private Button buttonToBeRemoved;
    private int minClientsToDisplay = 10;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emptyhomesupplier_layout);

        user = ParseUser.getCurrentUser();
        if (user == null){
            Intent intent = new Intent(EmptySupplierHomeActivity.this, FirstActivity.class);
            startActivity(intent);
            return;
        }
        Map<String,Double> params = new HashMap<>();
        params.put("latitude",(Double) user.get("latitude"));
        params.put("latitude",(Double) user.get("latitude"));
        try{
            numberOfClients = ParseCloud.callFunction("getNearClients",params);
        }
        catch (ParseException e){
            numberOfClients = minClientsToDisplay;
        }

        numberTextView = (TextView) findViewById(R.id.textView13);
        if (numberOfClients <= minClientsToDisplay)
            numberTextView.setText(Integer.toString(minClientsToDisplay));
        else
            numberTextView.setText(Integer.toString(numberOfClients));
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(EmptySupplierHomeActivity.this, SupplierProfileActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.switch_profile){
            switchToClient();
        }
        else if(id == R.id.action_faq){
            Intent intent = new Intent(EmptySupplierHomeActivity.this, WebViewActivity.class);
            intent.putExtra("url", getSupplierFaq());
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private String getSupplierFaq(){
        return getString(R.string.web_view_endpoint) + getString(R.string.faq_fornitore);
    }

    private void switchToClient(){
        // Cambio il tipo di utente a client
        user.put("type", "client");
        try{
            user.save();
        }
        catch (ParseException e){
            Toast.makeText(getApplicationContext(), "C\'è stato un errore durante il salvataggio.", Toast.LENGTH_LONG).show();
            return;
        }

        int reqNumber = getUserRequestsNumber(user);
        Log.i("Request cliente", Integer.toString(reqNumber));
        if (reqNumber == 0){
            Intent intent = new Intent(EmptySupplierHomeActivity.this, EmptyCustomerHomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        else {
            Intent intent = new Intent(EmptySupplierHomeActivity.this, HomeClientActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            //Toast.makeText(getApplicationContext(), "Home cliente (Work in Progress)", Toast.LENGTH_LONG).show();
        }
    }

    private int getUserRequestsNumber(ParseUser user) {
        List<ParseObject> list;
        ArrayList<Integer> illegalStates = new ArrayList<Integer>();
        illegalStates.add(4);
        illegalStates.add(5);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
        query.whereEqualTo("userId", user);
        query.whereNotContainedIn("state", illegalStates);
        query.whereNotEqualTo("confirmUncompleteReq", true);
        query.include("job");
        query.include("jobCatId");
        query.include("choosenProposal");
        query.include("choosenSupplier");
        query.orderByDescending("createdAt");
        try {
            list = query.find();
            return list.size();
        }
        catch (ParseException e){
            Log.i("REQUEST_LIST", "FAILED");
            Log.i("ParseException",e.toString());
            ParseErrorHandler.handleParseError(e, getApplicationContext());
        }
        return 0;
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
