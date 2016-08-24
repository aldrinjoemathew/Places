package com.example.aldrin.places.CustomClasses;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.example.aldrin.places.AccountManagement.UserManager;
import com.example.aldrin.places.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by aldrin on 10/8/16.
 * This is an AsyncTask used to get nearby services.
 */
public class NearbyServiceSearch extends AsyncTask<Void, Void, String> {

    private HashMap<String, String> mData = new HashMap<String, String>();
    private Context mContext;
    private static final String TAG_ERROR = "error";
    private static final String TAG_INFO = "info";
    private UserManager mUserManager;

    /**
     * Get data values for API call
     * @param context
     * @param data
     */
    public NearbyServiceSearch(Context context, HashMap<String, String> data) {
        mData = data;
        mContext = context;
        mUserManager = new UserManager(mContext);
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
                return stringBuilder.toString();
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
            return;
        }
        mUserManager.updateApiResponse(response);
        broadcastLocationUpdate();
    }

    /**
     * Sends a broadcast on location update.
     */
    void broadcastLocationUpdate() {
        Intent locationUpdate = new Intent("location_update");
        mContext.sendBroadcast(locationUpdate);
    }

    /**
     * To generate and return the required API request.
     * @return url
     */
    private String buildUrl() {
        String url = String.format(mContext.getString(R.string.nearby_search_url),
                mData.get("lat"), mData.get("lng"), mData.get("radius"), mData.get("type"));
        Log.i(TAG_INFO,url);
        return url;
    }

}