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
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class SignUp extends AppCompatActivity implements LocationListener, AdapterView.OnItemSelectedListener {


    /* Views */
    EditText usernameTxt, passwordTxt;
    Spinner spinner;


    /* Variables */
    Typeface gayatri = Configs.gayatri;
    Location currentLocation;
    LocationManager locationManager;
    ParseGeoPoint userLocation;

    private ArrayList teamList = new ArrayList();
    private String selectedTeam = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        //Check if Location service is permitted
        if (checkLocationPermission()) {
            getCurrentLocation();

            // Ask for location permission
        } else {
            ActivityCompat.requestPermissions(SignUp.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        // Hide ActionBar
        getSupportActionBar().hide();


        // Init views
        usernameTxt = findViewById(R.id.suUsernameTxt);
        passwordTxt = findViewById(R.id.suPasswordTxt);
        spinner = findViewById(R.id.spTeams);
//        usernameTxt.setTypeface(gayatri);
//        passwordTxt.setTypeface(gayatri);
        TextView topTxt = findViewById(R.id.topTxt2);
        //    topTxt.setTypeface(gayatri);

        getTeams();


        // SIGN UP BUTTON ------------------------------------------------------------------------
        Button signupButt = findViewById(R.id.suSignUpButt);
        //    signupButt.setTypeface(gayatri);
        signupButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // CURRENT LOCATION DETECTED!
                if (currentLocation != null) {

                    if (usernameTxt.getText().toString().matches("") || passwordTxt.getText().toString().matches("")) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(SignUp.this);
                        builder.setMessage("You must fill all the fields to Sign Up!")
                                .setTitle(R.string.app_name)
                                .setPositiveButton("OK", null);
                        AlertDialog dialog = builder.create();
                        dialog.setIcon(R.drawable.logo);
                        dialog.show();


                    } else {
                        Configs.showPD("Please wait...", SignUp.this);
                        dismisskeyboard();

                        final ParseUser currUser = new ParseUser();
                        currUser.setUsername(usernameTxt.getText().toString());
                        currUser.setPassword(passwordTxt.getText().toString());

                        // Register this user's current location, so you can place some monsters around his area
                        if (currentLocation.getLatitude() != 0.0) {
                            currUser.put(Configs.USER_CURRENT_LOCATION, userLocation);
                        }

                        currUser.put(Configs.USER_GEMS_COLLECTED, 0);
                        currUser.put(Configs.USER_POINTS, 0);
                        currUser.put(Configs.TEAM_CATEGORY, selectedTeam);

                        currUser.signUpInBackground(error -> {
                            if (error == null) {

                                // Save default avatar
                                Bitmap bitmap = BitmapFactory.decodeResource(SignUp.this.getResources(), R.drawable.logo);
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                byte[] byteArray = stream.toByteArray();
                                ParseFile imageFile = new ParseFile("avatar.jpg", byteArray);
                                currUser.put(Configs.USER_AVATAR, imageFile);
                                currUser.saveInBackground();


                                Configs.hidePD();
                                Intent intent = new Intent(SignUp.this, Home.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            } else {
                                Configs.hidePD();
                                Configs.simpleAlert(error.getMessage(), SignUp.this);
                            }
                        });
                    }


                    // NO CURRENT LOCATION DETECTED!
                } else {
                    Configs.simpleAlert("Failed to get your current location. Please make sure Location service is enabled in Settings, or move to another location.", SignUp.this);
                }

            }
        });


        // MARK: - TERMS OF USE BUTTON ----------------------------------------------------------
        Button touButt = findViewById(R.id.suTermsOfUseButt);
        // touButt.setTypeface(gayatri);
        touButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUp.this, TermsOfUse.class));
            }
        });


        // MARK: - DISMISS BUTTON ---------------------------------------------------------------
        Button dismissButt = findViewById(R.id.suDismissButt);
        dismissButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }// end onCreate()

    private void getTeams() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Teams");
        query.addAscendingOrder(Configs.TEAM_CATEGORY);
        query.findInBackground((teams, e) -> {
            if (e == null) {
                //Object was successfully retrieved
                for (ParseObject team : teams) {
                    String name = team.getString(Configs.TEAM_CATEGORY);
                    teamList.add(name);
                }
                ArrayAdapter aa = new ArrayAdapter(this, R.layout.spinner_item, teamList);
                aa.setDropDownViewResource(android.R.layout.simple_list_item_1);
                //Setting the ArrayAdapter data on the Spinner
                spinner.setAdapter(aa);
                spinner.setOnItemSelectedListener(this);

            } else {
                // something went wrong
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    // MARK: - CHECK LOCATION PERMISSION ------------------------------------------------------
    private boolean checkLocationPermission() {
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation();
                } else {
                    Configs.simpleAlert("You've denied Location permission.\nYou must enable Location service to play this game: go into Settings, search for this app and enable Location permission.", SignUp.this);
                }
                return;
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
        String provider = locationManager.getBestProvider(criteria, true);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        currentLocation = locationManager.getLastKnownLocation(provider);

        if (currentLocation != null) {

            // Set userLocation GeoPoint
            userLocation = new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());

        } else {
            locationManager.requestLocationUpdates(provider, 1000, 0, this);
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        //remove location callback:
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(this);

        currentLocation = location;

        if (currentLocation != null) {

            // Set userLocation GeoPoint
            userLocation = new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());

            // NO GPS location found!
        } else {
            Configs.simpleAlert("Failed to get your Location.\nGo into Settings and make sure Location Service is enabled, otherwise you won't be able to pòay this game.", SignUp.this);
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


    // DISMISS KEYBOARD
    public void dismisskeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(usernameTxt.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(passwordTxt.getWindowToken(), 0);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedTeam = teamList.get(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}//@end
