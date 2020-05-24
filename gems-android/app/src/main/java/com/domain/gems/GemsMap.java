package com.domain.gems;

/*-----------------------------------

    - Gems -

    created by cubycode ©2017
    All Rights reserved

-----------------------------------*/

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

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

public class GemsMap extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, LocationListener {

    /* Views */
    private GoogleMap mapView;
    TextView titleTxt, infoTxt;
    Button hybridButt, standardButt, satelliteButt;



    /* Variables */
    ArrayList<LatLng> markerPoints;
    Location currentLocation;
    LocationManager locationManager;
    ParseObject gObj;
    Context ctx = this;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gems_map);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();



        // Init views
        titleTxt = findViewById(R.id.gmTitleTxt);
        titleTxt.setTypeface(Configs.gayatri);
        infoTxt = findViewById(R.id.gmInfoTxt);
        infoTxt.setTypeface(Configs.gayatri);
        hybridButt = findViewById(R.id.hybridButt);
        hybridButt.setTypeface(Configs.gayatri);
        standardButt = findViewById(R.id.standardButt);
        standardButt.setTypeface(Configs.gayatri);
        satelliteButt = findViewById(R.id.satelliteButt);
        satelliteButt.setTypeface(Configs.gayatri);



        // Get objectID from previous .java
        Bundle extras = getIntent().getExtras();
        String objectID = extras.getString("objectID");
        gObj = ParseObject.createWithoutData(Configs.GEMS_CLASS_NAME, objectID);
        try { gObj.fetchIfNeeded().getParseObject(Configs.GEMS_CLASS_NAME);

            // Get current location
            getCurrentLocation();


            // Get Gem info
            infoTxt.setText(gObj.getString(Configs.GEMS_GEM_NAME).toUpperCase() + " | Points: " + String.valueOf(gObj.getNumber(Configs.GEMS_GEM_POINTS)));



            // MARK: - TRACE ROUTE BUTTON ------------------------------------
            Button traceButt = findViewById(R.id.gmTraceRouteButt);
            traceButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try { Configs.playSound("click.mp3", false, ctx);
                    } catch (IOException e) { e.printStackTrace(); }

                    Configs.showPD("Tracing Route...", GemsMap.this);

                    // Adding LatLng points to markerPoints array
                    ParseGeoPoint gp = gObj.getParseGeoPoint(Configs.GEMS_GEM_LOCATION);
                    LatLng currLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    LatLng gemLatLng = new LatLng(gp.getLatitude(), gp.getLongitude());

                    markerPoints.add(currLatLng);
                    markerPoints.add(gemLatLng);
                    LatLng origin = markerPoints.get(0);
                    LatLng dest = markerPoints.get(1);

                    // Getting URL to the Google Directions API
                    String url = getDirectionsUrl(origin, dest);
                    DownloadTask downloadTask = new DownloadTask();
                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);
            }});


        } catch (ParseException e) { e.printStackTrace(); }



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        // Initializing
        markerPoints = new ArrayList<LatLng>();





        // MARK: - HYBRID MAP BUTTON ------------------------------------
        hybridButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {

              try { Configs.playSound("click.mp3", false, ctx);
              } catch (IOException e) { e.printStackTrace(); }

              mapView.setMapType(GoogleMap.MAP_TYPE_HYBRID);
              standardButt.setTextColor(Color.parseColor("#999999"));
              hybridButt.setTextColor(Color.parseColor("#FFFFFF"));
              satelliteButt.setTextColor(Color.parseColor("#999999"));
         }});


        // MARK: - STANDARD MAP BUTTON ------------------------------------
        standardButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try { Configs.playSound("click.mp3", false, ctx);
                } catch (IOException e) { e.printStackTrace(); }

                mapView.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                standardButt.setTextColor(Color.parseColor("#FFFFFF"));
                hybridButt.setTextColor(Color.parseColor("#999999"));
                satelliteButt.setTextColor(Color.parseColor("#999999"));
        }});


        // MARK: - SATELLITE MAP BUTTON ------------------------------------
        satelliteButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try { Configs.playSound("click.mp3", false, ctx);
                } catch (IOException e) { e.printStackTrace(); }

                mapView.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                standardButt.setTextColor(Color.parseColor("#999999"));
                hybridButt.setTextColor(Color.parseColor("#999999"));
                satelliteButt.setTextColor(Color.parseColor("#FFFFFF"));
        }});





        // MARK: - BACK BUTTON ------------------------------------
        Button backButt = findViewById(R.id.gmBackutt);
        backButt.setTypeface(Configs.gayatri);
        backButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              try { Configs.playSound("click.mp3", false, ctx);
              } catch (IOException e) { e.printStackTrace(); }

              finish();
          }});



        // Init AdMob banner
        AdView mAdView =  findViewById(R.id.admobBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


    }// end onCreate()







    // MARK: - GET CURRENT LOCATION -------------------------------------------------
    protected void getCurrentLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        currentLocation = locationManager.getLastKnownLocation(provider);

        if (currentLocation != null) {
        } else {
            locationManager.requestLocationUpdates(provider, 1000, 0, (android.location.LocationListener) this);
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        //remove location callback:
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates((android.location.LocationListener) this);
        currentLocation = location;
    }







    // MARK: - SHOW CURRENT LOCATION ON MAP --------------------------------------------------------
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapView = googleMap;

        // Enable MyLocation Layer of Google Map
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mapView.setMyLocationEnabled(true);
        mapView.setOnMarkerClickListener(this);

        // Get currentLocation
        getCurrentLocation();

        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

        // Show the current location in Google Map
        mapView.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        // Zoom in the Google Map
        mapView.animateCamera(CameraUpdateFactory.zoomTo(Configs.mapZoom));

        // Set default Map type
        mapView.setMapType(GoogleMap.MAP_TYPE_HYBRID);


        // Get Gem's name for the image
        String gemName = gObj.getString(Configs.GEMS_GEM_NAME);
        final int imageID = getResources().getIdentifier(gemName.toLowerCase(), "drawable", getPackageName());

        // Get Gem geoPoint
        ParseGeoPoint gp = gObj.getParseGeoPoint(Configs.GEMS_GEM_LOCATION);

        // Add Monster on Map
        mapView.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(gemName.toLowerCase(),100,100)))
                .title(gemName)
                .snippet(gObj.getNumber(Configs.GEMS_GEM_POINTS) + " points")
                .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                .position(new LatLng(gp.getLatitude(), gp.getLongitude())));
    }



    public Bitmap resizeMapIcons(String iconName, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }



    // MARK: - METHODS TO TRACE ROUTE ON THE MAP ------------------------------------------------------------
    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Sensor enabled
        String sensor = "sensor=false";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
    }


    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {

        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Invokes the thread for parsing the JSON data
            if (result.contains("You must use an API key to authenticate each request to Google Maps Platform APIs")){
                Configs.hidePD();
                Toast.makeText(GemsMap.this,"You must use an API key to authenticate each request to Google Maps Platform APIs",Toast.LENGTH_SHORT).show();
            }else {
                ParserTask parserTask = new ParserTask();
                parserTask.execute(result);
            }

        }
    }


    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(5);
                lineOptions.color(Color.CYAN);
            }


            // Finlly drawing polyline in the Google Map for the Route
            if (lineOptions!=null)
            mapView.addPolyline(lineOptions);
            Configs.hidePD();

        }
    }
    // END ----------------------------------------------------------------------------------------------------






    // MARK: - TAP ON A GEM (A MAP'S MARKER) -----------------------------------
    @Override
    public boolean onMarkerClick(final Marker marker) {

        try { Configs.playSound("click.mp3", false, ctx);
        } catch (IOException e) { e.printStackTrace(); }


        // Get current Location
        getCurrentLocation();


        // Get geoPoint of the Monster
        ParseGeoPoint gp = gObj.getParseGeoPoint(Configs.GEMS_GEM_LOCATION);
        final Location mLocation = new Location("");
        mLocation.setLatitude(gp.getLatitude());
        mLocation.setLongitude(gp.getLongitude());

        // EDIT THIS VALUE AS YOU WISH -> YOU HAVE TO GET AT LEAST 50 METERS CLOSE TO A GEM TO CATCH IT!
        final float radius = (float) 50.0;

        float distance = currentLocation.distanceTo(mLocation);

        // GET GEM IF YOU'RE 50 METERS AROUND IT!
        if (distance <= radius) {

            Intent i = new Intent(GemsMap.this, GetGem.class);
            Bundle extras = new Bundle();
            extras.putString("objectID", gObj.getObjectId());
            i.putExtras(extras);
            startActivity(i);

        // YOU'RE TOO FAR AWAY FROM A MONSTER, CAN'T CATCH IT!
        } else {
            Configs.simpleAlert("You're too far away from " + marker.getTitle().toUpperCase() + "!\nGet at least 50m meters close to it!", ctx);
        }



        return true;
    }





}// @end

















