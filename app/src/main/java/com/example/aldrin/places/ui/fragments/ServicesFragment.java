package com.example.aldrin.places.ui.fragments;


import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.aldrin.places.R;
import com.example.aldrin.places.adapters.ServicesListAdapter;
import com.example.aldrin.places.events.ApiResponseUpdatedEvent;
import com.example.aldrin.places.helpers.RecyclerClickListener;
import com.example.aldrin.places.helpers.UserManager;
import com.example.aldrin.places.interfaces.ApiInterface;
import com.example.aldrin.places.models.nearby.GetFromJson;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 */
public class ServicesFragment extends Fragment {

    public static final String TAG = ServicesFragment.class.getSimpleName();
    private static final String TAG_ERROR = "error";
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Drawable mDivider;
    private List<String> mServiceTypeTitles;
    private List<String> mServiceTypes;
    private UserManager mUserManager;
    private String mUserEmail;

    @BindView(R.id.recycler_view_services)
    RecyclerView mRecyclerView;
    @BindView(R.id.et_search_service)
    EditText mSearchService;
    @BindView(R.id.iv_search_service)
    ImageView ivSearchService;
    @BindString(R.string.google_api_base_url)
    String mGoogleApiBaseUrl;
    @BindString(R.string.google_places_web_key)
    String mGooglePlacesWebKey;

    public static Fragment newInstance() {
        return new ServicesFragment();
    }

    public ServicesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_services, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);
        addToList();
        mUserManager = new UserManager(getContext());
        mUserEmail = mUserManager.getUserEmail();
        String[] serviceTypes = getString(R.string.services).split(",");
        mServiceTypes = new LinkedList<String>(Arrays.asList(serviceTypes));
        mRecyclerView.addOnItemTouchListener(startBackgroundThread);
    }

    RecyclerView.OnItemTouchListener startBackgroundThread =
            new RecyclerClickListener(getContext(), new RecyclerClickListener.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            String searchValue = mSearchService.getText().toString();
            if (searchValue!=null) {
                String serviceType = mServiceTypes.get(position);
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(mGoogleApiBaseUrl)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                String loc[] = mUserManager.getLocation();
                String location = loc[0] + "," + loc[1];
                String radius = mUserManager.getSearchRadius(mUserEmail);
                ApiInterface service = retrofit.create(ApiInterface.class);
                Call<GetFromJson> call =
                        service.getNearbyServices(mGooglePlacesWebKey, serviceType, searchValue,
                                location, radius);
                call.enqueue(displayServicesInNewFragment);
                Boolean isRestaurant = false;
                android.support.v4.app.FragmentTransaction fragmentTransaction =
                        getActivity().getSupportFragmentManager()
                                .beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.content_frame,
                        ListFragment.newInstance(isRestaurant), ListFragment.TAG).commit();
            }
        }

        @Override
        public void onItemLongClick(View view, int position) {
            Log.i("info", "long clicked" + position);
        }
    });

    Callback<GetFromJson> displayServicesInNewFragment = new Callback<GetFromJson>() {
        @Override
        public void onResponse(Call<GetFromJson> call, Response<GetFromJson> response) {
            Gson gson = new Gson();
            String apiResult = gson.toJson(response.body());
            mUserManager.updateNearbyResponse(false, apiResult);
            EventBus.getDefault().post(new ApiResponseUpdatedEvent(null));
        }

        @Override
        public void onFailure(Call<GetFromJson> call, Throwable t) {
            Log.e(TAG_ERROR, t.toString());
        }
    };

    private void addToList() {
        String[] serviceTitles = getString(R.string.services_display_names).split(",");
        mServiceTypeTitles = new LinkedList<String>(Arrays.asList(serviceTitles));
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new ServicesListAdapter(mServiceTypeTitles);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }
}
