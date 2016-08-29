package com.example.aldrin.places.Fragments;


import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.aldrin.places.AccountManagement.UserInformation;
import com.example.aldrin.places.AccountManagement.UserManager;
import com.example.aldrin.places.CustomClasses.CustomTextWatcher;
import com.example.aldrin.places.R;

import java.util.HashMap;

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
    private de.hdodenhof.circleimageview.CircleImageView imageViewProfile;
    private View mCurrentView;
    private Context mContext;
    private Button btnSubmitDetails;
    private FloatingActionButton fabChangeImage;
    private EditText etEmail;
    private EditText etPhoneNumber;
    private EditText etFirstname;
    private EditText etLastname;
    private TextInputLayout layoutFirstname;
    private TextInputLayout layoutLastname;
    private TextInputLayout layoutEmail;
    private TextInputLayout layoutPhoneNumber;
    private UserManager mCurrentUser;
    private String mUserEmail;

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
        mCurrentView = view;
        mContext = getContext();
        bindXmlFields();
        displayUserInformation();
        fabChangeImage.setOnClickListener(changeImage);
        btnSubmitDetails.setOnClickListener(submitDetails);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            OnProfilePicChangedListener profilePicChangedListener = (OnProfilePicChangedListener) getActivity();
            mCurrentUser.changeProfilePic(mUserEmail, selectedImage);
            displayProfilePic();
            profilePicChangedListener.onProfilePicChanged();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_READ_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
        HashMap<String, String> userDetails = mCurrentUser.getUserDetails(mUserEmail);
        String firstName = userDetails.get(KEY_FIRST_NAME);
        String lastName = userDetails.get(KEY_LAST_NAME);
        String phoneNumber = userDetails.get(KEY_PHONE_NUMBER);
        etEmail.setText(mUserEmail);
        etFirstname.setText(firstName);
        etLastname.setText(lastName);
        etPhoneNumber.setText(phoneNumber);
        displayProfilePic();
    }

    /**
     * Allows user signup if user credentials are valid.
     */
    Button.OnClickListener submitDetails = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            UserInformation newUser = new UserInformation();
            newUser.setmFirstName(etFirstname.getText().toString());
            newUser.setmLastName(etLastname.getText().toString());
            newUser.setmEmail(etEmail.getText().toString());
            newUser.setmPhoneNumber(etPhoneNumber.getText().toString());
        }
    };

    FloatingActionButton.OnClickListener changeImage = new FloatingActionButton.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_READ_STORAGE);
            }
            startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
        }
    };
    /**
     * Bind XML fields with Java code.
     */
    private void bindXmlFields(){
        btnSubmitDetails = (Button) mCurrentView.findViewById(R.id.button_submit);
        layoutFirstname = (TextInputLayout) mCurrentView.findViewById(R.id.first_name_layout);
        layoutLastname = (TextInputLayout) mCurrentView.findViewById(R.id.last_name_layout);
        layoutEmail = (TextInputLayout) mCurrentView.findViewById(R.id.email_layout);
        layoutPhoneNumber = (TextInputLayout) mCurrentView.findViewById(R.id.phone_number_layout);
        etFirstname = (EditText) mCurrentView.findViewById(R.id.first_name_et);
        etLastname = (EditText) mCurrentView.findViewById(R.id.last_name_et);
        etEmail = (EditText) mCurrentView.findViewById(R.id.email_et);
        etPhoneNumber = (EditText) mCurrentView.findViewById(R.id.phone_number_et);
        imageViewProfile = (de.hdodenhof.circleimageview.CircleImageView) mCurrentView.findViewById(R.id.profile_image);
        fabChangeImage = (FloatingActionButton) mCurrentView.findViewById(R.id.fab_change_image);
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
            }
            imageViewProfile.setImageURI(profileImage);
        } catch (NullPointerException e) {
            Log.e(TAG_ERROR, e.toString());
        }
    }
}
