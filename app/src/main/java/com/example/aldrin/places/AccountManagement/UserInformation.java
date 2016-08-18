package com.example.aldrin.places.AccountManagement;

import android.net.Uri;

import java.net.URI;

/**
 * A POJO class to retrieve user information from user and
 * to access user information.
 */

public class UserInformation {
    private String mFirstName;
    private String mLastName;
    private String mEmail;
    private String mPhoneNumber;
    private String mPassword;
    private String mConfirmPassword;
    private String mSearchRadius;
    private String mImageUri;

    public String getmImageUri() {
        return mImageUri;
    }

    public void setmImageUri(String mImageUri) {
        this.mImageUri = mImageUri;
    }

    @Override
    public boolean equals(Object object) {
        UserInformation newInfo = (UserInformation) object;
        if (!mFirstName.equals(newInfo.getmFirstName())) {
            return true;
        } else if (!mLastName.equals(newInfo.getmLastName())) {
            return true;
        } else if (!mPhoneNumber.equals(newInfo.getmPhoneNumber())) {
            return true;
        }
        return false;
    }
    public String getmSearchRadius() {
        return mSearchRadius;
    }

    public void setmSearchRadius(String mSearchRadius) {
        this.mSearchRadius = mSearchRadius;
    }

    public String getmFirstName() {
        return mFirstName;
    }

    public void setmFirstName(String mFirstName) {
        this.mFirstName = mFirstName;
    }

    public String getmLastName() {
        return mLastName;
    }

    public void setmLastName(String mLastName) {
        this.mLastName = mLastName;
    }

    public String getmEmail() {
        return mEmail;
    }

    public void setmEmail(String mEmail) {
        this.mEmail = mEmail;
    }

    public String getmPhoneNumber() {
        return mPhoneNumber;
    }

    public void setmPhoneNumber(String mPhoneNumber) {
        this.mPhoneNumber = mPhoneNumber;
    }

    public String getmPassword() {
        return mPassword;
    }

    public void setmPassword(String mPassword) {
        this.mPassword = mPassword;
    }

    public String getmConfirmPassword() {
        return mConfirmPassword;
    }

    public void setmConfirmPassword(String mConfirmPassword) {
        this.mConfirmPassword = mConfirmPassword;
    }
}
