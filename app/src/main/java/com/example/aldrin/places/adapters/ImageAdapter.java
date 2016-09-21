package com.example.aldrin.places.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.aldrin.places.R;
import com.example.aldrin.places.ui.fragments.ListFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by aldrin on 16/9/16.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private List<String> mUrls = new ArrayList<>();
    private Context mContext;

    public ImageAdapter(List<String> mUrls, Context mContext) {
        this.mUrls = mUrls;
        this.mContext = mContext;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View imageView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gallery_image_view, parent, false);
        return new ImageViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((Activity)mContext).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;
        String imageUrl = String.format(mContext.getString(R.string.image_url), mUrls.get(position));
        Picasso.with(mContext)
                .load(imageUrl)
                .resize(width/4, width/4)
                .centerCrop()
                .into(holder.ivGallery);
    }

    @Override
    public int getItemCount() {
        return mUrls.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.iv_gallery)
        ImageView ivGallery;

        public ImageViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
