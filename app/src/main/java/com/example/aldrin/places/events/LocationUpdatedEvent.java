package com.example.aldrin.places.events;

/**
 * Created by aldrin on 6/9/16.
 */

public class LocationUpdatedEvent {
    public final String message;

    public LocationUpdatedEvent(String message) {
        this.message = message;
    }
}