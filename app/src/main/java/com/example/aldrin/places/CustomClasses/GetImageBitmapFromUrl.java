package com.example.aldrin.places.CustomClasses;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by aldrin on 25/8/16.
 */

public class GetImageBitmapFromUrl extends AsyncTask<String, Void, Bitmap>{

    private static final String TAG_ERROR = "error";
    public ImageResponse mImageResponse;

    public GetImageBitmapFromUrl() {

    }
    @Override
    protected Bitmap doInBackground(String... urls) {
        try {
            URL url = new URL(urls[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (bitmap == null) {
            Log.e(TAG_ERROR, "Bitmap returned null value");
            return;
        }
        mImageResponse.loadImage(bitmap);
    }

    /**
     * Interface is used to pass the data from background thread to main thread.
     */
    public interface ImageResponse {
        void loadImage(Bitmap output);
    }
}
