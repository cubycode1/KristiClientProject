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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
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

public class Home extends AppCompatActivity   implements LocationListener {

    /* Views */
    TextView titleTxt;



    /* Variables */
    Location currentLocation;
    LocationManager locationManager;
    List<ParseObject>gemsArray;
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
                getCurrentLocation();

            // Ask for location permission
            } else {
                ActivityCompat.requestPermissions(Home.this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }


        // Request Storge permission
        if(!mmp.checkPermissionForReadExternalStorage()) {
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

        tab_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Home.this, TopHunters.class));
        }});

        tab_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Home.this, Identity.class));
        }});



        // Init views
        titleTxt = findViewById(R.id.hTitleTxt);
        titleTxt.setTypeface(Configs.gayatri);




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
                queryGems();
            }


        // NO GPS location found!
        } else {
            Configs.simpleAlert("Failed to get your Location.\nGo into Settings and make sure Location Service is enabled", Home.this);
        }

    }

    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override public void onProviderEnabled(String provider) {}
    @Override public void onProviderDisabled(String provider) {}








    // MARK: - GENERATE NEW GEMS AROUND YOUR CURRENT LOCATION ------------------------------------
    void generateNewGems() {
        Log.i("log-", "GEMS TO BE GENERATED: " + Configs.NEW_GEMS_TO_BE_GENERATED);

        for (int i = 0; i<Configs.NEW_GEMS_TO_BE_GENERATED; i++) {
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
                    if (e==null) {
                        Log.i("log-", "GEM SAVED!");
                    } else {
                        Configs.simpleAlert(e.getMessage(), Home.this);
            }}});


            // Save newGems global variable and call query
            if (Configs.newGems == Configs.NEW_GEMS_TO_BE_GENERATED) {
                // Save data
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Home.this);
                prefs.edit().putInt("newGems", Configs.newGems).apply();
                Log.i("log-", "NEW GEMS HAS BEEN SAVED (prefs): " + Configs.newGems);

                // Call query
                queryGems();
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
            return  new LatLng(currentLat + metersCordN, currentLong + metersCordN);
        }else if (randomPM == 1) {
            return new LatLng(currentLat - metersCordN, currentLong - metersCordN);
        }else if (randomPM == 2) {
            return new LatLng(currentLat + metersCordN, currentLong - metersCordN);
        }else if (randomPM == 3) {
            return new LatLng(currentLat - metersCordN, currentLong + metersCordN);
        }else if (randomPM == 4) {
            return new LatLng(currentLat, currentLong - metersCordN);
        } else {
            return new LatLng(currentLat - metersCordN, currentLong);
        }
    }








    // MARK: - QUERY GEMS ----------------------------------------------------------------
    void queryGems() {
        Configs.showPD("SCANNING AREA...", Home.this);

        // Get a PFGeoPoint out of your current location's coordinates
        ParseGeoPoint geoPoint = new ParseGeoPoint(currentLocation.getLatitude(),currentLocation.getLongitude());

        ParseQuery query = new ParseQuery(Configs.GEMS_CLASS_NAME);
        query.whereWithinKilometers(Configs.GEMS_GEM_LOCATION, geoPoint, 10);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    gemsArray = objects;
                    Configs.hidePD();

                    // CUSTOM LIST ADAPTER
                    class ListAdapter extends BaseAdapter {
                        private Context context;
                        public ListAdapter(Context context, List<ParseObject> objects) {
                            super();
                            this.context = context;
                        }


                        // CONFIGURE CELL
                        @Override
                        public View getView(int position, View cell, ViewGroup parent) {
                            if (cell == null) {
                                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                assert inflater != null;
                                cell = inflater.inflate(R.layout.cell_gem, null);
                            }

                            // Get Parse object
                            ParseObject gObj = gemsArray.get(position);

                            // Get Gem Name
                            TextView nameTxt = cell.findViewById(R.id.cgGemNametxt);
                            nameTxt.setTypeface(Configs.gayatri);
                            nameTxt.setText(gObj.getString(Configs.GEMS_GEM_NAME).toUpperCase());

                            // Get Gem Points
                            TextView pointsTxt = cell.findViewById(R.id.cgGemPointsTxt);
                            pointsTxt.setTypeface(Configs.gayatri);
                            pointsTxt.setText("Points: " + String.valueOf(gObj.getNumber(Configs.GEMS_GEM_POINTS)) );

                            // Get Gem Image
                            String gemName = gObj.getString(Configs.GEMS_GEM_NAME);
                            final int id = getResources().getIdentifier(gemName.toLowerCase(), "drawable", getPackageName());
                            ImageView gemImg =  cell.findViewById(R.id.cgGemImg);
                            gemImg.setImageResource(id);


                            // Get Gem distance from Current Location
                            ParseGeoPoint gp = gObj.getParseGeoPoint(Configs.GEMS_GEM_LOCATION);
                            Location mLocation = new Location("");
                            mLocation.setLatitude(gp.getLatitude());
                            mLocation.setLongitude(gp.getLongitude());
                            float distInMeters = currentLocation.distanceTo(mLocation);

                            TextView distTxt =  cell.findViewById(R.id.cgGemDistanceTxt);
                            distTxt.setTypeface(Configs.gayatri);
                            distTxt.setText("Distance: " + String.format("%.0f m", distInMeters));


                            // Set localize Txt typeface
                            TextView localizeTxt =  cell.findViewById(R.id.cgLocalizetxt);
                            localizeTxt.setTypeface(Configs.gayatri);


                            return cell;
                        }

                        @Override public int getCount() { return gemsArray.size(); }
                        @Override public Object getItem(int position) { return gemsArray.get(position); }
                        @Override public long getItemId(int position) { return position; }
                    }

                    // Init ListView and set its Adapter
                    ListView aList = (ListView) findViewById(R.id.hGemsListView);
                    aList.setAdapter(new ListAdapter(Home.this, gemsArray));
                    aList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                            // Play sound
                            try { Configs.playSound("click.mp3", false, Home.this);
                            } catch (IOException e) { e.printStackTrace(); }


                            ParseObject gObj = gemsArray.get(position);
                            Intent i = new Intent(Home.this, GemsMap.class);
                            Bundle extras = new Bundle();
                            extras.putString("objectID", gObj.getObjectId());
                            i.putExtras(extras);
                            startActivity(i);
                    }});


                // Error in query
                } else {
                    Configs.hidePD();
                    Configs.simpleAlert(error.getMessage(), Home.this);
        }}});

    }




}//@end
