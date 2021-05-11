package com.domain.gems;

/*-----------------------------------

    - Gems -

    created by cubycode ©2017
    All Rights reserved

-----------------------------------*/

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class OtherUserIdentity extends AppCompatActivity {


    /* Variables */
    ParseUser userObj;
    List<ParseObject> gemsArray;
    Context ctx = this;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.other_user_identity);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();



        // Get objectID from previous .java
        Bundle extras = getIntent().getExtras();
        String objectID = extras.getString("userID");
        userObj = (ParseUser) ParseObject.createWithoutData(Configs.USER_CLASS_NAME, objectID);
        try { userObj.fetchIfNeeded().getParseObject(Configs.USER_CLASS_NAME);


            // Get username
            TextView usernTxt = findViewById(R.id.ouiUsernameTxt);
           // usernTxt.setTypeface(Configs.gayatri);
            usernTxt.setText(userObj.getString(Configs.USER_USERNAME));

            // Get stats
            int userPoints = userObj.getInt(Configs.USER_POINTS);
            int gemsCollected = userObj.getInt(Configs.USER_GEMS_COLLECTED);
            TextView statsTxt = findViewById(R.id.ouiStatsTxt);
          //  statsTxt.setTypeface(Configs.gayatri);
            statsTxt.setText("GEMS COLLECTED: " + String.valueOf(gemsCollected) + "\nPOINTS: " + String.valueOf(userPoints));

            // Get Avatar
            final ImageView avImage = findViewById(R.id.ouiAvatarImg);
            ParseFile fileObject = userObj.getParseFile(Configs.USER_AVATAR);
            if (fileObject != null ) {
                fileObject.getDataInBackground(new GetDataCallback() {
                    public void done(byte[] data, ParseException error) {
                        if (error == null) {
                            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                            if (bmp != null) {
                                avImage.setImageBitmap(bmp);
            }}}});}



            // Call query
            queryCollectedGems();


        } catch (ParseException e) { e.printStackTrace(); }



        // MARK: - BACK BUTTON ------------------------------------
        Button backButt = findViewById(R.id.ouiBackButt);
      //  backButt.setTypeface(Configs.gayatri);
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





    // MARK: - QUERY COLLECTED GEMS ------------------------------------------------------
    void queryCollectedGems() {
        Configs.showPD("LOADING...", OtherUserIdentity.this);

        ParseQuery query = new ParseQuery(Configs.COLLECTED_CLASS_NAME);
        query.whereEqualTo(Configs.COLLECTED_USER_POINTER, userObj);
        query.setLimit(100000);
        query.orderByAscending(Configs.COLLECTED_CREATED_AT);
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
                                cell = inflater.inflate(R.layout.cell_collected, null);
                            }

                            // Get Parse object
                            ParseObject collObj = gemsArray.get(position);

                            // Get Gem Name
                            TextView nameTxt = cell.findViewById(R.id.ccGemNameTxt);
                         //   nameTxt.setTypeface(Configs.gayatri);
                            nameTxt.setText(collObj.getString(Configs.COLLECTED_GEM_NAME));


                            // Get city/state
                            ParseGeoPoint gp = collObj.getParseGeoPoint(Configs.COLLECTED_GEM_LOCATION);
                            try {
                                Geocoder geocoder = new Geocoder(OtherUserIdentity.this, Locale.getDefault());
                                double lat = gp.getLatitude();
                                double lon = gp.getLongitude();
                                List<Address> addresses = null;
                                addresses = geocoder.getFromLocation(lat, lon, 1);
                                if (Geocoder.isPresent()) {
                                    Address returnAddress = addresses.get(0);
                                    String address = returnAddress.getAddressLine(0);
                                    String city = returnAddress.getLocality();
                                    String country = returnAddress.getCountryName();
                                    String zipCode = returnAddress.getPostalCode();

                                    // Show Address
                                    TextView locTxt =  cell.findViewById(R.id.ccGemLocationTxt);
                                  //  locTxt.setTypeface(Configs.gayatri);
                                    locTxt.setText("Collected in " + city + ", " + country);

                                } else {
                                    Toast.makeText(getApplicationContext(), "Geocoder not present!", Toast.LENGTH_SHORT).show();
                                }

                            } catch (IOException e) {
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }



                            // Get Gem Image
                            String gemName = collObj.getString(Configs.COLLECTED_GEM_NAME);
                            final int id = getResources().getIdentifier(gemName.toLowerCase(), "drawable", getPackageName());
                            ImageView gemImg =  cell.findViewById(R.id.ccGemImg);
                            gemImg.setImageResource(id);


                            return cell;
                        }

                        @Override public int getCount() { return gemsArray.size(); }
                        @Override public Object getItem(int position) { return gemsArray.get(position); }
                        @Override public long getItemId(int position) { return position; }
                    }

                    // Init ListView and set its Adapter
                    ListView aList = (ListView) findViewById(R.id.ouiGemsListView);
                    aList.setAdapter(new ListAdapter(OtherUserIdentity.this, gemsArray));

                // Error in query
                } else {
                    Configs.simpleAlert(error.getMessage(), OtherUserIdentity.this);
                    Configs.hidePD();
        }}});

    }




}// @end
