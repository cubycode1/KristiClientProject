package com.domain.gems;

/*-----------------------------------

    - Gems -

    created by cubycode ©2017
    All Rights reserved

-----------------------------------*/

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;

import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.domain.gems.quizmodule.QuizActivity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static android.util.Log.d;
import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class GemsMap extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, LocationListener {

    /* Views */
    private GoogleMap mapView;
    TextView titleTxt, infoTxt;
    Button hybridButt, standardButt, satelliteButt;
    private final ArrayList<ParseObject> pois = new ArrayList<>();
    /* Variables */
    public static Location currentLocation;
    LocationManager locationManager;
    ParseObject gObj;
    Context ctx = this;
    public static Location quizLocation;
    public static String quizTitle = "";
    private boolean isTeacherShowed = false;
    private ArrayList<LatLng> trackLocationFromPointAToB = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gems_map);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        // Hide ActionBar
        Objects.requireNonNull(getSupportActionBar()).hide();
        // Init views
        titleTxt = findViewById(R.id.gmTitleTxt);
        //  titleTxt.setTypeface(Configs.gayatri);
        infoTxt = findViewById(R.id.gmInfoTxt);
        // infoTxt.setTypeface(Configs.gayatri);
        hybridButt = findViewById(R.id.hybridButt);
        // hybridButt.setTypeface(Configs.gayatri);
        standardButt = findViewById(R.id.standardButt);
        //  standardButt.setTypeface(Configs.gayatri);
        satelliteButt = findViewById(R.id.satelliteButt);
        //  satelliteButt.setTypeface(Configs.gayatri);


        Bundle extras = getIntent().getExtras();
        String objectID = null;
        if (extras != null) {
            objectID = extras.getString("objectID");
        }

        gObj = ParseObject.createWithoutData("Games", objectID);

        try {
            gObj.fetchIfNeeded().getParseObject("Games");
            getCurrentLocation();

        } catch (ParseException e) {
            e.printStackTrace();
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }


        // MARK: - HYBRID MAP BUTTON ------------------------------------
        hybridButt.setOnClickListener(view -> {

            try {
                Configs.playSound("click.mp3", false, ctx);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mapView.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            standardButt.setTextColor(Color.parseColor("#999999"));
            hybridButt.setTextColor(Color.parseColor("#FFFFFF"));
            satelliteButt.setTextColor(Color.parseColor("#999999"));
        });

        // MARK: - TRACE ROUTE BUTTON ------------------------------------
        Button traceButt = findViewById(R.id.gmTraceRouteButt);
        traceButt.setOnClickListener(view -> {
            startActivity(new Intent(GemsMap.this, QuizActivity.class));
//                try {
//                    Configs.playSound("click.mp3", false, ctx);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                Configs.showPD("Tracing Route...", GemsMap.this);
//
//                // Adding LatLng points to markerPoints array
//                ParseGeoPoint gp = gObj.getParseGeoPoint(Configs.GEMS_GEM_LOCATION);
//                LatLng currLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
////                LatLng gemLatLng = new LatLng(Double.parseDouble(currentLocLat), Double.parseDouble(currentLocLon));
//                LatLng gemLatLng = new LatLng(currLatLng.latitude, currLatLng.longitude);
//                markerPoints.add(currLatLng);
//                markerPoints.add(gemLatLng);
//                LatLng origin = markerPoints.get(0);
//                LatLng dest = markerPoints.get(1);
//
//                // Getting URL to the Google Directions API
//                String url = getDirectionsUrl(origin, dest);
//                DownloadTask downloadTask = new DownloadTask();
//                // Start downloading json data from Google Directions API
//                downloadTask.execute(url);
        });

        // MARK: - STANDARD MAP BUTTON ------------------------------------
        standardButt.setOnClickListener(view -> {

            try {
                Configs.playSound("click.mp3", false, ctx);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mapView.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            standardButt.setTextColor(Color.parseColor("#FFFFFF"));
            hybridButt.setTextColor(Color.parseColor("#999999"));
            satelliteButt.setTextColor(Color.parseColor("#999999"));
        });


        // MARK: - SATELLITE MAP BUTTON ------------------------------------
        satelliteButt.setOnClickListener(view -> {

            try {
                Configs.playSound("click.mp3", false, ctx);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mapView.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            standardButt.setTextColor(Color.parseColor("#999999"));
            hybridButt.setTextColor(Color.parseColor("#999999"));
            satelliteButt.setTextColor(Color.parseColor("#FFFFFF"));
        });


        // MARK: - BACK BUTTON ------------------------------------
        Button backButt = findViewById(R.id.gmBackutt);
        //  backButt.setTypeface(Configs.gayatri);
        backButt.setOnClickListener(view -> {
            try {
                Configs.playSound("click.mp3", false, ctx);
            } catch (IOException e) {
                e.printStackTrace();
            }

            finish();
        });


        // Init AdMob banner
        AdView mAdView = findViewById(R.id.admobBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


    }// end onCreate()


    // MARK: - GET CURRENT LOCATION -------------------------------------------------
    protected void getCurrentLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String provider = locationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        currentLocation = locationManager.getLastKnownLocation(provider);
        locationManager.requestLocationUpdates(provider, 0, 10, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getCurrentLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        //remove location callback:
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        trackLocationFromPointAToB.add(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()));
        currentLocation = location;
//        calculateDistance("");
        d("Location", "onLocationChanged: " + "Lat:" + location.getLatitude() + " Lng: " + location.getLongitude());
        makeText(this, "" + location.getLatitude() + location.getLongitude(), LENGTH_SHORT).show();

        final Location firstPoint = new Location("");
        firstPoint.setLatitude(pois.get(0).getParseGeoPoint("POIs").getLatitude());
        firstPoint.setLongitude(pois.get(0).getParseGeoPoint("POIs").getLongitude());

        final Location secondPoint = new Location("");
        firstPoint.setLatitude(pois.get(1).getParseGeoPoint("POIs").getLatitude());
        firstPoint.setLongitude(pois.get(1).getParseGeoPoint("POIs").getLongitude());

        final Location thirdPoint = new Location("");
        firstPoint.setLatitude(pois.get(2).getParseGeoPoint("POIs").getLatitude());
        firstPoint.setLongitude(pois.get(2).getParseGeoPoint("POIs").getLongitude());

        float radius = (float) 50.0;
        float firstDistance = location.distanceTo(firstPoint);
        float secondDistance = location.distanceTo(secondPoint);
        float thirdDistance = location.distanceTo(thirdPoint);


        if (firstDistance <= radius) {
            findParseObjInfo(pois.get(0));

        } else if (secondDistance <= radius) {
            //TODO add a poly line for user location track realtime
//            PolylineOptions polylineOptions = new PolylineOptions();
//            polylineOptions.addAll(trackLocationFromPointAToB);
//            polylineOptions.color(Color.CYAN);
//            polylineOptions.width(5);
//            mapView.addPolyline(polylineOptions);
            findParseObjInfo(pois.get(1));
        } else if (thirdDistance <= radius) {
            findParseObjInfo(pois.get(2));
        }

        quizLocation = location;

    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    private void getPOIs() {
        Configs.showPD("Please Wait...", GemsMap.this);

        // Get Gem's name for the image
        String gemName = gObj.getString("QuizTitle");

        ParseQuery<ParseUser> query = ParseQuery.getQuery("POIs");
        query.orderByAscending("order");
        query.whereEqualTo("fk_game_id", ParseObject.createWithoutData("Games", gObj.getObjectId()));
        query.findInBackground((routes, e) -> {
            if (e == null) {

                pois.addAll(routes);
                isTeacherShowed = pois.get(0).getBoolean("show");

                calculateDistanceFromStartingPoint(gemName);

                Configs.hidePD();
            } else {
                // Something went wrong.
                Configs.hidePD();
                makeText(this, e.getMessage(), LENGTH_SHORT).show();
            }
        });
    }

    private void calculateDistanceFromStartingPoint(String gemName) {
        final Location mLocation = new Location("");
        mLocation.setLatitude(pois.get(0).getParseGeoPoint("POIs").getLatitude());
        mLocation.setLongitude(pois.get(0).getParseGeoPoint("POIs").getLongitude());

        // EDIT THIS VALUE AS YOU WISH -> YOU HAVE TO GET AT LEAST 50 METERS CLOSE TO A GEM TO CATCH IT!
        final float radius = (float) 50.0;

        float distance = currentLocation.distanceTo(mLocation);


        // GET GEM IF YOU'RE 20 METERS AROUND IT!
        if (distance <= radius && isTeacherShowed) {
            for (int i = 0; i < pois.size(); i++) {
                //TODO get direction api and map polygons between two points
                mapView.addMarker(new MarkerOptions()
                        .icon(getMarker(i))
                        .title(gemName)
//                        .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                        .position(new LatLng(pois.get(i).getParseGeoPoint("POIs").getLatitude(), pois.get(i).getParseGeoPoint("POIs").getLongitude())))
                ;
            }
            String url = getDirectionsUrl(new LatLng(pois.get(0).getParseGeoPoint("POIs").getLatitude(), pois.get(0).getParseGeoPoint("POIs").getLongitude()), new LatLng(pois.get(1).getParseGeoPoint("POIs").getLatitude(), pois.get(1).getParseGeoPoint("POIs").getLongitude()));
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(url);
        } else {
            mapView.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(String.valueOf(R.drawable.start), 100, 100)))
                    .title(gemName)
//                    .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                    .position(new LatLng(pois.get(0).getParseGeoPoint("POIs").getLatitude(), pois.get(0).getParseGeoPoint("POIs").getLongitude())));
        }

    }

    private BitmapDescriptor getMarker(int i) {
        switch (pois.get(i).getString("order")) {
            case "1":
                return BitmapDescriptorFactory.fromBitmap(resizeMapIcons(String.valueOf(R.drawable.start), 100, 100));
            case "2":
                return BitmapDescriptorFactory.fromBitmap(resizeMapIcons(String.valueOf(R.drawable.controlpoint), 100, 100));
            case "3":
                return BitmapDescriptorFactory.fromBitmap(resizeMapIcons(String.valueOf(R.drawable.finish), 100, 100));
            default:
                return null;
        }
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

        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

        // Show the current location in Google Map
        mapView.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        // Zoom in the Google Map
        mapView.animateCamera(CameraUpdateFactory.zoomTo(Configs.mapZoom));

        // Set default Map type
        mapView.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        getPOIs();
    }


    public Bitmap resizeMapIcons(String iconName, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", getPackageName()));
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }


    // MARK: - METHODS TO TRACE ROUTE ON THE MAP ------------------------------------------------------------
    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Sensor enabled
        String sensor = "sensor=false";
        //multiple paths
        String alternatives = "alternatives=true";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + alternatives;
        // Output format
        String output = "json";
        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=AIzaSyAAusNXBEdExN39_EHpuV_BzMR6eGxhJz0";
    }


    /**
     * A method to download json data from url
     */
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

            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (iStream != null) {
                iStream.close();
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return data;
    }


    // Fetches data from url passed
    @SuppressLint("StaticFieldLeak")
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Dowoading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Invokes the thread for parsing the JSON data
            if (result.contains("You must use an API key to authenticate each request to Google Maps Platform APIs")) {
                Configs.hidePD();
                makeText(GemsMap.this, "You must use an API key to authenticate each request to Google Maps Platform APIs", LENGTH_SHORT).show();
            } else {
                ParserTask parserTask = new ParserTask();
                parserTask.execute(result);
            }

        }
    }


    /**
     * A class to parse the Google Places in JSON format
     */
    @SuppressLint("StaticFieldLeak")
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
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th rAioute
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(Objects.requireNonNull(point.get("lat")));
                    double lng = Double.parseDouble(Objects.requireNonNull(point.get("lng")));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(5);
                lineOptions.color(Color.BLACK);
            }


            // Finlly drawing polyline in the Google Map for the Route
            if (lineOptions != null)
                mapView.addPolyline(lineOptions);
            Configs.hidePD();

        }
    }
    // END ----------------------------------------------------------------------------------------------------


    // MARK: - TAP ON A GEM (A MAP'S MARKER) -----------------------------------
    @Override
    public boolean onMarkerClick(final Marker marker) {

        try {
            Configs.playSound("click.mp3", false, ctx);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        for (int i = 0; i < pois.size(); i++) {
//           ParseObject  obj =  pois.get(i);
//           if(marker.getPosition().latitude == obj.getParseGeoPoint("POIs").getLatitude()
//           &&marker.getPosition().longitude == obj.getParseGeoPoint("POIs").getLongitude()){
//               findParseObjInfo(obj);
//               break;
//           }
//        }

        // Get current Location
        return true;
    }

    private void findParseObjInfo(ParseObject parseObject) {
        Configs.showPD("Please Wait...", GemsMap.this);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("POIs");
        query.getInBackground(parseObject.getObjectId(), (obj, e) -> {
            if (e == null) {
//                Configs.simpleAlert(obj.getString("YoutubeURL"), this);
                quizTitle = obj.getString("quiz_title");

                viewQuizDialog(GemsMap.this, obj.getString("YoutubeURL"), quizTitle);
                Configs.hidePD();
            } else {
                // Something went wrong.
                Configs.hidePD();
                makeText(this, e.getMessage(), LENGTH_SHORT).show();
            }
        });
    }

    public void viewQuizDialog(Context context, String youtubeLink, String quizTitle) {
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_view_quiz_dialoge, null);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.show();
        TextView tvYoutubeLink = view.findViewById(R.id.tv_youtube_link);
        Button btnViewQuiz = view.findViewById(R.id.btn_view_quiz);
        TextView tvQuizTitle = view.findViewById(R.id.tv_quiz_title);
        tvYoutubeLink.setText(youtubeLink);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (youtubeLink != null) {
                tvYoutubeLink.setText(Html.fromHtml(youtubeLink, Html.FROM_HTML_MODE_COMPACT));
            }
        } else {
            if (youtubeLink != null) {
                tvYoutubeLink.setText(Html.fromHtml(youtubeLink));
            }
        }
        Linkify.addLinks(tvYoutubeLink, Linkify.ALL);
        tvYoutubeLink.setMovementMethod(LinkMovementMethod.getInstance());
        tvQuizTitle.setText(quizTitle);
        btnViewQuiz.setOnClickListener(v -> {
            String userId = getIntent().getExtras().getString("user_id","");
            String teamId = getIntent().getExtras().getString("team_id","");
            Intent intent = new Intent(GemsMap.this, QuizActivity.class);
            intent.putExtra("user_id",userId);
            intent.putExtra("team_id",teamId);
            startActivity(intent);
            dialog.dismiss();
        });
    }

}// @end

















