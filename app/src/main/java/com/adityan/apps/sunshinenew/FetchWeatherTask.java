package com.adityan.apps.sunshinenew;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

/**
 * Created by aditya on 11/30/15.
 */
public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

    public final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

    String forecastJSONStr = null;
    HttpURLConnection urlConnection = null;
    BufferedReader reader = null;
    String format = "json";
    String units = "metrics";
    int numDays = 7;

    ResultsListener listener;
    Context mContext;
    public void setOnResultsListener(ResultsListener listener) {
        Log.d("FWT","setOnResultsListener"+listener.toString());
        this.listener = listener;
    }
    public void setTaskMContext(Context context){
        this.mContext=context;
    }

    @Override
    protected String[] doInBackground(String... params) {
        try {
            final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily/";
            ///?q=Mumbai,In&mode=json&units=metrics&cnt=7&appid=5799f440f6d3770166be14e40aea8bc1

            final String QUERY_PARAM = "q";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";
            final String APPID_PARAM = "APPID";

            String appID = "5799f440f6d3770166be14e40aea8bc1";
            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, params[0])
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                    .appendQueryParameter(APPID_PARAM, appID)
                    .build();
            Log.d(LOG_TAG,"The built URI is"+builtUri);
            URL url = new URL(builtUri.toString());
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            // Read input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream == null) {
                return null;
            }
            StringBuffer buffer = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null){
                buffer.append(line+"\n");
            }
            if (buffer.length() == 0) {
                return null;
            }
            forecastJSONStr = buffer.toString();
            return getWeatherDataFromJson(forecastJSONStr,numDays);


        } catch (IOException e) {
            Log.e(LOG_TAG,"ERROR of IO in FWT", e);
            e.printStackTrace();
        } catch (JSONException e) {
            Log.e(LOG_TAG,"JSON Exception", e);
        } finally {
            if (urlConnection !=null){
                urlConnection.disconnect();
            }
            if (reader != null){
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG,"Error closing stream",e);
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String[] strings) {
        if (strings != null) {
            listener.onResultsSucceeded(strings);
        }
    }

    /* The date/time conversion code is going to be moved outside the asynctask later,
             * so for convenience we're breaking it out into its own method now.
             */
    private String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low, String unitType) {
        String IMPERIAL = mContext.getResources().getString(R.string.pref_units_imperial);
        String METRICS = mContext.getResources().getString(R.string.pref_units_metric);
        if (unitType.equals(IMPERIAL)) {
            high = (high * 1.8) + 32;
            low = (low * 1.8) + 32;
        } else if (!unitType.equals(METRICS)) {
            Log.d(LOG_TAG, "Unit type not found: " + unitType);
        }

        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DESCRIPTION = "main";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        // OWM returns daily forecasts based upon the local time of the city that is being
        // asked for, which means that we need to know the GMT offset to translate this data
        // properly.

        // Since this data is also sent in-order and the first day is always the
        // current day, we're going to take advantage of that to get a nice
        // normalized UTC date for all of our weather.

        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();

        String[] resultStrs = new String[numDays];

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String unitsType = prefs.getString(mContext.getResources().getString(R.string.pref_units_key),
                mContext.getResources().getString(R.string.pref_units_metric));
        Log.d("FWT", "Found unitsType Settings as "+ unitsType);

        for(int i = 0; i < weatherArray.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            String day;
            String description;
            String highAndLow;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            long dateTime;
            // Cheating to convert this to UTC time, which is what we want anyhow
            dateTime = dayTime.setJulianDay(julianStartDay+i);
            day = getReadableDateString(dateTime);

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            highAndLow = formatHighLows(high, low,unitsType);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }

        for (String s : resultStrs) {
            Log.v(LOG_TAG, "Forecast entry: " + s);
        }
        return resultStrs;

    }

}
