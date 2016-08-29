package com.example.aldrin.places.CustomClasses;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.aldrin.places.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by aldrin on 18/8/16.
 */

public class GetPlacesDetails extends AsyncTask<Void, Void, String>{

    public AsyncResponse delegate;
    private final String TAG_ERROR = "error";
    private final String TAG_INFO = "info";
    private String mPlaceId;
    private Context mContext;

    /**
     * Get data values for API call
     * @param context
     */
    public GetPlacesDetails(Context context, String placeId) {
        mContext = context;
        mPlaceId = placeId;
    }

    /**
     * Letting know other activities that background process have started.
     */
    protected void onPreExecute() {
    }

    /**
     * Making an http connection to the remote api in the background.
     * Save the mData from the api in string format and returned to onPostExecute.
     * @param urls
     * @return
     */
    protected String doInBackground(Void... urls) {
        try {
            URL url = new URL(buildUrl());
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                BufferedReader bufferedReader =
                        new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                String response = stringBuilder.toString();
                return response;
            } catch (IOException e) {
                Log.e(TAG_ERROR, e.getMessage(), e);
                return null;
            }
            finally{
                urlConnection.disconnect();
            }
        } catch (MalformedURLException e) {
            Log.e(TAG_ERROR, e.getMessage(), e);
            return null;
        } catch(IOException e) {
            Log.e(TAG_ERROR, e.getMessage(), e);
            return null;
        }
    }

    /**
     * response is a json string returned by background task.
     * Write search response into cache.
     * @param response
     */
    protected void onPostExecute(String response) {
        if(response == null) {
            Log.i(TAG_INFO, String.valueOf(R.string.error));
        }
        delegate.displayProcessDetails(response);
    }


    /**
     * Interface is used to pass the data from background thread to main thread.
     */
    public interface AsyncResponse {
        void displayProcessDetails(String output);
    }

    /**
     * Method is used to build the places details API url.
     * @return url
     */
    private String buildUrl() {
        String url = String.format(mContext.getString(R.string.places_details_url),
                mPlaceId);
        Log.i(TAG_INFO,url);
        return url;
    }
}
