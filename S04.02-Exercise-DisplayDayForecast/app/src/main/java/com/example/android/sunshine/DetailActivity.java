package com.example.android.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private TextView mDetailedTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mDetailedTextView = (TextView)findViewById(R.id.tv_detailed_weather);
        Intent startingIntent = getIntent();

        if (startingIntent != null && startingIntent.hasExtra(Intent.EXTRA_TEXT)) {
            mDetailedTextView.setText(startingIntent.getStringExtra(Intent.EXTRA_TEXT));
        }
    }
}