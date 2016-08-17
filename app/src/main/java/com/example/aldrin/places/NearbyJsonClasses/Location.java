package com.example.aldrin.places.NearbyJsonClasses;

import java.io.Serializable;

/**
 * Created by aldrin on 10/8/16.
 */

public class Location implements Serializable {
    private Double lat;

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    private Double lng;
}
