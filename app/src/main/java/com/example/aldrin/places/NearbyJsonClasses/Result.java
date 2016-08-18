package com.example.aldrin.places.NearbyJsonClasses;

import java.io.Serializable;

/**
 * Created by aldrin on 10/8/16.
 */

public class Result implements Serializable {
    Geometry geometry;
    String name;
    String place_id;

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
