package com.example.aldrin.places.interfaces;

import com.example.aldrin.places.models.nearby.GetFromJson;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by aldrin on 5/9/16.
 */

public interface ApiInterface {
    @GET("place/details/json")
    Call<com.example.aldrin.places.models.placesdetails.GetFromJson> getPlaceDetails(@Query("key") String apiKey, @Query("placeid") String placeId);
    @GET("place/nearbysearch/json")
    Call<GetFromJson> getNearbyServices(@Query("key") String apiKey, @Query("type") String serviceType,
                                        @Query("name") String searchValue,
                                        @Query("location") String location, @Query("radius") String radius);
}
