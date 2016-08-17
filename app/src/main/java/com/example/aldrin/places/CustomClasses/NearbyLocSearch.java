package com.example.aldrin.places.CustomClasses;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.aldrin.places.NearbyJsonClasses.GetFromJson;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by aldrin on 10/8/16.
 */
public class NearbyLocSearch extends AsyncTask<Void, Void, String> {

    private HashMap<String, String> mData = new HashMap<String, String>();
    private Context mContext;
    private static final String KEY_JSON = "key";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LNG = "lng";
    public static Boolean bgProcessStarted = false;
    public static Boolean locationDetailsAvailable = false;
    public static Handler.Callback callbackBackgroundThreadCompleted;

    /**
     * Get data values for API call
     * @param context
     * @param data
     */
    public NearbyLocSearch(Context context, HashMap<String, String> data) {
        mData = data;
        mContext = context;
    }

    /**
     * Letting know other activities that background process have started.
     */
    protected void onPreExecute() {
        bgProcessStarted = true;
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
            StringBuilder sb = new StringBuilder();
            sb.append("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=");
            sb.append(mData.get("lat"));
            sb.append(',');
            sb.append(mData.get("lng"));
            sb.append("&radius=");
            sb.append(mData.get("radius"));
            sb.append("&type=");
            sb.append(mData.get("type"));
            sb.append("&key=");
            sb.append(mData.get("key"));
            URL url = new URL(sb.toString());
            Log.i("url",url.toString());
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
                Log.e("error", e.getMessage(), e);
                return null;
            }
            finally{
                urlConnection.disconnect();
            }
        } catch (MalformedURLException e) {
            Log.e("error", e.getMessage(), e);
            return null;
        } catch(IOException e) {
            Log.e("error", e.getMessage(), e);
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
            response = "error";
            Log.i("INFO", response);
            return;
        }
        Log.i("INFO", response);
        LatLng position = new LatLng(Double.parseDouble(mData.get("lat")), Double.parseDouble(mData.get("lng")));
        Gson gson = new Gson();
        GetFromJson json = gson.fromJson(response, GetFromJson.class);
        try {
            InternalStorage.writeObject(mContext, KEY_JSON, json);
            InternalStorage.writeObject(mContext, KEY_LAT, mData.get("lat"));
            InternalStorage.writeObject(mContext, KEY_LNG, mData.get("lng"));
        } catch (IOException e) {
            Log.e("error", e.getMessage());
        }
        bgProcessStarted = false;
        locationDetailsAvailable = true;
        Handler handler = new Handler(callbackBackgroundThreadCompleted);
        Message message = new Message();
        handler.sendMessage(message);
    }
}