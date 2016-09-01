package com.example.aldrin.places.models.placesdetails;

/**
 * Created by aldrin on 31/8/16.
 */

public class Review {
    private String author_name;
    private String text;
    private float rating;
    private long time;

    public String getAuthor_name() {
        return author_name;
    }

    public String getText() {
        return text;
    }

    public float getRating() {
        return rating;
    }

    public long getTime() {
        return time;
    }
}
