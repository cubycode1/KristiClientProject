package com.domain.gems;

/*-----------------------------------

    - Gems -

    created by cubycode ©2017
    All Rights reserved

-----------------------------------*/

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.domain.gems.adapter.GamesAdapter;
import com.domain.gems.data.Game;
import com.domain.gems.interfaces.RecyclerviewClickListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Home extends AppCompatActivity implements LocationListener, RecyclerviewClickListener {

    /* Views */
    TextView titleTxt;

    private EditText etSearch;
    private RecyclerView recyclerView;
    private GamesAdapter adapter;
    private ArrayList<Game> gameArrayList = new ArrayList<>();


    /* Variables */
    Location currentLocation;
    LocationManager locationManager;
    List<ParseObject> gemsArray;
    MarshMallowPermission mmp = new MarshMallowPermission(this);


    // ON START() ----------------------------------------------------------------------
    @Override
    protected void onStart() {
        super.onStart();


        // Check it you're logged in
        if (ParseUser.getCurrentUser().getUsername() == null) {
            startActivity(new Intent(Home.this, Login.class));
        } else {
            // Check if Location service is permitted
            if (checkLocationPermission()) {
               // getCurrentLocation();

                // Ask for location permission
            } else {
                ActivityCompat.requestPermissions(Home.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }


        // Request Storge permission
        if (!mmp.checkPermissionForReadExternalStorage()) {
            mmp.requestPermissionForReadExternalStorage();
        }
    }


    // ON CREATE() -----------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();


        // Init TabBar buttons
        Button tab_one = findViewById(R.id.tab_two);
        Button tab_two = findViewById(R.id.tab_three);
        Button tab_four = findViewById(R.id.tab_four);
        Button tab_five = findViewById(R.id.tab_five);

        tab_one.setOnClickListener(v -> startActivity(new Intent(Home.this, Leaderboards.class)));

        tab_two.setOnClickListener(v -> startActivity(new Intent(Home.this, Identity.class)));
        tab_four.setOnClickListener(v -> startActivity(new Intent(Home.this, StatisticsActivity.class)));

        tab_five.setOnClickListener(v -> {
            startActivity(new Intent(Home.this, TeamActivity.class));
        });


        // Init views
        titleTxt = findViewById(R.id.hTitleTxt);
        // titleTxt.setTypeface(Configs.gayatri);

        etSearch = findViewById(R.id.etSearch);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //if (!s.toString().isEmpty())
                    queryGems();
            }
        });

        recyclerView = findViewById(R.id.hGemsListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new GamesAdapter(this);
        recyclerView.setAdapter(adapter);

        queryGems();


        // Init AdMob banner
        AdView mAdView = findViewById(R.id.admobBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }// end onCreate()


    // MARK: - CHECK LOCATION PERMISSION ------------------------------------------------------
    private boolean checkLocationPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation();
                } else {
                    Configs.simpleAlert("You've denied Location permission.\nIf you want to enable Location service, go into Settings, search for this app and enable Location permission.", Home.this);
                }
            }
        }
    }


    // MARK: - GET CURRENT LOCATION ------------------------------------------------------
    protected void getCurrentLocation() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_LOW);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        String provider = locationManager.getBestProvider(criteria, true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        currentLocation = locationManager.getLastKnownLocation(provider);
        if (currentLocation != null) {

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Home.this);
            Configs.newGems = prefs.getInt("newGems", Configs.newGems);
            Log.i("log-", "NEW GEMS (ON GET CURR LOCATION): " + Configs.newGems);
            if (Configs.newGems == 0) {
                generateNewGems();
            } else {
                // Call query
                queryGems();
            }

        } else {
            locationManager.requestLocationUpdates(provider, 1000, 0, this);
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        //remove location callback:
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(this);
        currentLocation = location;
        if (currentLocation != null) {


            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Home.this);
            Configs.newGems = prefs.getInt("newGems", Configs.newGems);
            Log.i("log-", "NEW GEMS (ON GET CURR LOCATION 2): " + Configs.newGems);
            if (Configs.newGems == 0) {
                generateNewGems();

            } else {
                // Call query
                //queryGems();
            }


            // NO GPS location found!
        } else {
            Configs.simpleAlert("Failed to get your Location.\nGo into Settings and make sure Location Service is enabled", Home.this);
        }

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


    // MARK: - GENERATE NEW GEMS AROUND YOUR CURRENT LOCATION ------------------------------------
    void generateNewGems() {
        Log.i("log-", "GEMS TO BE GENERATED: " + Configs.NEW_GEMS_TO_BE_GENERATED);

        for (int i = 0; i < Configs.NEW_GEMS_TO_BE_GENERATED; i++) {
            Configs.newGems++;

            ParseObject gObj = new ParseObject(Configs.GEMS_CLASS_NAME);
            LatLng coords = generateRandomCoordinates(Configs.MINIMUM_DISTANCE_FROM_YOU, Configs.MAXIMUM_DISTANCE_FROM_YOU);
            ParseGeoPoint gp = new ParseGeoPoint(coords.latitude, coords.longitude);

            // Get a random Gem's name
            Random r = new Random();
            int randomName = r.nextInt(Configs.gemNames.length);

            // Convert arrays into List
            List<String> names = new ArrayList<String>(Arrays.asList(Configs.gemNames));
            List<Integer> points = new ArrayList<Integer>(Arrays.<Integer>asList(Configs.gemPoints));

            // Save new Gems to your Parse Dashboard
            gObj.put(Configs.GEMS_GEM_NAME, names.get(randomName));
            gObj.put(Configs.GEMS_GEM_POINTS, points.get(randomName));
            gObj.put(Configs.GEMS_GEM_LOCATION, gp);
            gObj.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Log.i("log-", "GEM SAVED!");
                    } else {
                        Configs.simpleAlert(e.getMessage(), Home.this);
                    }
                }
            });


            // Save newGems global variable and call query
            if (Configs.newGems == Configs.NEW_GEMS_TO_BE_GENERATED) {
                // Save data
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Home.this);
                prefs.edit().putInt("newGems", Configs.newGems).apply();
                Log.i("log-", "NEW GEMS HAS BEEN SAVED (prefs): " + Configs.newGems);

                // Call query
                //queryGems();
            }
        }// end FOR loop
    }


    // MARK: - GENERATE RANDOM COORDINATES FROM YOUR CURRENT LOCATION ------------------------------
    public LatLng generateRandomCoordinates(int min, int max) {
        // Get the Current Location's longitude and latitude
        double currentLong = currentLocation.getLongitude();
        double currentLat = currentLocation.getLatitude();

        // 1 KiloMeter = 0.00900900900901° So, 1 Meter = 0.00900900900901 / 1000
        double meterCord = 0.00900900900901 / 1000;

        //Generate random Meters between the maximum and minimum Meters
        Random r = new Random();
        int randomMeters = r.nextInt(max + min);

        //then Generating Random numbers for different Methods
        int randomPM = r.nextInt(6);

        //Then we convert the distance in meters to coordinates by Multiplying number of meters with 1 Meter Coordinate
        double metersCordN = meterCord * (double) randomMeters;

        //here we generate the last Coordinates
        if (randomPM == 0) {
            return new LatLng(currentLat + metersCordN, currentLong + metersCordN);
        } else if (randomPM == 1) {
            return new LatLng(currentLat - metersCordN, currentLong - metersCordN);
        } else if (randomPM == 2) {
            return new LatLng(currentLat + metersCordN, currentLong - metersCordN);
        } else if (randomPM == 3) {
            return new LatLng(currentLat - metersCordN, currentLong + metersCordN);
        } else if (randomPM == 4) {
            return new LatLng(currentLat, currentLong - metersCordN);
        } else {
            return new LatLng(currentLat - metersCordN, currentLong);
        }
    }


    // MARK: - QUERY GEMS ----------------------------------------------------------------
    void queryGems() {
        if (etSearch.getText().toString().equals("")) {
            Configs.showPD("SCANNING AREA...", Home.this);
        }

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Games");
//        query.orderByAscending("title");
        query.whereMatches("title", etSearch.getText().toString(),"i");
        query.findInBackground((objects, error) -> {
            if (error == null) {
//                    gemsArray = objects;
                gameArrayList.clear();
                for (ParseObject user : objects) {
                    String name = user.getString("title");
                    Game team1 = new Game();
                    team1.setGameName(name);
                    team1.setParseObject(user);
                    gameArrayList.add(team1);
                }
                if (gameArrayList.size() > 0) {
                    adapter.setList(gameArrayList);
                }
                Configs.hidePD();

                // Error in query
            } else {
                Configs.hidePD();
                Configs.simpleAlert(error.getMessage(), Home.this);
            }
        });

    }


    @Override
    public void onClickListItem(int position) {
        try {
            Configs.playSound("click.mp3", false, Home.this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Game gObj = gameArrayList.get(position);
        Intent i = new Intent(Home.this, GemsMap.class);
        Bundle extras = new Bundle();
        extras.putString("objectID", gObj.getParseObject().getObjectId());
//        extras.putString("currentLocLat", currentLocation.getLatitude()+"");
//        extras.putString("currentLocLon", currentLocation.getLongitude()+"");

        i.putExtras(extras);
        startActivity(i);
    }



}//@end
