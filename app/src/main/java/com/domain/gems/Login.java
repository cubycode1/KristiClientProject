package com.domain.gems;

/*-----------------------------------

    - Gems -

    created by cubycode ©2017
    All Rights reserved

-----------------------------------*/

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Login extends AppCompatActivity implements LocationListener {

    /* Views */
    EditText usernameTxt;
    EditText passwordTxt;


    /* Variables */
    Location currentLocation;
    LocationManager locationManager;
    ParseGeoPoint userLocation;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        //Check if Location service is permitted
        if (checkLocationPermission()) {
            getCurrentLocation();

        // Ask for location permission
        } else {
            ActivityCompat.requestPermissions(Login.this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }




        // Hide ActionBar
        getSupportActionBar().hide();



        // Init views
        usernameTxt = findViewById(R.id.suUsernameTxt);
        passwordTxt = findViewById(R.id.suPasswordTxt);
      //  usernameTxt.setTypeface(Configs.gayatri);
       // passwordTxt.setTypeface(Configs.gayatri);
       // TextView titleTxt = findViewById(R.id.loginTitleTxt);
   //     titleTxt.setTypeface(Configs.gayatri);



        // MARK: - FACEBOOK LOGIN BUTTON ------------------------------------------------------------------
        Button fbButt = findViewById(R.id.facebookButt);
        fbButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<String> permissions = Arrays.asList("public_profile");
                    Configs.showPD("Please wait...", Login.this);

                    ParseFacebookUtils.logInWithReadPermissionsInBackground(Login.this, permissions, new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if (user == null) {
                                Log.i("log-", "Uh oh. The user cancelled the Facebook login.");
                                Configs.hidePD();

                            } else if (user.isNew()) {
                                getUserDetailsFromFB();

                            } else {
                                Log.i("log-", "RETURNING User logged in through Facebook!");
                                Configs.hidePD();
                                openHome();
                            }}});
                }});




            // This code generates a KeyHash that you'll have to copy from your Logcat console and paste it into Key Hashes field in the 'Settings' section of your Facebook Android App
            try {
                PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
                for (Signature signature : info.signatures) {
                    MessageDigest md = MessageDigest.getInstance("SHA");
                    md.update(signature.toByteArray());
                    Log.i("log-", "keyhash: " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
                }
            } catch (PackageManager.NameNotFoundException e) {
            } catch (NoSuchAlgorithmException e) {}








            // MARK: - LOGIN BUTTON ------------------------------------------------------------------
            Button loginButt = findViewById(R.id.suSignUpButt);
          //  loginButt.setTypeface(Configs.gayatri);
            loginButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Configs.showPD("Please wait...", Login.this);

                    ParseUser.logInInBackground(usernameTxt.getText().toString(), passwordTxt.getText().toString(),
                            new LogInCallback() {
                                public void done(ParseUser user, ParseException error) {
                                    if (user != null) {
                                        Configs.hidePD();
                                        openHome();
                                    } else {
                                        Configs.hidePD();
                                        Configs.simpleAlert(error.getMessage(), Login.this);
                    }}});
                }});





        // MARK: - SIGN UP BUTTON ------------------------------------------------------------------
        Button signupButt = findViewById(R.id.signUpButt);
     //   signupButt.setTypeface(Configs.gayatri);
            signupButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Login.this, SignUp.class));
        }});



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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation();
                } else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(Login.this);
                    alert.setMessage("You've denied Location permission.\nYou must enable Location service to play this game: go into Settings, search for this app and enable Location permission.")
                            .setTitle(R.string.app_name)
                            .setPositiveButton("OK", null)
                            .setIcon(R.drawable.logo);
                    alert.create().show();
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        currentLocation = locationManager.getLastKnownLocation(provider);

        if (currentLocation != null) {

            userLocation = new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());

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

            userLocation = new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());

        // NO GPS location found!
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(Login.this);
            alert.setMessage("Failed to get your Location.\nGo into Settings and make sure Location Service is enabled")
                    .setTitle(R.string.app_name)
                    .setPositiveButton("OK", null)
                    .setIcon(R.drawable.logo);
            alert.create().show();
        }

    }

    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override public void onProviderEnabled(String provider) {}
    @Override public void onProviderDisabled(String provider) {}











    // MARK: - FACEBOOK GRAPH REQUEST --------------------------------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }


    void getUserDetailsFromFB() {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),new GraphRequest.GraphJSONObjectCallback(){
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                String facebookID = "";
                String name = "";
                String username = "";

                try{
                    name = object.getString("name");
                    facebookID = object.getString("id");

                    String[] one = name.toLowerCase().split(" ");
                    for (String word : one) { username += word; }
                    Log.i("log-", "USERNAME: " + username + "\n");
                    Log.i("log-", "name: " + name + "\n");

                } catch(JSONException e){ e.printStackTrace(); }


                // SAVE NEW USER IN YOUR PARSE DASHBOARD -> USER CLASS
                final String finalFacebookID = facebookID;
                final String finalUsername = username;
                final String finalName = name;

                final ParseUser currUser = ParseUser.getCurrentUser();
                currUser.put(Configs.USER_USERNAME, finalUsername);

                // Register this user's current location, so you can place some monsters around his area
                if (currentLocation.getLatitude() != 0.0) { currUser.put(Configs.USER_CURRENT_LOCATION, userLocation); }

                currUser.put(Configs.USER_GEMS_COLLECTED, 0);
                currUser.put(Configs.USER_POINTS, 0);

                currUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Log.i("log-", "NEW USER signed up and logged in through Facebook...");


                        // Get and Save avatar from Facebook
                        new Timer().schedule(new TimerTask() {
                            @Override public void run() {
                                try {
                                    URL imageURL = new URL("https://graph.facebook.com/" + finalFacebookID + "/picture?type=large");
                                    Bitmap avatarBm = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());

                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    avatarBm.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                    byte[] byteArray = stream.toByteArray();
                                    ParseFile imageFile = new ParseFile("image.jpg", byteArray);
                                    currUser.put(Configs.USER_AVATAR, imageFile);
                                    currUser.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException error) {
                                            Log.i("log-", "... AND AVATAR SAVED!");
                                            Configs.hidePD();

                                            openHome();
                                    }});
                                } catch (IOException error) { error.printStackTrace(); }

                        }}, 1000); // 1 second


                }}); // end saveInBackground


            }}); // end graphRequest


            Bundle parameters = new Bundle();
            parameters.putString("fields", "email, name, picture.type(large)");
            request.setParameters(parameters);
            request.executeAsync();
        }

    private void openHome() {
        Intent intent = new Intent(Login.this,Home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    // END FACEBOOK GRAPH REQUEST --------------------------------------------------------------------




    }//@end



