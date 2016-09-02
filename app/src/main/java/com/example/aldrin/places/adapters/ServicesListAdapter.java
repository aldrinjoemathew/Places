package com.example.aldrin.places.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.aldrin.places.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by aldrin on 2/9/16.
 */

public class ServicesListAdapter extends RecyclerView.Adapter<ServicesListAdapter.ServicesViewHolder> {

    private List<String> mServices;

    public ServicesListAdapter(List<String> mServices) {
        this.mServices = mServices;
    }

    @Override
    public ServicesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_services, parent, false);
        return new ServicesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ServicesViewHolder holder, int position) {
        holder.tvService.setText(mServices.get(position));
    }

    @Override
    public int getItemCount() {
        return mServices.size();
    }

    public class ServicesViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_services)
        TextView tvService;

        public ServicesViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
