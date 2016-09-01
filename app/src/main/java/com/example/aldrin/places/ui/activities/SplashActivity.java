package com.example.aldrin.places.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.example.aldrin.places.helpers.UserManager;
import com.example.aldrin.places.R;

/**
 * Activity to display a splash screen.
 * Allows auto login to user account if a login session exists.
 * Otherwise goes to LoginActivity.
 */
public class SplashActivity extends AppCompatActivity {

    private Handler mHandler = new Handler();
    static final int sTimerDelay =5000;
    Boolean mBackPressed = false;
    UserManager mSession;

    final Runnable loginRunnable = new Runnable() {
        public void run() {
            if(mSession.isUserLoggedIn()) {
                Intent userHomeIntent = new Intent(getBaseContext(),UserhomeActivity.class);
                startActivity(userHomeIntent);
                finish();
            }
            else {
                Intent loginIntent = new Intent(getBaseContext(),LoginActivity.class);
                startActivity(loginIntent);
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mSession = new UserManager(this);
    }

    /**
     * Stops the SplashActivty from creating a new activity onPause.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(loginRunnable);
    }

    /**
     * Resume the splash activity on restarting the application.
     */
    @Override
    protected void onResume() {
        super.onResume();
        mHandler.postDelayed(loginRunnable, sTimerDelay);
    }

    /**
     * Stops the SplashActivty from creating a new activity on back key pressed.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mHandler.removeCallbacks(loginRunnable);
    }
}
