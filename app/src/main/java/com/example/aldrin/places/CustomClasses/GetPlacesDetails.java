package com.example.aldrin.places.CustomClasses;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.aldrin.places.NearbyJsonClasses.Result;
import com.example.aldrin.places.PlacesDetailsJsonClasses.GetFromJson;
import com.example.aldrin.places.R;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by aldrin on 18/8/16.
 */

public class GetPlacesDetails extends AsyncTask<Void, Void, String>{

    private static final String TAG_ERROR = "error";
    private static final String TAG_INFO = "info";
    private List<Result> mResults;
    private Context mContext;
    /**
     * Get data values for API call
     * @param context
     */
    public GetPlacesDetails(Context context, List<Result> results) {
        mContext = context;
        mResults = results;
    }

    /**
     * Letting know other activities that background process have started.
     */
    protected void onPreExecute() {
        Log.e(TAG_ERROR, "hkjskdfhlkdf");
    }

    /**
     * Making an http connection to the remote api in the background.
     * Save the mData from the api in string format and returned to onPostExecute.
     * @param urls
     * @return
     */
    protected String doInBackground(Void... urls) {
        if (mResults!= null) {
            for (int i=0; i< mResults.size(); i++) {
                Result venue = mResults.get(i);
                String placeId = venue.getPlace_id();
                String response = getPlaceDetails(placeId);
                if(response == null) {
                    Log.i(TAG_INFO, String.valueOf(R.string.error));
                    continue;
                }
                Gson gson = new Gson();
                GetFromJson json = gson.fromJson(response, GetFromJson.class);
            }
        }
        return null;
    }

    private String getPlaceDetails(String placeId) {
        try {
            URL url = new URL(buildUrl(placeId));
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
    }

    private String buildUrl(String placeId) {
        StringBuilder sb = new StringBuilder();
        sb.append("https://maps.googleapis.com/maps/api/place/");
        sb.append("details");
        sb.append("/json?placeid=");
        sb.append(placeId);
        sb.append("&key=");
        sb.append(mContext.getString(R.string.google_places_web_key));
        String url = sb.toString();
        Log.i(TAG_INFO,url);
        return url;
    }
}
