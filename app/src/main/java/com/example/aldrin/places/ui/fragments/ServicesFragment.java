package com.example.aldrin.places.ui.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;

import com.example.aldrin.places.R;
import com.example.aldrin.places.adapters.AutoCompleteAdapter;
import com.example.aldrin.places.adapters.ServicesListAdapter;
import com.example.aldrin.places.events.ServiceResponseUpdatedEvent;
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
    private static final String TAG_ERROR = "ERROR";
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<String> mServiceTypeTitles;
    private List<String> mServiceTypes;
    private UserManager mUserManager;
    private Retrofit retrofit;
    private Call<GetFromJson> call;
    private Bundle bundle = new Bundle();
    private Gson gson = new Gson();

    @BindView(R.id.layout_services)
    CoordinatorLayout layoutSevices;
    @BindView(R.id.recycler_view_services)
    RecyclerView mRecyclerView;
    @BindView(R.id.et_search_service)
    SearchView mSearchService;
    @BindString(R.string.google_api_base_url)
    String mGoogleApiBaseUrl;
    @BindString(R.string.google_places_web_key)
    String mGooglePlacesWebKey;

    @BindView(R.id.actv_search)
    AutoCompleteTextView actvSearch;

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserManager = new UserManager(getContext());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        initializeRecyclerView();
        mSearchService.setIconified(false);
        String[] serviceTypes = getString(R.string.services).split(",");
        mServiceTypes = new LinkedList<String>(Arrays.asList(serviceTypes));
        mRecyclerView.addOnItemTouchListener(startBackgroundThread);
        retrofit = new Retrofit.Builder()
                .baseUrl(mGoogleApiBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        actvSearch.setAdapter(new AutoCompleteAdapter(getContext(), R.layout.layout_list_item));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * Implementation of Recycler view touch listener.
     * Displays nearby services when clicking on a service.
     */
    RecyclerView.OnItemTouchListener startBackgroundThread =
            new RecyclerClickListener(getContext(), new RecyclerClickListener.OnItemTouchListener() {
        @Override
        public void onItemClick(View view, int position) {
            String searchValue = mSearchService.getQuery().toString();
            String serviceType = mServiceTypes.get(position);
            if (!searchValue.isEmpty()) {
                UserManager.serviceType = serviceType;
                UserManager.searchValue = searchValue;
                String loc[] = mUserManager.getLocation();
                String location = loc[0] + "," + loc[1];
                String radius = mUserManager.getSearchRadius();
                ApiInterface service = retrofit.create(ApiInterface.class);
                call = service.getNearbyServices(mGooglePlacesWebKey, serviceType, searchValue,
                                location, radius);
                call.enqueue(displayServicesInNewFragment);
                Boolean isRestaurant = false;
                android.support.v4.app.FragmentTransaction fragmentTransaction =
                        getActivity().getSupportFragmentManager()
                                .beginTransaction();
                /*fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);*/
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                fragmentTransaction.addToBackStack(null);
                String serviceName = mServiceTypeTitles.get(position);
                bundle.putBoolean("isRestaurant", isRestaurant);
                bundle.putString("service", serviceName);
                ListFragment tab2 = new ListFragment();
                tab2.setArguments(bundle);
                fragmentTransaction.replace(R.id.content_frame,
                        tab2, ListFragment.TAG).commit();
            } else {
                Snackbar.make(layoutSevices, "Enter a searchtext", Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mSearchService.requestFocus();
                            }
                        })
                        .show();
            }
        }

        @Override
        public void onItemLongClick(View view, int position) {
        }

        @Override
        public void onDoubleTap(View childView, int childAdapterPosition) {
        }
    });

    /**
     * Restrofit callback.
     * Displays the services in a new fragment on success.
     */
    Callback<GetFromJson> displayServicesInNewFragment = new Callback<GetFromJson>() {
        @Override
        public void onResponse(Call<GetFromJson> call, Response<GetFromJson> response) {
            String apiResult = gson.toJson(response.body());
            mUserManager.updateNearbyResponse(false, apiResult);
            EventBus.getDefault().post(new ServiceResponseUpdatedEvent());
        }

        @Override
        public void onFailure(Call<GetFromJson> call, Throwable t) {
            Log.e(TAG_ERROR, t.toString());
        }
    };

    /**
     * Initailzing the recycler view.
     */
    private void initializeRecyclerView() {
        String[] serviceTitles = getString(R.string.services_display_names).split(",");
        mServiceTypeTitles = new LinkedList<>(Arrays.asList(serviceTitles));
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new ServicesListAdapter(mServiceTypeTitles);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }
}
