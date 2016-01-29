package com.makarfi.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.makarfi.sunshine.app.data.WeatherContract;

/**
 * Created by Makarfi on 22/01/16.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    final String LOG_TAG = DetailFragment.class.getSimpleName();

    final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    private String mForecast;
    private ShareActionProvider mShareActionProvider;

    private ImageView mIconView;
    private TextView mDayView;
    private TextView mDateView;
    private TextView mDescriptionView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mPressureView;

    private static final int DETAIL_LOADER = 0;

    private static String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE
    };

    //FORECAST_COLUMNS indices
    private static final int WEATHER_ID = 0;
    private static final int COL_WEATHER_DATE = 1;
    private static final int COL_WEATHER_CONDITION_ID = 2;
    private static final int COL_WEATHER_SHORT_DESC = 3;
    private static final int COL_WEATHER_MAX_TEMP = 4;
    private static final int COL_WEATHER_MIN_TEMP = 5;
    private static final int COL_WEATHER_HUMIDITY = 6;
    private static final int COL_WEATHER_WIND_SPEED = 7;
    private static final int COL_WEATHER_WIND_DEGREES = 8;
    private static final int COL_WEATHER_PRESSURE = 9;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        mDayView = (TextView) rootView.findViewById(R.id.detail_day_textview);
        mDateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        mDescriptionView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
        mHighTempView = (TextView) rootView.findViewById(R.id.detail_high_textview);
        mLowTempView = (TextView) rootView.findViewById(R.id.detail_low_textview);
        mHumidityView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
        mWindView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
        mPressureView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);

        return rootView;
    }

    private Intent createShareForecastIntent(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_fragment, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if(mForecast != null)
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        else{
            Log.v(LOG_TAG, "ShareActionProvider is null");
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Intent intent = getActivity().getIntent();
        if(intent == null || intent.getData() == null) {
            return null;
        }

        return new CursorLoader(getActivity(),
                intent.getData(),
                FORECAST_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(!data.moveToFirst()) {
            return;
        }

        // Read weather condition ID from cursor
        int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);
        // Use Placeholder image
        mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

        // Read date from cursor and update views for day of week and date
        long date = data.getLong(COL_WEATHER_DATE);
        String friendlyDay = Utility.getFriendlyDayString(getActivity(), date);
        String dateText = Utility.getFormattedMonthDay(getActivity(), date);
        mDayView.setText(friendlyDay);
        mDateView.setText(dateText);

        // Read description from cursor and update view
        String description = data.getString(COL_WEATHER_SHORT_DESC);
        mDescriptionView.setText(description);

        // Read temperature from cursor and update view
        boolean isMetric = Utility.isMetric(getActivity());

        double highTemp = data.getDouble(COL_WEATHER_MAX_TEMP);
        double lowTemp = data.getDouble(COL_WEATHER_MIN_TEMP);
        mHighTempView.setText(Utility.formatTemperature(getActivity(), highTemp, isMetric));
        mLowTempView.setText(Utility.formatTemperature(getActivity(), lowTemp, isMetric));

        // Read humidity from cursor and update view
        float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
        mHumidityView.setText(getActivity().getString(R.string.format_humidity, humidity));

        // Read wind speed from cursor and update view
        float windSpeed = data.getFloat(COL_WEATHER_WIND_SPEED);
        float windDirection = data.getFloat(COL_WEATHER_WIND_DEGREES);
        mWindView.setText(Utility.getFormattedWind(getActivity(), windSpeed, windDirection));

        // Read pressure from cursor and update view
        float pressure = data.getFloat(COL_WEATHER_PRESSURE);
        mPressureView.setText(getActivity().getString(R.string.format_pressure, pressure));

        // We still need this for the share intent
        mForecast = String.format("%s - %s - %s/%s", dateText, description, highTemp, lowTemp);

        if(mShareActionProvider != null)
            mShareActionProvider.setShareIntent(createShareForecastIntent());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
