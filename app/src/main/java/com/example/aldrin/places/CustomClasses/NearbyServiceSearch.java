package com.example.aldrin.places.CustomClasses;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.aldrin.places.NearbyJsonClasses.GetFromJson;
import com.example.aldrin.places.NearbyJsonClasses.Result;
import com.example.aldrin.places.R;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

/**
 * Created by aldrin on 10/8/16.
 * This is an AsyncTask used to get nearby services.
 */
public class NearbyServiceSearch extends AsyncTask<Void, Void, String> {

    private HashMap<String, String> mData = new HashMap<String, String>();
    private Context mContext;
    private static final String KEY_JSON = "key";
    private static final String TAG_ERROR = "error";
    private static final String TAG_INFO = "info";
    private HashMap<String, String> mApiUrlData = new HashMap<String, String>();
    private GetPlacesDetails getPlacesDetails;
    public static Boolean bgProcessExists = false;
    public static Boolean locationDetailsAvailable = false;
    public static Handler.Callback callbackBackgroundThreadCompleted;

    /**
     * Get data values for API call
     * @param context
     * @param data
     */
    public NearbyServiceSearch(Context context, HashMap<String, String> data) {
        mData = data;
        mContext = context;
    }

    /**
     * Letting know other activities that background process have started.
     */
    protected void onPreExecute() {
        bgProcessExists = true;
        locationDetailsAvailable = false;
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
        Gson gson = new Gson();
        GetFromJson json = gson.fromJson(response, GetFromJson.class);
        try {
            InternalStorage.writeObject(mContext, KEY_JSON, json);
        } catch (IOException e) {
            Log.e(TAG_ERROR, e.getMessage());
        }
        List<Result> results = json.getResults();
        if (results != null) {
            getPlacesDetails = new GetPlacesDetails(mContext, results);
            getPlacesDetails.execute();
        }
        bgProcessExists = false;
        locationDetailsAvailable = true;
        Handler handler = new Handler(callbackBackgroundThreadCompleted);
        Message message = new Message();
        handler.sendMessage(message);
    }

    private String buildUrl() {
        StringBuilder sb = new StringBuilder();
        sb.append("https://maps.googleapis.com/maps/api/place/");
        sb.append(mData.get("service"));
        sb.append("/json?location=");
        sb.append(mData.get("lat"));
        sb.append(',');
        sb.append(mData.get("lng"));
        sb.append("&radius=");
        sb.append(mData.get("radius"));
        sb.append("&type=");
        sb.append(mData.get("type"));
        sb.append("&key=");
        sb.append(mContext.getString(R.string.google_places_web_key));
        String url = sb.toString();
        Log.i(TAG_INFO,url);
        return url;
    }
}