package com.domain.gems;

/*-----------------------------------

    - Gems -

    created by cubycode ©2017
    All Rights reserved

-----------------------------------*/

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetGem extends AppCompatActivity implements SensorEventListener {

    /* Views */
    Button gemButt;


    /* Variables */
    ParseObject gObj;
    float currentDegree = 0;
    SensorManager mSensorManager;
    Context ctx = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_gem);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();



        // Init views
        TextView infoTxt = findViewById(R.id.ggInfoTxt);
      //  infoTxt.setTypeface(Configs.gayatri);
        gemButt = findViewById(R.id.ggGemButt);

        // Initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> availableSensorList = null;
        if (mSensorManager != null) {
            availableSensorList = mSensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
        }

        // Get objectID from previous .java
        Bundle extras = getIntent().getExtras();
        String objectID = extras.getString("objectID");
        gObj = ParseObject.createWithoutData(Configs.GEMS_CLASS_NAME, objectID);
        try {
            gObj.fetchIfNeeded().getParseObject(Configs.GEMS_CLASS_NAME);

            // Get Gem image
            String gemName = gObj.getString(Configs.GEMS_GEM_NAME);
            final int id = getResources().getIdentifier(gemName.toLowerCase(), "drawable", getPackageName());
            gemButt.setBackgroundResource(id);

            if (availableSensorList == null || availableSensorList.isEmpty()) {
                gemButt.setVisibility(View.VISIBLE);
            } else {
                gemButt.setVisibility(View.INVISIBLE);
            }


            // MARK: - GET GEM BUTTON ------------------------------------------
            gemButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Play sound
                    try {
                        Configs.playSound("click.mp3", false, ctx);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    final ParseUser currUser = ParseUser.getCurrentUser();
                    Configs.showPD("Getting " + gObj.getString(Configs.GEMS_GEM_NAME).toUpperCase(), GetGem.this);

                    // Update User's points
                    currUser.increment(Configs.USER_GEMS_COLLECTED, 1);
                    int points = gObj.getInt(Configs.GEMS_GEM_POINTS);
                    currUser.increment(Configs.USER_POINTS, points);
                    currUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                // Play a sound
                                try {
                                    Configs.playSound("get_gem.mp3", false, ctx);
                                } catch (IOException err) {
                                    err.printStackTrace();
                                }

                                gemButt.setVisibility(View.INVISIBLE);

                                // Save the caught Gem
                                ParseObject gcObj = new ParseObject(Configs.COLLECTED_CLASS_NAME);
                                gcObj.put(Configs.COLLECTED_USER_POINTER, currUser);
                                gcObj.put(Configs.COLLECTED_GEM_NAME, gObj.getString(Configs.GEMS_GEM_NAME));
                                gcObj.put(Configs.COLLECTED_GEM_LOCATION, gObj.getParseGeoPoint(Configs.GEMS_GEM_LOCATION));
                                gcObj.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {

                                            // Delete this Gem from the Gems class in your Parse Dashboard
                                            gObj.deleteInBackground(new DeleteCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if (e == null) {
                                                        Configs.newGems--;
                                                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GetGem.this);
                                                        prefs.edit().putInt("newGems", Configs.newGems).apply();
                                                        Log.i("log-", "GEMS REMAINING: " + Configs.newGems);

                                                        Configs.hidePD();


                                                        AlertDialog.Builder alert = new AlertDialog.Builder(GetGem.this);
                                                        alert.setMessage("Great, you've got " + gObj.getString(Configs.GEMS_GEM_NAME).toUpperCase() + "!\nGo Back and search for other gems in your area.")
                                                                .setTitle(R.string.app_name)
                                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        dialog.dismiss();
                                                                    }
                                                                })
                                                                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                                    @Override
                                                                    public void onDismiss(DialogInterface dialog) {
                                                                        Intent homeIntent = new Intent(GetGem.this, Home.class);
                                                                        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                        startActivity(homeIntent);
                                                                    }
                                                                })
                                                                .setIcon(R.drawable.logo);
                                                        alert.create().show();

                                                        // error on deletion
                                                    } else {
                                                        Configs.simpleAlert(e.getMessage(), GetGem.this);
                                                        Configs.hidePD();
                                                    }
                                                }
                                            });

                                        }
                                    }
                                });

                                // error on updating User
                            } else {
                                Configs.hidePD();
                                Configs.simpleAlert(e.getMessage(), GetGem.this);
                            }
                        }
                    });


                }
            });// end button

        } catch (ParseException e) {
            e.printStackTrace();
        }


        // MARK: - BACK BUTTON ------------------------------------
        Button backButt = findViewById(R.id.ggBackButt);
        backButt.setTypeface(Configs.gayatri);
        backButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Play sound
                try {
                    Configs.playSound("click.mp3", false, ctx);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                finish();
            }
        });


    }// end onCreate()


    // MARK: - SHOW GEM WHILE ROTATING THE DEVICE --------------------------------------------------------
    @Override
    public void onSensorChanged(SensorEvent event) {

        // Get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);
//        Log.i("log-", "DEGREES: " + Float.toString(degree));

        if (degree < 180 && degree > 100) {
            gemButt.setVisibility(View.VISIBLE);
        } else {
            gemButt.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }


    @Override
    protected void onResume() {
        super.onResume();
        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }





}// @end
