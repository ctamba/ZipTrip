package com.example.ziptrip.googlemaps;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.MainThread;

import com.example.ziptrip.CreateTripActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.io.IOException;

public class GetDirectionsData extends AsyncTask<Object, String, String> {
    GoogleMap userMap;
    String url;
    String googleDirectionsData;

    @Override
    protected String doInBackground(Object... objects) {
        userMap = (GoogleMap)objects[0];
        url = (String)objects[1];

        DownloadUrl downloadUrl = new DownloadUrl();
        try{
            googleDirectionsData = downloadUrl.readUrl(url);
        } catch(IOException e){
            e.printStackTrace();
        }

        return googleDirectionsData;
    }

    @Override
    protected void onPostExecute(String s){
        String[] directionsList;
        DataParser parser = new DataParser();
        directionsList = parser.parseDirections(s);
        Log.i("TAG", "In on post execute");
        displayDirection(directionsList);
    }

    public void displayDirection(String[] directionsList){
        for(int i = 0; i < directionsList.length; i++){
            PolylineOptions options = new PolylineOptions();
            options.color(Color.GREEN);
            options.width(10);
            options.addAll(PolyUtil.decode(directionsList[i]));

            userMap.addPolyline(options);
        }
    }
}
