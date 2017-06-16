package com.division70.jobbe.jobbe_git;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class ParseErrorHandler {
    public static void handleParseError(ParseException e, Context c) {
        switch (e.getCode()) {
            case ParseException.INVALID_SESSION_TOKEN: handleInvalidSessionToken(c);
                break;

             // Other Parse API errors that you want to explicitly handle
        }
    }

    private static void handleInvalidSessionToken(Context c) {
        //--------------------------------------
        // Option 1: Show a message asking the user to log out and log back in.
        //--------------------------------------
        // If the user needs to finish what they were doing, they have the opportunity to do so.
        //
        // new AlertDialog.Builder(getActivity())
        //   .setMessage("Session is no longer valid, please log out and log in again.")
        //   .setCancelable(false).setPositiveButton("OK", ...).create().show();

        //--------------------------------------
        // Option #2: Show login screen so user can re-authenticate.
        //--------------------------------------
        // You may want this if the logout button could be inaccessible in the UI.
        //
        // startActivityForResult(new ParseLoginBuilder(getActivity()).build(), 0);

        Log.i("INVALID SESSION", "BAAAAMMMMM!!!");
        //TODO LOGOUT

        Log.i("INVALID SESSION","start logout");
        ParseUser.logOut();
        Log.i("INVALID SESSION", "create intent");
        Intent intent = new Intent(c, FirstActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Log.i("INVALID SESSION", "start activity");
        c.startActivity(intent);
        Log.i("INVALID SESSION", "first activity started");
    }

}
