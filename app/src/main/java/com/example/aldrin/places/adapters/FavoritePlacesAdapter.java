package com.example.aldrin.places.adapters;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.aldrin.places.R;
import com.example.aldrin.places.helpers.UserManager;
import com.example.aldrin.places.models.placesdetails.Result;

import java.text.DecimalFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by aldrin on 31/8/16.
 * Adapter used to add data to the FavoritePlaces recycler view.
 */

public class FavoritePlacesAdapter extends RecyclerView.Adapter<FavoritePlacesAdapter.FavoriteViewHolder> {

    private List<Result> mVenues;
    private UserManager mUserManager;
    private int mCardHighlightColor = Color.rgb(130, 201, 191);
    /**
     * Constructor to initialize mVenues list.
     * @param context
     * @param venues
     */
    public FavoritePlacesAdapter(Context context, List<Result> venues) {
        this.mVenues = venues;
        mUserManager = new UserManager(context);
    }

    @Override
    public FavoriteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_card_location_details, parent, false);
        /*itemView.getBackground().setColorFilter(Color.alpha(R.color.cardHighlightColor), PorterDuff.Mode.MULTIPLY );*/
        return new FavoriteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(FavoriteViewHolder holder, int position) {
        holder.tvRestaurantName.setText(mVenues.get(position).getName());
        holder.tvAddress.setText(mVenues.get(position).getFormatted_address());
        if (mVenues.get(position).getFormatted_address() == null) {
            holder.tvAddress.setText(mVenues.get(position).getVicinity());
        }
        holder.tvDistance.setText(distanceFromCurrentPosition(mVenues.get(position)));
        Boolean isSelected = mVenues.get(position).getSelected();
        if (isSelected) {
            holder.cvPlaces.setCardBackgroundColor(mCardHighlightColor);
        } else {
            holder.cvPlaces.setCardBackgroundColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        return mVenues.size();
    }


    /**
     *  Custom holder class for inflating the view.
     */
    public class FavoriteViewHolder extends RecyclerView.ViewHolder{

        View mItemView;
        public Boolean isSelected = false;

        @BindView(R.id.places_card_view)
        CardView cvPlaces;
        @BindView(R.id.iv_venue_icon)
        ImageView ivVenueIcon;
        @BindView(R.id.tv_address)
        TextView tvAddress;
        @BindView(R.id.tv_distance)
        TextView tvDistance;
        @BindView(R.id.tv_rest_name)
        TextView tvRestaurantName;
        @BindView(R.id.checked_tv_open_now)
        CheckedTextView ctvOpenNow;
        @BindView(R.id.ratingbar_venue)
        RatingBar ratingVenue;

        public FavoriteViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
            ButterKnife.bind(this, itemView);
            itemView.setClickable(true);
            itemView.setLongClickable(true);
        }
    }


    public void clearData() {
        mVenues.clear();
    }

    /**
     * Method used find the distance of a venue from current position.
     * @param result
     * @return
     */
    private String distanceFromCurrentPosition(Result result) {
        Location userLocation = new Location("user_location");
        Location venueLocation = new Location("venue_location");
        String loc[] = mUserManager.getLocation();
        userLocation.setLatitude(Double.parseDouble(loc[0]));
        userLocation.setLongitude(Double.parseDouble(loc[1]));
        venueLocation.setLatitude(result.getGeometry().getLocation().getLat());
        venueLocation.setLongitude(result.getGeometry().getLocation().getLng());
        DecimalFormat df = new DecimalFormat("#.####");
        Float distance = userLocation.distanceTo(venueLocation); //distance in meter
        return String.valueOf(Double.parseDouble(df.format(distance/1000))); //distance in km
    }
}
