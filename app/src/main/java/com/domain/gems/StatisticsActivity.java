package com.domain.gems;

/*-----------------------------------

    - Gems -

    created by cubycode ©2017
    All Rights reserved

-----------------------------------*/

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.IOException;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity  implements SwipeRefreshLayout.OnRefreshListener {

    /* Views */
    TextView titleTxt;



    /* Variables */
    List<ParseObject> userStatisticsArray;
    SwipeRefreshLayout refreshControl;
    Context ctx = this;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();



        // Init views
        TextView titleTxt = findViewById(R.id.thTitleTxt);
       // titleTxt.setTypeface(Configs.gayatri);


        // Init a refreshControl
        refreshControl = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        refreshControl.setOnRefreshListener(this);



        // Init TabBar buttons
        Button tab_one = findViewById(R.id.tab_one);
        Button tab_two = findViewById(R.id.tab_three);
        Button tab_four = findViewById(R.id.tab_four);

        tab_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StatisticsActivity.this, Home.class));
            }});

        tab_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StatisticsActivity.this, Identity.class));
            }});
        tab_four.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StatisticsActivity.this, StatisticsActivity.class));
            }});


        // Call query
        queryUserStatistics();


        // Init AdMob banner
        AdView mAdView =  findViewById(R.id.admobBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }// end onCreate()

    // MARK: - QUERY TOP HUNTERS ---------------------------------------------------------------
    void queryUserStatistics() {
        Configs.showPD("LOADING...", StatisticsActivity.this);

        ParseQuery query = new ParseQuery(Configs.USER_CLASS_NAME);
        query.whereGreaterThanOrEqualTo(Configs.USER_GEMS_COLLECTED, 1);
        query.setLimit(50);
        query.orderByDescending(Configs.USER_GEMS_COLLECTED);

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    userStatisticsArray = objects;
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
                                cell = inflater.inflate(R.layout.cell_statistics, null);
                            }

                            final ImageView anImage =  cell.findViewById(R.id.ctAvatarImg);

//                            if (position == 0){
//                                anImage.setImageResource(R.drawable.badge_one);
//                            }else if(position == 1){
//                                anImage.setImageResource(R.drawable.badge_b);
//                            }else if (position == 2){
//                                anImage.setImageResource(R.drawable.badge_two);
//                            }


                            // Get Parse object
                            ParseUser userObj = (ParseUser) userStatisticsArray.get(position);

                            // Get username
                            TextView usernTxt =  cell.findViewById(R.id.ccGemNameTxt);
                          //  usernTxt.setTypeface(Configs.gayatri);
                            usernTxt.setText(userObj.getString(Configs.USER_USERNAME));

                            // Get stats
                            int userPoints = 0;
                            userPoints = userObj.getInt(Configs.USER_POINTS);
                            int gemsCaught = 0;
                            gemsCaught = userObj.getInt(Configs.USER_GEMS_COLLECTED);

                            TextView statsTxt = cell.findViewById(R.id.tv_points);
                            //   statsTxt.setTypeface(Configs.gayatri);
                            statsTxt.setText(""+userPoints);

                            TextView gemsTxt = cell.findViewById(R.id.tv_gems);
                            //   statsTxt.setTypeface(Configs.gayatri);
                            gemsTxt.setText(""+gemsCaught);

                            TextView averageTxt = cell.findViewById(R.id.tv_average);
                            //   statsTxt.setTypeface(Configs.gayatri);
                            int average = 0;
                            average = userPoints/gemsCaught;
                            averageTxt.setText(average+"%");


                            // Get Image

                            ParseFile fileObject = userObj.getParseFile(Configs.USER_AVATAR);
                            if (fileObject != null) {
                            fileObject.getDataInBackground(new GetDataCallback() {
                                public void done(byte[] data, ParseException error) {
                                    if (error == null) {
                                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                        if (bmp != null) {
                                            anImage.setImageBitmap(bmp);
//                                            anImage.setImageResource(R.drawable.new_marker);
                            }}}});}


                            return cell;
                        }

                        @Override public int getCount() { return userStatisticsArray.size(); }
                        @Override public Object getItem(int position) { return userStatisticsArray.get(position); }
                        @Override public long getItemId(int position) { return position; }
                    }

                    // Init ListView and set its Adapter
                    ListView aList = (ListView) findViewById(R.id.thTopListView);
                    aList.setAdapter(new ListAdapter(StatisticsActivity.this, userStatisticsArray));
                    aList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                            try { Configs.playSound("click.mp3", false, ctx);
                            } catch (IOException e) { e.printStackTrace(); }

                            ParseUser uObj = (ParseUser) userStatisticsArray.get(position);
                            Intent i = new Intent(StatisticsActivity.this, OtherUserIdentity.class);
                            Bundle extras = new Bundle();
                            extras.putString("userID", uObj.getObjectId());
                            i.putExtras(extras);
                            startActivity(i);
                    }});


                // Error in query
                } else {
                    Configs.hidePD();
                    Configs.simpleAlert(error.getMessage(), StatisticsActivity.this);
        }}});

    }





    // MARK: - REFRESH TOP HUNTERS DATA ------------------------------------------------
    @Override
    public void onRefresh() {
        Log.i("log-", "REFRESHING...");

        // Call query
        queryUserStatistics();

        refreshControl.setRefreshing(false);
    }







}// @end
