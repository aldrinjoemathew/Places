package com.example.aldrin.places.Adapters;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.aldrin.places.NearbyJsonClasses.Result;
import com.example.aldrin.places.R;
import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aldrin on 23/8/16.
 */

public class CustomCardArrayAdapter extends ArrayAdapter<Result> {
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
            row = inflater.inflate(R.layout.card_location_details, parent, false);
            viewHolder = new CardViewHolder();
            viewHolder.tvTitle = (TextView) row.findViewById(R.id.rest_name_text_view);
            viewHolder.tvAddress = (TextView) row.findViewById(R.id.address_text_view);
            viewHolder.tvDistance = (TextView) row.findViewById(R.id.distance_text_view);
            viewHolder.ratingRestaurant = (RatingBar) row.findViewById(R.id.venue_rating);
            viewHolder.ctvOpen = (CheckedTextView) row.findViewById(R.id.open_now);
            row.setTag(viewHolder);
        } else {
            viewHolder = (CardViewHolder)row.getTag();
        }
        Result venue = getItem(position);
        viewHolder.tvTitle.setText(venue.getName());
        viewHolder.tvAddress.setText(venue.getVicinity());
        viewHolder.tvDistance.setText(distanceFromCurrentPosition(venue) + "km");
        viewHolder.ratingRestaurant.setRating(venue.getRating());
/*        Boolean isOpenNow = venue.getOpening_hours().getOpen_now();
        viewHolder.ctvOpen.setChecked(isOpenNow);*/
        return row;
    }

    private class CardViewHolder {
        ImageView imageVenue;
        TextView tvTitle;
        TextView tvAddress;
        TextView tvDistance;
        RatingBar ratingRestaurant;
        CheckedTextView ctvOpen;
    }

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
