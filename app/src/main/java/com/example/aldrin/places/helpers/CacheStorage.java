package com.example.aldrin.places.helpers;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * Created by aldrin on 2/9/16.
 */

public class CacheStorage {

    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    final int cacheSize = maxMemory / 8;
    private LruCache<String, Bitmap> mMemoryCache;

    public CacheStorage() {
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    /**
     * Method to add a bitmap image to cache if available.
     * @param key
     * @param bitmap
     */
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    /**
     * Method to get the bitmap of image stored in the cache storage.
     * Returns null if bitmap is not available in the cache.
     * @param key
     * @return imageBitmap
     */
    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }


}
