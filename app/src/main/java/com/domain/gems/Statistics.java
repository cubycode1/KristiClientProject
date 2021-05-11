package com.domain.gems;

import android.content.Intent;
import android.content.pm.ActivityInfo;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Statistics extends AppCompatActivity {

    /* Views */
    TextView titleTxt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Init TabBar buttons
        Button tab_two = findViewById(R.id.tab_two);
        Button tab_one = findViewById(R.id.tab_one);
        Button tab_three = findViewById(R.id.tab_three);
        Button tab_four = findViewById(R.id.tab_four);

        tab_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Statistics.this, Leaderboards.class));
            }});

        tab_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Statistics.this, Home.class));
            }});

        tab_three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Statistics.this, Identity.class));
            }});
//        tab_four.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(Statistics.this, Statistics.class));
//            }});


    }
}