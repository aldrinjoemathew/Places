package com.example.aldrin.places.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.aldrin.places.models.placesdetails.Review;
import com.example.aldrin.places.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by aldrin on 31/8/16.
 */

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviews;

    public ReviewAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_review, parent, false);
        return new ReviewViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        holder.tvReviewAuthor.setText(reviews.get(position).getAuthor_name());
        holder.ratingReview.setRating(reviews.get(position).getRating());
        holder.tvReview.setText(reviews.get(position).getText());
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_review_author)
        TextView tvReviewAuthor;
        @BindView(R.id.tv_review)
        TextView tvReview;
        @BindView(R.id.ratingbar_review)
        RatingBar ratingReview;

        public ReviewViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
