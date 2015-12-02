package com.adityan.apps.sunshinenew;

/**
 * Created by aditya on 12/2/15.
 */
/**
 * Activites that wish to be notified about results
 * in onPostExecute of an AsyncTask must implement
 * this interface.
 *
 * This is the basic Observer pattern.
 */
public interface ResultsListener {
    public void onResultsSucceeded(String[] results);
}