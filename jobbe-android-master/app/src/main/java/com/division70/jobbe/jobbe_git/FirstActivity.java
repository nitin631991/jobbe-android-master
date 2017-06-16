package com.division70.jobbe.jobbe_git;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
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
 * Created by osvy on 26/10/15.
 */
public class FirstActivity extends Activity {
    private ParseObject[] jobs;
    private ParseObject[] zones;
    private int reqNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // LE CHIAMATE A PARSECLOUD SONO SINCRONE IN QUANTO QUESTA ACTIVITY
        // VIENE ESEGUITA PRIMA DI MOSTRARE LA PRIMA SCHERMATA, QUINDI
        // NON VIENE BLOCCATO NULLA

        selectPage();

        //Solo x debug, selezionare l'activity da provare x andarci subito
        //Intent intent = new Intent(FirstActivity.this, HomeClientActivity.class);
        //startActivity(intent);
    }

    private void selectPage(){
        ParseUser user = ParseUser.getCurrentUser();
        // WARNING : SOLO PER DEBUG
        //user = null;
        // END WARNING
        if (user != null){
            if(Utils.checkConnection(getApplicationContext(),true)) {
                try {
                    user.fetch();
                } catch (ParseException e) {
                    ParseErrorHandler.handleParseError(e, getApplicationContext());
                    e.printStackTrace();
                    return;
                }
            }
            String type = user.get("type").toString();
            Log.i("FirstActivity_UserType",type.toString());
            if (type.equals("supplier")){
                Intent intent = new Intent(FirstActivity.this, HomeSupplierActivity.class);
                startActivity(intent);
            }
            else {
                Intent intent = new Intent(FirstActivity.this, HomeClientActivity.class);
                startActivity(intent);
            }
        }
        else {
            //Vai alla pagina accedi / registrati
            Intent intent = new Intent(FirstActivity.this, MainActivity.class);
            startActivity(intent);
        }

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
