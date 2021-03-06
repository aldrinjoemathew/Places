package com.example.aldrin.places.AccountManagement;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.aldrin.places.Activities.ChangePasswordActivity;
import com.example.aldrin.places.Activities.LoginActivity;
import com.example.aldrin.places.R;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Random;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Manages all the functions related to a user account
 */

public class UserManager {
    // Shared Preferences reference
    SharedPreferences mPreferences;
    SharedPreferences.Editor mPrefEditor;
    Context mContext;
    private static final int PRIVATE_MODE = 0;
    private static final String PREFER_NAME = "Accounts";
    private static final String IS_USER_LOGIN = "IsUserLoggedIn";
    public static final String KEY_LOGGED_IN_EMAIL = "email";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_FIRST_NAME = "firstname";
    public static final String KEY_LAST_NAME = "lastname";
    public static final String KEY_PHONE_NUMBER = "phonenumber";

    public UserManager(Context context) {
        mContext = context;
        mPreferences = mContext.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
        mPrefEditor = mPreferences.edit();
    }

    /**
     * Creates a login session on login or signup.
     * Store the current logged in user's email ID in shared pref.
     * @param email
     */
    public void createUserLoginSession(String email){
        mPrefEditor.putBoolean(IS_USER_LOGIN, true);
        mPrefEditor.putString(KEY_LOGGED_IN_EMAIL, email);
        mPrefEditor.commit();
    }

    /**
     * Clear the user login session on logging out.
     */
    public void logoutUser(){
        mPrefEditor.putBoolean(IS_USER_LOGIN, false);
        mPrefEditor.putString(KEY_LOGGED_IN_EMAIL, null);
        mPrefEditor.commit();
        // After logout redirect user to Login Activity
        Intent i = new Intent(mContext, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(i);
    }

    /**
     * Check whether user is logged in or not.
     * @return true if logged in, false otherwise.
     */
    public boolean isUserLoggedIn(){
        return mPreferences.getBoolean(IS_USER_LOGIN, false);
    }

    /**
     * Return the current user's email ID.
     * @return email
     */
    public String getUserEmail () {
        return mPreferences.getString(KEY_LOGGED_IN_EMAIL, null);
    }

    /**
     * Create a new tag in shared pref with Email ID as key.
     * Insert the user info converted to JSON string as the value.
     * @param newUser
     */
    public void createNewAccount(UserInformation newUser){
        newUser.setmConfirmPassword(null);
        Gson gson = new Gson();
        String json = gson.toJson(newUser);
        mPrefEditor.putString(newUser.getmEmail(),json);
        mPrefEditor.commit();
    }

    /**
     * Accept the updated user info from activity.
     * Update the information in shared pref.
     * @param newInfo
     */

    public Boolean changeAccountDetails(UserInformation newInfo) {
        String detailsJson = mPreferences.getString(newInfo.getmEmail(),null);
        Gson gson = new Gson();
        UserInformation oldInfo = gson.fromJson(detailsJson, UserInformation.class);
        Boolean detailsChanged = oldInfo.equals(newInfo);
        if (!detailsChanged) {
            return false;
        }
        oldInfo.setmFirstName(newInfo.getmFirstName());
        oldInfo.setmLastName(newInfo.getmLastName());
        oldInfo.setmPhoneNumber(newInfo.getmPhoneNumber());
        String userDetails = gson.toJson(oldInfo);
        mPrefEditor.putString(newInfo.getmEmail(),userDetails);
        mPrefEditor.commit();
        return true;
    }

    /**
     * Email is used as the key value in sharedpref.
     * Entered email ID is checked against the list of key values in the sharedpref.
     * Sharedpref method contains is used for this purpose.
     * @param email
     * @return
     */
    public Boolean checkUserExists(String email){
        if (mPreferences.contains(email))
            return true;
        else
            return false;
    }

    /**
     * Call method generatePassword to generate a passcode.
     * Update the passcode in shared pref.
     * Generate a notification displaying the passcode.
     * On clicking on notification go to ChangePasswordActivity.
     * @param email
     */
    public void resetPassword(String email){
        String newPassword = generatePassword();
        String detailsJson = mPreferences.getString(email,null);
        Gson gson = new Gson();
        UserInformation userInfo = gson.fromJson(detailsJson, UserInformation.class);
        userInfo.setmPassword(newPassword);
        String json = gson.toJson(userInfo);
        mPrefEditor.putString(userInfo.getmEmail(),json);
        mPrefEditor.commit();

        Intent intent = new Intent(mContext, ChangePasswordActivity.class);
        intent.putExtra("passcode",newPassword);
        PendingIntent currentIntent = PendingIntent.getActivity(mContext,
                (int) System.currentTimeMillis(), intent, 0);
        Notification n  = new Notification.Builder(mContext)
                .setContentTitle("Your password has been successfully changed")
                .setContentText(newPassword)
                .setSmallIcon(R.drawable.cast_ic_notification_small_icon)
                .setContentIntent(currentIntent)
                .setAutoCancel(true).build();
        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, n);
    }

