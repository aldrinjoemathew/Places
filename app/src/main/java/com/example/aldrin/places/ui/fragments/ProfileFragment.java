package com.example.aldrin.places.ui.fragments;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.aldrin.places.R;
import com.example.aldrin.places.events.ConfigurationChnagedEvent;
import com.example.aldrin.places.events.ProfileImageUpdatedEvent;
import com.example.aldrin.places.helpers.CustomTextWatcher;
import com.example.aldrin.places.helpers.UserManager;
import com.example.aldrin.places.models.UserInformation;
import com.example.aldrin.places.ui.activities.UserhomeActivity;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    public static final String KEY_FIRST_NAME = "firstname";
    public static final String KEY_LAST_NAME = "lastname";
    public static final String KEY_PHONE_NUMBER = "phonenumber";
    private static final int MY_PERMISSIONS_READ_STORAGE = 1;
    private static final String TAG_ERROR = "error";
    private static final int GET_FROM_GALLERY = 1;
    private Context mContext;
    private UserManager mCurrentUser;
    private String mUserEmail;
    private int radiusValue;
    private OnProfilePicChangedListener profilePicChangedListener;

    @BindView(R.id.button_submit)
    Button btnSubmitDetails;
    @BindView(R.id.tv_radius)
    TextView tvRadius;
    @BindView(R.id.email_et)
    EditText etEmail;
    @BindView(R.id.phone_number_et)
    EditText etPhoneNumber;
    @BindView(R.id.first_name_et)
    EditText etFirstname;
    @BindView(R.id.last_name_et)
    EditText etLastname;
    @BindView(R.id.first_name_layout)
    TextInputLayout layoutFirstname;
    @BindView(R.id.last_name_layout)
    TextInputLayout layoutLastname;
    @BindView(R.id.email_layout)
    TextInputLayout layoutEmail;
    @BindView(R.id.phone_number_layout)
    TextInputLayout layoutPhoneNumber;
    @BindView(R.id.seekbar_radius)
    SeekBar seekbarRadius;
    @BindView(R.id.iv_profile)
    ImageView imageViewProfile;

    public static final String TAG = ProfileFragment.class.getSimpleName();
    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mContext = getContext();
        ButterKnife.bind(this, view);
        addTextChangedListeners();
        displayUserInformation();
        seekbarRadius.setOnSeekBarChangeListener(radiusChanged);
        profilePicChangedListener = (OnProfilePicChangedListener) getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            mCurrentUser.changeProfilePic(selectedImage);
            displayProfilePic();
            profilePicChangedListener.onProfilePicChanged();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_READ_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
            } else {

            }
            return;
        }
    }

    // Container Activity must implement this interface
    public interface OnProfilePicChangedListener {
        void onProfilePicChanged();
    }


    private void displayUserInformation() {
        mCurrentUser = new UserManager(mContext);
        mUserEmail = mCurrentUser.getUserEmail();
        HashMap<String, String> userDetails = mCurrentUser.getUserDetails();
        String firstName = userDetails.get(KEY_FIRST_NAME);
        String lastName = userDetails.get(KEY_LAST_NAME);
        String phoneNumber = userDetails.get(KEY_PHONE_NUMBER);
        etEmail.setText(mUserEmail);
        etFirstname.setText(firstName);
        etLastname.setText(lastName);
        etPhoneNumber.setText(phoneNumber);
        displayProfilePic();
        String radius = mCurrentUser.getSearchRadius();
        int radiusValue =  Integer.parseInt(radius);
        seekbarRadius.setProgress(radiusValue);
        if (Integer.parseInt(radius)<1000) {
            radius = radius + " m";
        } else {
            radius = String.valueOf((float)radiusValue/1000) + " km";
        }
        tvRadius.setText(radius);
    }

    /**
     * Allows user signup if user credentials are valid.
     */
    @OnClick(R.id.button_submit)
    void submitDetails() {
        UserInformation newUser = new UserInformation();
        newUser.setmFirstName(etFirstname.getText().toString());
        newUser.setmLastName(etLastname.getText().toString());
        newUser.setmEmail(etEmail.getText().toString());
        newUser.setmPhoneNumber(etPhoneNumber.getText().toString());
        if (radiusValue != seekbarRadius.getProgress()) {
            mCurrentUser.updateSearchRadius(String.valueOf(seekbarRadius.getProgress()));
            ((UserhomeActivity) getActivity()).getNearbyRestaurants();
        }
    }

    @OnClick(R.id.iv_profile)
    void changeImage() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_READ_STORAGE);
        } else {
            startActivityForResult(new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
        }
    }

    @Subscribe
    public void onConfigurationChanged(ConfigurationChnagedEvent event) {
        displayProfilePic();
    }

    SeekBar.OnSeekBarChangeListener radiusChanged = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
            String radius;
            if (progress<1000){
                radius = String.valueOf(progress) + " m";
            } else {
                radius = String.valueOf((float) progress/1000) + " km";
            }
            tvRadius.setText(radius);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    /**
     * Bind XML fields with Java code.
     */
    private void addTextChangedListeners(){
        etFirstname.addTextChangedListener(new CustomTextWatcher(layoutFirstname));
        etLastname.addTextChangedListener(new CustomTextWatcher(layoutLastname));
        etEmail.addTextChangedListener(new CustomTextWatcher(layoutEmail));
        etPhoneNumber.addTextChangedListener(new CustomTextWatcher(layoutPhoneNumber));
    }

    /**
     * Method to display user's profile image.
     */
    public void displayProfilePic() {
        Uri profileImage;
        try {
            profileImage = mCurrentUser.getProfilePic(mUserEmail);
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_READ_STORAGE);
            } else {
                DisplayMetrics displaymetrics = new DisplayMetrics();
                ((Activity)mContext).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                int width = displaymetrics.widthPixels;
                Picasso.with(getContext())
                        .load(profileImage)
                        .centerCrop()
                        .resize(width, (int) getResources().getDimension(R.dimen.image_height))
                        .into(imageViewProfile);
                /*Glide.with(this)
                        .load(profileImage)
                        .centerCrop()
                        .into(imageViewProfile);*/
            }
        } catch (NullPointerException e) {
            Log.e(TAG_ERROR, e.toString());
        }
    }
}
