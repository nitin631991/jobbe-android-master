package com.division70.jobbe.jobbe_git;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by giorgio on 22/10/15.
 */
public class ParseManager {

    public ArrayList<ParseObject> getZones(Context context) throws ExecutionException, InterruptedException {
        return new getZonesQuery().execute().get();
    }

    private class getZonesQuery extends AsyncTask<Void, Void, ArrayList<ParseObject>> {

        public getZonesQuery() {
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected ArrayList<ParseObject> doInBackground(Void... params) {

            ArrayList<ParseObject> result = null;

            try {
                result = (ArrayList<ParseObject>) ParseQuery.getQuery("Zone").whereEqualTo("active", true).find(); //Getting zones
            } catch (Exception e) {

            }

            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<ParseObject> zones) {}
    }

}
