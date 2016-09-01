package com.example.aldrin.places.models.nearby;

import java.io.Serializable;

/**
 * Created by aldrin on 10/8/16.
 */

public class Location implements Serializable {
    private Double lat;
    private Double lng;

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

}
