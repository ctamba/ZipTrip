package com.example.ziptrip;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StopsActivity extends AppCompatActivity implements OnMapReadyCallback {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    GoogleMap userMap;
    Intent stopsIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stops);

        stopsIntent = getIntent();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        userMap = googleMap;

        // Get latlang for the start and end destination
        db.collection("trips").document(stopsIntent.getStringExtra("tripId")).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful() && task != null){
                            Map<String, Double> startMap = (Map<String, Double>)task.getResult().get("startlocation");
                            Map<String, Double> destMap = (Map<String, Double>)task.getResult().get("destinationlocation");
                            LatLng startLatLng = new LatLng(startMap.get("latitude"), startMap.get("longitude"));
                            LatLng destLatLng = new LatLng(destMap.get("latitude"), destMap.get("longitude"));

                            // Add markers
                            Marker startMarker = userMap.addMarker(new MarkerOptions().position(startLatLng));
                            Marker destMarker = userMap.addMarker(new MarkerOptions().position(destLatLng));

                            // Zoom map
                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            builder.include(startMarker.getPosition());
                            builder.include(destMarker.getPosition());
                            LatLngBounds mapBounds = builder.build();
                            userMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, 100));

                            // Draw path between points
                            String url = getUrl(startLatLng, destLatLng);
                            TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                            taskRequestDirections.execute(url);
                        }
                    }
                });
    }

    private String getUrl(LatLng origin, LatLng dest){
        String originStr = "origin=" + origin.latitude + "," + origin.longitude;
        String destStr = "destination=" + dest.latitude + "," + dest.longitude;
        String sensorStr = "sensor=false";
        String modeStr = "mode=driving";
        String keyStr = "key=" + getString(R.string.google_maps_key);
        String parameters = originStr + "&" + destStr + "&" + sensorStr + "&" + modeStr + "&" + keyStr;

        // Creating url to request
        String url = "https://maps.googleapis.com/maps/api/directions/json?" + parameters;
        return url;
    }

    // Used to get directions from two points using url created
    private String requestDirections(String reqUrl) throws IOException {
        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try{
            URL url = new URL(reqUrl);
            httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.connect();

            // Get the response result
            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while((line = bufferedReader.readLine()) != null){
                stringBuffer.append(line);
            }

            responseString = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(inputStream != null){
                inputStream.close();
            }
            httpURLConnection.disconnect();
        }
        return responseString;
    }

    // Class used to send async task to get directions
    public class TaskRequestDirections extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String responseString = "";
            try{
                responseString = requestDirections(strings[0]);
            } catch (Exception e){
                e.printStackTrace();
            }

            return responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // Parse json result
            TaskParser taskParser = new TaskParser();
            taskParser.execute(s);
        }
    }

    public class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String, String>>>>{

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject = null;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionsParser directionsParser = new DirectionsParser();
                routes = directionsParser.parse(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            // Get list route and display it into the map
            ArrayList points = null;
            PolylineOptions polylineOptions = null;

            for(List<HashMap<String, String>> path : lists){
                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                for(HashMap<String, String> point : path){
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lon"));
                    points.add(new LatLng(lat, lng));
                }

                polylineOptions.addAll(points);
                polylineOptions.width(10);
                polylineOptions.color(Color.rgb(72, 153, 88));
                polylineOptions.geodesic(true);
            }

            if(polylineOptions != null){
                userMap.addPolyline(polylineOptions);
            }
            else{
                Toast.makeText(StopsActivity.this, "Directions were unable to be found", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
