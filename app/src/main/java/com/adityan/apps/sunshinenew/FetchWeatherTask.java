package com.adityan.apps.sunshinenew;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by aditya on 11/30/15.
 */
public class FetchWeatherTask extends AsyncTask {

    public final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

    String forecastJSONStr = null;
    HttpURLConnection urlConnection = null;
    BufferedReader reader = null;
    @Override
    protected Object doInBackground(Object[] params) {
        try {
            final String FORECAST_URL = "http://api.openweathermap.org/data/2.5/forecast/daily/?q=Mumbai,In&mode=json&units=metrics&cnt=7&appid=5799f440f6d3770166be14e40aea8bc1";

            URL url = new URL(FORECAST_URL);
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


        } catch (IOException e) {
            Log.e(LOG_TAG,"ERROR", e);
            e.printStackTrace();
        }finally {

        }
        return null;
    }
}
