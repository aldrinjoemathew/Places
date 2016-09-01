package com.example.aldrin.places.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

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
    private ProgressBar mProgressBar;
    private String mImageUrl;

    public GetImageBitmapFromUrl(ProgressBar progressBar) {
        mProgressBar = progressBar;
    }

    @Override
    protected void onPreExecute() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
        try {
            mImageUrl = urls[0];
            URL url = new URL(mImageUrl);
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
        mProgressBar.setVisibility(View.GONE);
        mImageResponse.loadImage(mImageUrl, bitmap);
    }

    /**
     * Interface is used to pass the data from background thread to main thread.
     */
    public interface ImageResponse {
        void loadImage(String url, Bitmap output);
    }
}
