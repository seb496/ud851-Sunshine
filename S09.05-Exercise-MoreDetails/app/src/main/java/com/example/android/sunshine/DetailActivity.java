/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.utilities.SunshineDateUtils;
import com.example.android.sunshine.utilities.SunshineWeatherUtils;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = DetailActivity.class.getSimpleName();

    /*
     * In this Activity, you can share the selected day's forecast. No social sharing is complete
     * without using a hashtag. #BeTogetherNotTheSame
     */
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    /*
     * The columns of data that we are interested in displaying within our MainActivity's list of
     * weather data.
     */
    public static final String[] DETAIL_FORECAST_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
    };

    /*
     * We store the indices of the values in the array of Strings above to more quickly be able to
     * access the data from our query. If the order of the Strings above changes, these indices
     * must be adjusted to match the order of the Strings.
     */
    public static final int INDEX_WEATHER_DATE = 0;
    public static final int INDEX_WEATHER_CONDITION_ID = 1;
    public static final int INDEX_WEATHER_MAX_TEMP = 2;
    public static final int INDEX_WEATHER_MIN_TEMP = 3;
    public static final int INDEX_WEATHER_HUMIDITY = 4;
    public static final int INDEX_WEATHER_PRESSURE = 5;
    public static final int INDEX_WEATHER_WIND_SPEED = 6;
    private static final int INDEX_WEATHER_WIND_DIRECTION = 7;

    private static final int ID_DETAIL_LOADER = 2;

    /* A summary of the forecast that can be shared by clicking the share button in the ActionBar */
    private String mForecastSummary;

    private Uri mUri;

    private TextView mWeatherDate;
    private TextView mWeatherDescription;
    private TextView mWeatherHighTemp;
    private TextView mWeatherLowTemp;
    private TextView mWeatherHumidity;
    private TextView mWeatherWind;
    private TextView mWeatherPressure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mWeatherDate = (TextView) findViewById(R.id.tv_detail_date);
        mWeatherDescription = (TextView) findViewById(R.id.tv_detail_description);
        mWeatherHighTemp = (TextView) findViewById(R.id.tv_detail_high);
        mWeatherLowTemp = (TextView) findViewById(R.id.tv_detail_low);
        mWeatherHumidity = (TextView) findViewById(R.id.tv_detail_humidity);
        mWeatherWind = (TextView) findViewById(R.id.tv_detail_wind);
        mWeatherPressure = (TextView) findViewById(R.id.tv_detail_pressure);

        Intent intentThatStartedThisActivity = getIntent();
        if (intentThatStartedThisActivity != null) {
            mUri = intentThatStartedThisActivity.getData();
        }
        if (mUri == null) {
            throw new NullPointerException("null intent or null uri");
        }

        getSupportLoaderManager().initLoader(ID_DETAIL_LOADER, null, this);
    }

    /**
     * This is where we inflate and set up the menu for this Activity.
     *
     * @param menu The options menu in which you place your items.
     *
     * @return You must return true for the menu to be displayed;
     *         if you return false it will not be shown.
     *
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.detail, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    /**
     * Callback invoked when a menu item was selected from this Activity's menu. Android will
     * automatically handle clicks on the "up" button for us so long as we have specified
     * DetailActivity's parent Activity in the AndroidManifest.
     *
     * @param item The menu item that was selected by the user
     *
     * @return true if you handle the menu click here, false otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* Get the ID of the clicked item */
        int id = item.getItemId();

        /* Settings menu item clicked */
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        /* Share menu item clicked */
        if (id == R.id.action_share) {
            Intent shareIntent = createShareForecastIntent();
            startActivity(shareIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Uses the ShareCompat Intent builder to create our Forecast intent for sharing.  All we need
     * to do is set the type, text and the NEW_DOCUMENT flag so it treats our share as a new task.
     * See: http://developer.android.com/guide/components/tasks-and-back-stack.html for more info.
     *
     * @return the Intent to use to share our weather forecast
     */
    private Intent createShareForecastIntent() {
        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(mForecastSummary + FORECAST_SHARE_HASHTAG)
                .getIntent();
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        return shareIntent;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        switch (i) {
            case ID_DETAIL_LOADER:
                Uri forecastQueryUri = WeatherContract.WeatherEntry.CONTENT_URI;

                return new CursorLoader(this,
                        mUri,
                        DETAIL_FORECAST_PROJECTION,
                        null,
                        null,
                        null);

            default:
                throw new IllegalArgumentException("Unknown loader id " + i);

        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || !cursor.moveToFirst() ) {
            Log.e(TAG, "No data?? the uri was: " + mUri.toString());
            return;
        }
        for (int i=0; i<5; ++i) {
            Log.e(TAG, "get string for " + i + " gives " + cursor.getString(i));
        }
        mWeatherDate.setText(SunshineDateUtils.getFriendlyDateString(this, cursor.getLong(INDEX_WEATHER_DATE), true));
        mWeatherDescription.setText(SunshineWeatherUtils.getStringForWeatherCondition(this, cursor.getInt(INDEX_WEATHER_CONDITION_ID)));
        mWeatherHighTemp.setText(SunshineWeatherUtils.formatTemperature(this, cursor.getDouble(INDEX_WEATHER_MAX_TEMP)));
        mWeatherLowTemp.setText(SunshineWeatherUtils.formatTemperature(this, cursor.getDouble(INDEX_WEATHER_MIN_TEMP)));
        mWeatherHumidity.setText(getString(R.string.format_humidity, cursor.getFloat(INDEX_WEATHER_HUMIDITY)));
        mWeatherWind.setText(SunshineWeatherUtils.getFormattedWind(this, cursor.getFloat(INDEX_WEATHER_WIND_SPEED), cursor.getFloat(INDEX_WEATHER_WIND_DIRECTION)));
        mWeatherPressure.setText(getString(R.string.format_pressure, cursor.getFloat(INDEX_WEATHER_PRESSURE)));

        mForecastSummary = mWeatherDate.getText() + "\n" +
                mWeatherDescription.getText()  + "\n" +
                mWeatherHighTemp.getText()  + " - " + mWeatherLowTemp.getText() + "\n" +
                mWeatherHumidity.getText()  + "\n" +
                mWeatherWind.getText()  + "\n" +
                mWeatherPressure.getText() + "\n";
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}