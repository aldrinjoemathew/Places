package com.example.aldrin.places.adapters;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.aldrin.places.R;
import com.example.aldrin.places.models.nearby.Result;
import com.example.aldrin.places.ui.activities.PlacesDetailsActivity;
import com.example.aldrin.places.ui.activities.UserhomeActivity;
import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aldrin on 23/8/16.
 * Used to inflate card view for each nearby place.
 */

public class CustomCardArrayAdapter extends ArrayAdapter<Result> {
    private static final String TAG_ERROR = "error";
    private static final int NAVIGATE_UP_FROM_CHILD = 2;
    private List<Result> cardVenueList = new ArrayList<Result>();
    private LatLng mPosition;
    public CustomCardArrayAdapter(Context context, int textViewResourceId, LatLng userLocation) {
        super(context, textViewResourceId);
        this.mPosition = userLocation;
    }

    @Override
    public void add(Result object) {
        cardVenueList.add(object);
        super.add(object);
    }

    @Override
    public int getCount() {
        return cardVenueList.size();
    }

    @Override
    public Result getItem(int position) {
        return cardVenueList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        CardViewHolder viewHolder;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.layout_card_location_details, parent, false);
            viewHolder = new CardViewHolder();
            viewHolder.tvTitle = (TextView) row.findViewById(R.id.tv_rest_name);
            viewHolder.tvAddress = (TextView) row.findViewById(R.id.tv_address);
            viewHolder.tvDistance = (TextView) row.findViewById(R.id.tv_distance);
            viewHolder.ratingRestaurant = (RatingBar) row.findViewById(R.id.ratingbar_venue);
            viewHolder.ctvOpen = (CheckedTextView) row.findViewById(R.id.checked_tv_open_now);
            row.setTag(viewHolder);
        } else {
            viewHolder = (CardViewHolder)row.getTag();
        }
        final Result venue = getItem(position);
        viewHolder.tvTitle.setText(venue.getName());
        viewHolder.tvAddress.setText(venue.getVicinity());
        viewHolder.tvDistance.setText(distanceFromCurrentPosition(venue) + "km");
        viewHolder.ratingRestaurant.setRating(venue.getRating());
        try {
            Boolean isOpenNow = venue.getOpening_hours().getOpen_now();
            viewHolder.ctvOpen.setChecked(isOpenNow);
        } catch (NullPointerException e) {
            Log.i(TAG_ERROR, "Open now information not available");
        }
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String placeId = venue.getPlace_id();
                Intent placesDetailsIntent = new Intent(getContext(), PlacesDetailsActivity.class);
                placesDetailsIntent.putExtra("place_id", placeId);
                ((UserhomeActivity)getContext()).startActivityForResult(placesDetailsIntent, NAVIGATE_UP_FROM_CHILD);
            }
        });
        return row;
    }

    /**
     * Data class for the adapter.
     */
    private class CardViewHolder {
        ImageView imageVenue;
        TextView tvTitle;
        TextView tvAddress;
        TextView tvDistance;
        RatingBar ratingRestaurant;
        CheckedTextView ctvOpen;
    }

    /**
     * Method used to find the distance of the venue from current position.
     * @param venue
     * @return
     */
    private String distanceFromCurrentPosition(Result venue) {
        Location userLocation = new Location("user_location");
        Location venueLocation = new Location("venue_location");
        userLocation.setLatitude(mPosition.latitude);
        userLocation.setLongitude(mPosition.longitude);
        venueLocation.setLatitude(venue.getGeometry().getLocation().getLat());
        venueLocation.setLongitude(venue.getGeometry().getLocation().getLng());
        DecimalFormat df = new DecimalFormat("#.####");
        Float distance = userLocation.distanceTo(venueLocation); //distance in meter
        return String.valueOf(Double.parseDouble(df.format(distance/1000))); //distance in km
    }
}
