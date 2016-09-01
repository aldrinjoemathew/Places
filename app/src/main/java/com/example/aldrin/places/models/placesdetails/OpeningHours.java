package com.example.aldrin.places.models.placesdetails;

/**
 * Created by aldrin on 18/8/16.
 */

public class OpeningHours {
    private Boolean open_now;
    private String[] weekday_text;

    public String[] getWeekday_text() {
        return weekday_text;
    }

    public Boolean getOpen_now() {
        return open_now;
    }
}