    /**
     * Change password to newPassword if oldPassword is correct.
     * @param email
     * @param oldPassword
     * @param newPassword
     * @return true if password change is success.
     */
    public Boolean changePassword(String email, String oldPassword, String newPassword){
        String detailsJson = mPreferences.getString(email,null);
        Gson gson = new Gson();
        UserInformation userInfo = gson.fromJson(detailsJson, UserInformation.class);
        if (userInfo.getmPassword().equals(oldPassword)) {
            userInfo.setmPassword(newPassword);
            String json = gson.toJson(userInfo);
            mPrefEditor.putString(userInfo.getmEmail(),json);
            mPrefEditor.commit();
            return true;
        }
        return false;
    }

    /**
     * Generates a random password on password reset.
     * @return the generated password.
     */
    private String generatePassword() {
        // For starting character of password
        String alphabets = mContext.getString(R.string.alphabets) + mContext.getString(R.string.alphabets).toLowerCase();
        char[] chars1 = alphabets.toCharArray();
        // For remaining text of password
        String passwordChars = alphabets + mContext.getString(R.string.numbers);
        char[] chars2 = passwordChars.toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        // Generates a random first character
        char c1 = chars1[random.nextInt(chars1.length)];
        sb.append(c1);
        // Generates remaining characters of password
        for (int i = 0; i < 6; i++) {
            c1 = chars2[random.nextInt(chars2.length)];
            sb.append(c1);
        }
        String newPassword = sb.toString();
        return newPassword;
    }

    /**
     * Use the email ID as key value to retrieve user details.
     * Extract user data using the JSON object.
     * User data is converted into a hash map object and returned back.
     * @param email
     * @return the user details.
     */
    public HashMap<String, String> getUserDetails(String email) {
        HashMap<String, String> user = new HashMap<String, String>();
        String detailsJson = mPreferences.getString(email,null);
        Gson gson = new Gson();
        UserInformation userInfo = gson.fromJson(detailsJson, UserInformation.class);
        user.put(KEY_FIRST_NAME, userInfo.getmFirstName());
        user.put(KEY_LAST_NAME, userInfo.getmLastName());
        user.put(KEY_PHONE_NUMBER, userInfo.getmPhoneNumber());
        user.put(KEY_EMAIL, userInfo.getmEmail());
        return user;
    }

    /**
     * Validate password on login.
     * @param email
     * @param password
     * @return true if password is correct, false otherwise.
     */
    public Boolean validateUser (String email, String password) {
        String detailsJson = mPreferences.getString(email,null);
        Gson gson = new Gson();
        UserInformation userInfo = gson.fromJson(detailsJson, UserInformation.class);
        if (userInfo.getmPassword().equals(password))
            return true;
        else return false;
    }
}
