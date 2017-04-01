package com.omeryaari.parke.ui;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.omeryaari.parke.R;

public class MainActivity extends AppCompatActivity {

    private ImageButton buttonParkSearch;
    private ImageButton buttonParkMark;
    private Button buttonSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        buttonParkSearch = (ImageButton) findViewById(R.id.imagebutton_main_parking_search);
        buttonParkMark = (ImageButton) findViewById(R.id.imagebutton_main_parking_mark);

        buttonParkSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startParkSearchActivity();
            }
        });
        buttonParkMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startParkMarkActivity();
            }
        });
    }

    /**
     * Starts the parking search activity.
     */
    private void startParkSearchActivity() {
        Intent intent = new Intent(MainActivity.this, ParkSearchActivity.class);
        MainActivity.this.startActivity(intent);
    }

    /**
     * Starts the parking mark activity.
     */
    private void startParkMarkActivity() {
        Intent intent = new Intent(MainActivity.this, ParkMarkActivity.class);
        MainActivity.this.startActivity(intent);
    }
}
