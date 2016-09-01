package com.example.aldrin.places.models.placesdetails;

import java.util.List;

/**
 * Created by aldrin on 18/8/16.
 */

public class Result {
    private String name;
    private String formatted_address;
    private String formatted_phone_number;
    private Geometry geometry;
    private String international_phone_number;
    private OpeningHours opening_hours;
    private List<Photo> photos;
    private String website;
    private String url;
    private float rating;
    private List<Review> reviews;
    private String place_id;

    public String getPlace_id() {
        return place_id;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public float getRating() {
        return rating;
    }

    public String getUrl() {
        return url;
    }

    public String getWebsite() {
        return website;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public String getName() {
        return name;
    }

    public String getFormatted_address() {
        return formatted_address;
    }

    public String getFormatted_phone_number() {
        return formatted_phone_number;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public String getInternational_phone_number() {
        return international_phone_number;
    }

    public OpeningHours getOpening_hours() {
        return opening_hours;
    }
}