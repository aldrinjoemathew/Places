package com.example.aldrin.places.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.aldrin.places.R;

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by aldrin on 20/9/16.
 */

public class BottomSheetDialog extends BottomSheetDialogFragment {

    @BindView(R.id.iv_venue_icon)
    ImageView ivVenueIcon;
    @BindView(R.id.tv_rest_name)
    TextView tvRestaurantName;
    @BindView(R.id.tv_address)
    TextView tvAddress;
    @BindView(R.id.tv_distance)
    TextView tvDistance;

    static BottomSheetDialog newInstance(String string) {
        BottomSheetDialog f = new BottomSheetDialog();
        Bundle args = new Bundle();
        args.putString("string", string);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

    }
}
