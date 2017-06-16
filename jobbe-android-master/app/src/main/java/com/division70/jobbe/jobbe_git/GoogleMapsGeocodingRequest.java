package com.division70.jobbe.jobbe_git;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by giorgio on 30/10/15.
 */
public class GoogleMapsGeocodingRequest {

    private String googleApiURL = "http://maps.googleapis.com/maps/api/geocode/json";
    private LatLng coords;
    private String province = null; //Default
    private String cityName = null; //Default
    private String locationAddress = null; //Defult

    public GoogleMapsGeocodingRequest(){}

    public HashMap<String, Object> getLatLngFromPlace(String place) throws ExecutionException, InterruptedException {
        this.googleApiURL = this.googleApiURL + "?address=" + place; //Setting up the URL
        this.googleApiURL = this.googleApiURL.replace(" ", "%20");
        Log.i("GOOGLE_URL", this.googleApiURL);

        new ConnectionToGoogleAPIforLatLng().execute().get();

        HashMap<String, Object> tmp = new HashMap<>();
        if(coords == null)
            return null;
        tmp.put("latitude", this.coords.latitude);
        tmp.put("longitude", this.coords.longitude);
        tmp.put("fullAddress", this.locationAddress);
        tmp.put("province", province);

        return tmp;
    }

    public String getAddressFromCoords(LatLng coords) throws ExecutionException, InterruptedException {
        this.coords = coords;

        //Setting up the URL
        this.googleApiURL =
                this.googleApiURL + "?latlng=" + coords.latitude + "," + coords.longitude + "&sensor=false";

        new ConnectionToGoogleAPIforAddress().execute().get();

        return this.locationAddress;
    }

    public String getAddressForMapFromCoords(LatLng coords) throws ExecutionException, InterruptedException {
        this.coords = coords;

        //Setting up the URL
        this.googleApiURL =
                this.googleApiURL + "?latlng=" + coords.latitude + "," + coords.longitude + "&sensor=false";

        new ConnectionToGoogleAPIforAddressForMap().execute().get();

        return this.locationAddress;
    }

    public String getCityNameFromCoords (LatLng coords) throws Exception {
        this.coords = coords;
        this.googleApiURL = this.googleApiURL + "?latlng=" + coords.latitude + "," + coords.longitude; //Setting up the URL

        new ConnectToGoogleAPIForCityName().execute().get();

        //If here, either no match was found or the province is null
        return cityName;
    }

    //This function gets a JSON back from Google API endpoint with all
    //the info given a LatLng variable and the list of zones.
    //It returns the Province
    public ParseObject getProvinceFromCoords (LatLng coords, ArrayList<ParseObject> zones) throws Exception {
        this.coords = coords;
        this.googleApiURL = this.googleApiURL + "?latlng=" + coords.latitude + "," + coords.longitude; //Setting up the URL

        new ConnectToGoogleAPI().execute().get();

        //Check if zones contains the returned province (if != null)
        for(ParseObject zone : zones)
            if (zone.get("sigla").toString().equals(this.province))
                return zone;

        //If here, either no match was found or the province is null
        return null;
    }

    private class ConnectToGoogleAPI extends AsyncTask<Void, Void, String> {

        public ConnectToGoogleAPI() {}

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... params) {

            String jsonResult = "NULL";

            try {
                jsonResult = sendGet();
                handleJSON(jsonResult);
            } catch (Exception e) {

            }

            return jsonResult;
        }

        // HTTP GET request
        private String sendGet() throws Exception {

            URL obj = new URL(googleApiURL);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //Optional default is GET
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            //Log.i("HTTP", "RespCode: " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);

            in.close();

            return response.toString();
        }

