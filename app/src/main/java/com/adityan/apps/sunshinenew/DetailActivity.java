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

package com.adityan.apps.sunshinenew;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ShareActionProvider;
import android.widget.TextView;

public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this,SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment {
        private static final String LOG_TAG = DetailFragment.class.getSimpleName();
        public static final String SHARE_HASHTAG = " #SunshineNewApp";
        private String mForeCastStr;

        public DetailFragment() {

            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            Intent currentIntent = getActivity().getIntent();
            // The currentIntent Envolope is sent with Extra Text(forecast)
            if (currentIntent.hasExtra(Intent.EXTRA_TEXT)) {
                mForeCastStr = currentIntent.getStringExtra(Intent.EXTRA_TEXT);
                // We get the TextView in the fragment_detail by using detail_text id
                ((TextView)rootView.findViewById(R.id.detail_text))
                        .setText(mForeCastStr);
            }
            return rootView;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
            inflater.inflate(R.menu.share_detailfragment,menu);
            // Get Share menu item
            MenuItem menuItem = menu.findItem(R.id.action_share);

            ShareActionProvider mShareActionProvider = (ShareActionProvider)menuItem.getActionProvider();

//            Now Attach an intent to this mShareActionProvider
            // Attach an intent to this ShareActionProvider.  You can update this at any time,
            // like when the user selects a new piece of data they might like to share.
            // If you have something to be auto shared, or you need to share from diff method,
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
            else {
                Log.d(LOG_TAG,"Share Action provider got null?");
            }
        }

        public Intent createShareForecastIntent(){
            Intent sendIntet = new Intent();
            sendIntet.setAction(Intent.ACTION_SEND);
            // To come back to Original task after sharing.
            sendIntet.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            sendIntet.setType("text/plain");
            sendIntet.putExtra(Intent.EXTRA_TEXT,mForeCastStr+SHARE_HASHTAG);
            return sendIntet;
        }
    }
}