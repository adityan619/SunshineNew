package com.adityan.apps.sunshinenew;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by aditya on 11/30/15.
 */
public class ForecastFragment extends Fragment implements ResultsListener{


    private ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        // This is where the fragment is created and called before onCreateView
        super.onCreate(savedInstanceState);
        // This let's the Fragment know there is a menu and indicates there're callbacks from below 2 methods.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public void onStart() {
        // Here, onStart of this Fragment, you can refresh Weather
        super.onStart();
        updateWeather();
    }

    public void updateWeather(){
        FetchWeatherTask weatherTask = new FetchWeatherTask();
        weatherTask.setOnResultsListener(this);
        weatherTask.setTaskMContext(this.getContext());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String locationQuery = "Moscow,ru";
        locationQuery = prefs.getString(getString(R.string.pref_location_key),getString(R.string.pref_location_default));
        weatherTask.execute(locationQuery);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int selectedId = item.getItemId();
        if (selectedId==R.id.action_refresh){
            updateWeather();
            return true;
        }
        if (selectedId==R.id.action_settings){
            Intent intent = new Intent(getActivity(),SettingsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // invoking this function is where UI gets initialized.
        String[] weatherData = {
                "Mon 6/23 - Sunny - 31/17",
                "Tue 6/24 - Foggy - 21/8",
                "Wed 6/25 - Cloudy - 22/17",
                "Thurs 6/26 - Rainy - 18/11",
                "Fri 6/27 - Foggy - 21/10",
                "Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",
                "Sun 6/29 - Sunny - 20/7"
        };
        List weekForecast = new ArrayList<String>(Arrays.asList(weatherData));
        mForecastAdapter = new ArrayAdapter<String>(
                getContext(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weekForecast
        );

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ListView mlistView = (ListView) rootView.findViewById(R.id.listview_forecast);
        // Attach our Adapter to the ListView so it can automatically create ListView.
        mlistView.setAdapter(mForecastAdapter);
        mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String forecastForPosition = mForecastAdapter.getItem(position);
//                Toast.makeText(getActivity(),forecastForPosition,Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(),DetailActivity.class).putExtra(Intent.EXTRA_TEXT,forecastForPosition);
                startActivity(intent);
            }
        });
        return rootView;
    }

    @Override
    public void onResultsSucceeded(String[] results) {
        mForecastAdapter.clear();
        mForecastAdapter.addAll(results);
    }
}