        protected void handleJSON(String jsonResult) {
            try {
                JSONArray addressArray = new JSONObject(jsonResult).getJSONArray("results").getJSONObject(0).getJSONArray("address_components");

                for(int i = 0; i< addressArray.length(); i++) {
                    if (addressArray.getJSONObject(i).getJSONArray("types").get(0).toString().equals("administrative_area_level_2")) {
                        province = addressArray.getJSONObject(i).get("short_name").toString();
                        Log.i("PROVINCE", province);
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class ConnectToGoogleAPIForCityName extends AsyncTask<Void, Void, String> {

        public ConnectToGoogleAPIForCityName() {}

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... params) {

            String jsonResult = "NULL";

            try {
                jsonResult = sendGet();
                handleJSON(jsonResult);
            } catch (Exception e) {

            }

            return jsonResult;
        }

        // HTTP GET request
        private String sendGet() throws Exception {

            URL obj = new URL(googleApiURL);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //Optional default is GET
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            //Log.i("HTTP", "RespCode: " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);

            in.close();

            return response.toString();
        }

        protected void handleJSON(String jsonResult) {
            try {
                JSONArray addressArray = new JSONObject(jsonResult).getJSONArray("results").getJSONObject(0).getJSONArray("address_components");

                for(int i = 0; i< addressArray.length(); i++) {
                    if (addressArray.getJSONObject(i).getJSONArray("types").get(0).toString().equals("administrative_area_level_2")) {
                        cityName = addressArray.getJSONObject(i).get("long_name").toString();
                        Log.i("cityName", cityName);
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class ConnectionToGoogleAPIforAddress extends AsyncTask<Void, Void, String> {

        public ConnectionToGoogleAPIforAddress() {}

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... params) {

            String jsonResult = "NULL";

            try {
                jsonResult = sendGet();
                handleJSON(jsonResult);
            } catch (Exception e) {

            }

            return jsonResult;
        }

        // HTTP GET request
        private String sendGet() throws Exception {

            URL obj = new URL(googleApiURL);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //Optional default is GET
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            //Log.i("HTTP", "RespCode: " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);

            in.close();

            return response.toString();
        }

        protected void handleJSON(String jsonResult) {
            try {
                JSONArray addressArray = new JSONObject(jsonResult).getJSONArray("results").getJSONObject(0).getJSONArray("address_components");

                for(int i = 0; i< addressArray.length(); i++) {
                    if (addressArray.getJSONObject(i).getJSONArray("types").get(0).toString().equals("administrative_area_level_2")) {
                        locationAddress = new JSONObject(jsonResult).getJSONArray("results").getJSONObject(0).getString("formatted_address");
                        Log.i("ADDRESS", locationAddress);
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class ConnectionToGoogleAPIforAddressForMap extends AsyncTask<Void, Void, String> {

        public ConnectionToGoogleAPIforAddressForMap() {}

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... params) {

            String jsonResult = "NULL";

            try {
                jsonResult = sendGet();
                handleJSON(jsonResult);
            } catch (Exception e) {

            }

            return jsonResult;
        }

        // HTTP GET request
        private String sendGet() throws Exception {

            URL obj = new URL(googleApiURL);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //Optional default is GET
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            //Log.i("HTTP", "RespCode: " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);

            in.close();

            return response.toString();
        }

        protected void handleJSON(String jsonResult) {
            try {
                JSONArray addressArray = new JSONObject(jsonResult).getJSONArray("results").getJSONObject(0).getJSONArray("address_components");

                String route = "", number = "", town = "", province = "";

                for(int i = 0; i< addressArray.length(); i++) {
                    if (addressArray.getJSONObject(i).getJSONArray("types").get(0).toString().equals("street_number")) {
                        number = addressArray.getJSONObject(i).getString("long_name");
                    }
                    else if (addressArray.getJSONObject(i).getJSONArray("types").get(0).toString().equals("route")) {
                        route = addressArray.getJSONObject(i).getString("long_name");
                    }
                    else if (addressArray.getJSONObject(i).getJSONArray("types").get(0).toString().equals("locality")) {
                        town = addressArray.getJSONObject(i).getString("long_name");
                    }
                    else if (addressArray.getJSONObject(i).getJSONArray("types").get(0).toString().equals("administrative_area_level_2")) {
                        province = addressArray.getJSONObject(i).getString("short_name");
                    }

                    if(!number.isEmpty() && !route.isEmpty() && !town.isEmpty() && !province.isEmpty())
                        break;
                }

                locationAddress = route + " " + number + " - " + town + " (" + province + ")";
                Log.i("address", locationAddress);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class ConnectionToGoogleAPIforLatLng extends AsyncTask<Void, Void, String> {

        public ConnectionToGoogleAPIforLatLng() {}

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... params) {

            String jsonResult = "NULL";

            try {
                jsonResult = sendGet();
                handleJSON(jsonResult);
            } catch (Exception e) {

            }

            return jsonResult;
        }

        // HTTP GET request
        private String sendGet() throws Exception {

            URL obj = new URL(googleApiURL);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //Optional default is GET
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            //Log.i("HTTP", "RespCode: " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);

            in.close();

            return response.toString();
        }

        protected void handleJSON(String jsonResult) {
            try {
                JSONArray addressArray = new JSONObject(jsonResult).getJSONArray("results").getJSONObject(0).getJSONArray("address_components");

                for(int i = 0; i< addressArray.length(); i++) {
                    if (addressArray.getJSONObject(i).getJSONArray("types").get(0).toString().equals("administrative_area_level_2")) {
                        locationAddress = new JSONObject(jsonResult).getJSONArray("results").getJSONObject(0).getString("formatted_address");
                        province = addressArray.getJSONObject(i).get("short_name").toString();
                        Log.i("PROVINCE", province);
                        break;
                    }
                }

                JSONObject geometryLocation = new JSONObject(jsonResult).getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
                Log.i("LAT: ", geometryLocation.get("lat").toString());
                Log.i("LNG: ", geometryLocation.get("lng").toString());
                coords = new LatLng((double) geometryLocation.get("lat"), (double) geometryLocation.get("lng"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
