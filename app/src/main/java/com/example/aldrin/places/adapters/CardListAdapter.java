package com.example.aldrin.places.adapters;

import android.content.Context;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.aldrin.places.R;
import com.example.aldrin.places.helpers.UserManager;
import com.example.aldrin.places.models.nearby.Result;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by aldrin on 13/9/16.
 */

public class CardListAdapter extends RecyclerView.Adapter<CardListAdapter.CardViewHolder> {

    private static final String TAG_ERROR = "error";
    private List<Result> mVenues = new ArrayList<Result>();
    private UserManager mUserManager;
    private Context mContext;
    private Boolean mIsGrid;

    public CardListAdapter(Context context, List<Result> cardVenueList, Boolean isGrid) {
        this.mVenues = cardVenueList;
        mUserManager = new UserManager(context);
        mContext = context;
        mIsGrid = isGrid;
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View cardView;
        if (mIsGrid) {
            cardView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_grid_location_details, parent, false);
        } else {
            cardView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_card_location_details, parent, false);
        }
        return new CardViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(CardViewHolder holder, int position) {
        holder.tvRestaurantName.setText(mVenues.get(position).getName());
        holder.tvAddress.setText(mVenues.get(position).getVicinity());
        holder.tvDistance.setText(distanceFromCurrentPosition(mVenues.get(position)) + " km");
        holder.rbVenue.setRating(mVenues.get(position).getRating());
        try {
            Boolean isOpenNow = mVenues.get(position).getOpening_hours().getOpen_now();
            holder.ctvOpenNow.setChecked(isOpenNow);
        } catch (NullPointerException e) {
            Log.i(TAG_ERROR, "Open now information not available");
        }
        Picasso.with(mContext)
                .load(mVenues.get(position).getIcon().toString())
                .into(holder.ivVenueIcon);
    }

    @Override
    public int getItemCount() {
        return mVenues.size();
    }

    public class CardViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_venue_icon)
        ImageView ivVenueIcon;
        @BindView(R.id.tv_rest_name)
        TextView tvRestaurantName;
        @BindView(R.id.tv_address)
        TextView tvAddress;
        @BindView(R.id.tv_distance)
        TextView tvDistance;
        @BindView(R.id.checked_tv_open_now)
        CheckedTextView ctvOpenNow;
        @BindView(R.id.ratingbar_venue)
        RatingBar rbVenue;

        public CardViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
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
