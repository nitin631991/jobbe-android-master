package com.division70.jobbe.jobbe_git;

import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by giorgio on 30/11/15.
 */
public class ParseProxyObject implements Serializable {

    private static final long serialVersionUID = 1L;
    private HashMap<String, Object> values = new HashMap<String, Object>();
    private String objectId;
    private Date createdAt;

    public HashMap<String, Object> getValues() {
        return values;
    }

    public void setValues(HashMap<String, Object> values) {
        this.values = values;
    }

    public ParseProxyObject(ParseObject object) {
        // grab object id and creation date
        this.objectId = object.getObjectId();
        this.createdAt = object.getCreatedAt();

        // Loop the keys in the ParseObject
        for(String key : object.keySet()) {
            @SuppressWarnings("rawtypes")
            Class classType = object.get(key).getClass();
            if(classType == byte[].class || classType == String.class ||
                    classType == Integer.class || classType == Boolean.class ||
                    classType == Date.class || classType == File.class ||
                    classType == ArrayList.class || classType == double.class) {
                values.put(key, object.get(key));
                Log.i("KEY", key + "  :  " + object.get(key).toString());

            } /*else if(classType == ParseUser.class || classType == ParseObject.class) {
                ParseObject tmp = (ParseObject) object.get(key);
                try {
                    tmp.fetchIfNeeded();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                boolean recursion = false;
                //Avoid recoursion
                for(String column :  tmp.keySet()) {
                    Class columnClassType = object.get(key).getClass();
                    if(classType == ParseObject.class) {
                        if(tmp.get(column) == object)
                            recursion = true;
                        break;
                    }
                }

                if(!recursion) {
                    ParseProxyObject parseObject = new ParseProxyObject((ParseObject) object.get(key));
                    values.put(key, parseObject);
                    Log.i("DENTRO A JOB", parseObject.getString("name"));
                }

            } */else if(classType == ParseGeoPoint.class){
                ParseGeoPoint point = (ParseGeoPoint) object.get(key);
                HashMap<String, Object> location = new HashMap<>();
                location.put("latitude", point.getLatitude());
                location.put("longitude", point.getLongitude());
                values.put(key, location);
            }

            if(key.equals("job")){
                ParseObject obj = null;
                try {
                    obj = object.getParseObject("job").fetchIfNeeded();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Log.i("jobNAMEEEEE", obj.getString("name"));
            }
        }
    }

    public ParseGeoPoint getLocationCords(String key){
        if(has(key)) {
            return (ParseGeoPoint) geoPointFromHasMap((HashMap<String, Object>) values.get(key));
        } else {
            return null;
        }
    }

    private ParseGeoPoint geoPointFromHasMap(HashMap<String, Object> map){
        return new ParseGeoPoint((double) map.get("latitude"), (double) map.get("longitude"));
    }

    public String getString(String key) {
        if(has(key)) {
            return (String) values.get(key);
        } else {
            return "";
        }
    }

    public int getInt(String key) {
        if(has(key)) {
            return (Integer)values.get(key);
        } else {
            return 0;
        }
    }

    public Boolean getBoolean(String key) {
        if(has(key)) {
            return (Boolean)values.get(key);
        } else {
            return false;
        }
    }

    public byte[] getBytes(String key) {
        if(has(key)) {
            return (byte[])values.get(key);
        } else {
            return new byte[0];
        }
    }

    public ParseProxyObject getParseObject(String key) {
        if(has(key)) {
            return (ParseProxyObject) values.get(key);
        } else {
            return null;
        }
    }

    public ParseFile getParseFile(String key) {
        if(has(key)) {
            return (ParseFile) values.get(key);
        } else {
            return null;
        }
    }

    public ArrayList getArrayList(String key) {
        if(has(key)) {
            return (ArrayList<HashMap<String, Object>>) values.get(key);
        } else {
            return null;
        }
    }

    public double getDouble(String key) {
        if(has(key)) {
            return (double) values.get(key);
        } else {
            return -1;
        }
    }

    public Date getDate(String key) {
        if(has(key)) {
            return (Date) values.get(key);
        } else {
            return null;
        }
    }

    public Boolean has(String key) {
        return values.containsKey(key);
    }

    /**
     * Get objectId
     */
    public String getObjectId() {
        return objectId;
    }

    /**
     * Get createdId
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * Create a ParseObject copy with the same objectId
     */
    public <T extends ParseObject> T getParseObject(Class<T> className)
    {
        T object = ParseObject.createWithoutData(className, objectId);

        for (String key : values.keySet())
            object.put(key, values.get(key));

        return object;
    }
}
