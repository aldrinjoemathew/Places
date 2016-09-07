package com.example.aldrin.places.events;

/**
 * Created by aldrin on 6/9/16.
 */

public class ApiResponseUpdatedEvent {
    public final String message;

    public ApiResponseUpdatedEvent(String message) {
        this.message = message;
    }
}