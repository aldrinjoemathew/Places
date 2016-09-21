package com.example.aldrin.places.ui.fragments;


import android.os.Bundle;
import android.os.storage.StorageManager;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.aldrin.places.R;
import com.example.aldrin.places.adapters.ImageAdapter;
import com.example.aldrin.places.events.ConfigurationChnagedEvent;
import com.example.aldrin.places.helpers.InternalStorage;
import com.example.aldrin.places.helpers.RecyclerClickListener;
import com.example.aldrin.places.helpers.UserManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class GalleryGridFragment extends Fragment {

    public static final String TAG = GalleryGridFragment.class.getSimpleName();
    @BindView(R.id.recycler_grid_gallery)
    RecyclerView recyclerViewGalleryGrid;
    @BindView(R.id.layout_gallery_grid)
    CoordinatorLayout layoutGallery;

    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<String> mUrls = new ArrayList<>();
    private UserManager mUserManager;
    private Boolean mFavImagesAvailable = true;
    private Bundle args = new Bundle();
    private ImageViewFragment im;
    private FragmentTransaction fragmentTransaction;

    public GalleryGridFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance() {
        return new GalleryGridFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gallery_grid, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        mLayoutManager = new GridLayoutManager(getContext(), 4);
        getImageUrls();
        showGallery();
        recyclerViewGalleryGrid.addOnItemTouchListener(listItemListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onConfigurationChanged(ConfigurationChnagedEvent event) {
        showGallery();
    }

    RecyclerView.OnItemTouchListener listItemListener =
            new RecyclerClickListener(getContext(), new RecyclerClickListener.OnItemTouchListener() {
                @Override
                public void onItemClick(View view, int position) {
                    args.putInt("pos", position);
                    im = new ImageViewFragment();
                    im.setArguments(args);
                    fragmentTransaction = getActivity().getSupportFragmentManager()
                                    .beginTransaction();
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.replace(R.id.content_frame,
                            im, "image").commit();
                }

                @Override
                public void onItemLongClick(View view, int position) {
                }

                @Override
                public void onDoubleTap(View childView, int childAdapterPosition) {
                }

                @Override
                public void onFling(View childView1, View childView2, int pos1, int pos2) {
                }
            });

    private void getImageUrls() {
        try {
            mUserManager = new UserManager(getContext());
            String urlString = mUserManager.getFavoriteImages();
            String[] urls = urlString.split("\\s");
            mUrls = new LinkedList<>(Arrays.asList(urls));
            mUrls.remove("null");
            mUrls.remove("");
        } catch (NullPointerException e) {
            mFavImagesAvailable = false;
        }
    }

    private void showGallery() {
        if (mFavImagesAvailable) {
            recyclerViewGalleryGrid.setHasFixedSize(true);
            mAdapter = new ImageAdapter(mUrls, getContext());
            recyclerViewGalleryGrid.setLayoutManager(mLayoutManager);
            recyclerViewGalleryGrid.setAdapter(mAdapter);
        } else {
            Snackbar.make(layoutGallery, "No images available", Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            getActivity().onBackPressed();
                        }
                    })
                    .show();
        }
    }

}
