package com.example.aldrin.places.NearbyJsonClasses;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aldrin on 10/8/16.
 */

public class GetFromJson implements Serializable {
    public List<Result> results = new ArrayList<Result>();

    public List<Result> getResults() {
        return results;
    }
}
