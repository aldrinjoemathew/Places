package com.example.aldrin.places.models.nearby;

import java.io.Serializable;
import java.net.URL;

/**
 * Created by aldrin on 10/8/16.
 */

public class Result implements Serializable {
    private Geometry geometry;
    private String name;
    private String place_id;
    private URL icon;
    private float rating;
    private OpeningHours opening_hours;
    private String vicinity;

    public URL getIcon() {
        return icon;
    }

    public float getRating() {
        return rating;
    }

    public OpeningHours getOpening_hours() {
        return opening_hours;
    }

    public String getVicinity() {
        return vicinity;
    }

    public String getPlace_id() {
        return place_id;
    }

    public String getName() {
        return name;
    }

    public Geometry getGeometry() {
        return geometry;
    }
}
