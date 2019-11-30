package com.example.ziptrip.googlemaps;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class DataParser {

    private HashMap<String, String> getPlace(JSONObject googlePlaceJson){
        HashMap<String, String> googlePlacesMap = new HashMap<>();
        String placeName = "NA";
        String vicinity = "NA";
        String latitude = "";
        String longitude = "";
        String reference = "";

        try{
            if(!googlePlaceJson.isNull("name")) {
                placeName = googlePlaceJson.getString("name");
            }
            if(!googlePlaceJson.isNull("vicinity")){
                vicinity = googlePlaceJson.getString("vicinity");
            }
            latitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng");
            reference = googlePlaceJson.getString("reference");
            googlePlacesMap.put("place_name", placeName);
            googlePlacesMap.put("vicinity", vicinity);
            googlePlacesMap.put("lat", latitude);
            googlePlacesMap.put("lng", longitude);
            googlePlacesMap.put("reference", reference);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return googlePlacesMap;
    }

    public String[] parseDirections(String jsonData){
        JSONArray routeArray = null;
        JSONObject routeObject;
        try{
            routeObject = new JSONObject(jsonData);
            routeArray = routeObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs")
                    .getJSONObject(0).getJSONArray("steps");
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        return getPaths(routeArray);
    }

    public String getPath(JSONObject googlePathJson){
        String polyline = null;
        try {
            polyline = googlePathJson.getJSONObject("polyline").getString("points");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return polyline;
    }

    public String[] getPaths(JSONArray googleStepJson){
        // Give as many slots as there are steps in array
        String[] polylines = new String[googleStepJson.length()];

        for(int i = 0; i < googleStepJson.length(); i++){
            try {
                polylines[i] = getPath(googleStepJson.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return polylines;
    }

    private HashMap<String, String> getDuration(JSONArray googleDirectionsJson){
        HashMap<String, String> googleDirectionsMap = new HashMap<>();
        String duration = "";
        String distance = "";

        Log.d("JSON response", googleDirectionsJson.toString());
        return googleDirectionsMap;
    }
}
