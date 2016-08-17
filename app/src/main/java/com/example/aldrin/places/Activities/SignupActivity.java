package com.example.aldrin.places.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.aldrin.places.AccountManagement.UserInformation;
import com.example.aldrin.places.AccountManagement.UserManager;
import com.example.aldrin.places.CustomClasses.CustomTextWatcher;
import com.example.aldrin.places.R;

import java.util.regex.Pattern;

/**
 * Activity to perform signup for a new user.
 */
public class SignupActivity extends AppCompatActivity {

    Toolbar mToolBar;
    private UserManager mNewAccount;
    private Boolean mProceedWithSignup;
    private Button btnSignup;
    private EditText etEmail;
    private EditText etPhoneNumber;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private EditText etFirstname;
    private EditText etLastname;
    private TextInputLayout layoutFirstname;
    private TextInputLayout layoutLastname;
    private TextInputLayout layoutEmail;
    private TextInputLayout layoutPhoneNumber;
    private TextInputLayout layoutPassword;
    private TextInputLayout layoutConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mToolBar = (Toolbar)findViewById(R.id.toolbar_layout);
        setSupportActionBar(mToolBar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setDisplayShowHomeEnabled(true);
        mToolBar.getNavigationIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        bindXmlFields();
        btnSignup.setOnClickListener(signUp);
    }

    /**
     * Allows user signup if user credentials are valid.
     */
    Button.OnClickListener signUp = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            UserInformation newUser = new UserInformation();
            newUser.setmFirstName(etFirstname.getText().toString());
            newUser.setmLastName(etLastname.getText().toString());
            newUser.setmEmail(etEmail.getText().toString());
            newUser.setmPhoneNumber(etPhoneNumber.getText().toString());
            newUser.setmPassword(etPassword.getText().toString());
            newUser.setmConfirmPassword(etConfirmPassword.getText().toString());
            validateCredentials(newUser);
            if (mProceedWithSignup){
                mNewAccount = new UserManager(getApplicationContext());
                if(mNewAccount.checkUserExists(newUser.getmEmail())) {
                    mProceedWithSignup = false;
                    layoutEmail.setError(getString(R.string.error_duplicate_email));
                } else {
                    createNewAccount(newUser);
                }
            }
        }
    };

    /**
     * Bind XML fields with Java code.
     */
    private void bindXmlFields(){
        btnSignup = (Button)findViewById(R.id.button_signup);
        layoutFirstname = (TextInputLayout) findViewById(R.id.first_name_layout);
        layoutLastname = (TextInputLayout) findViewById(R.id.last_name_layout);
        layoutEmail = (TextInputLayout) findViewById(R.id.email_layout);
        layoutPhoneNumber = (TextInputLayout) findViewById(R.id.phone_number_layout);
        layoutPassword = (TextInputLayout) findViewById(R.id.password_layout);
        layoutConfirmPassword = (TextInputLayout) findViewById(R.id.confirm_layout);
        etFirstname = (EditText) findViewById(R.id.first_name_et);
        etLastname = (EditText) findViewById(R.id.last_name_et);
        etEmail = (EditText) findViewById(R.id.email_et);
        etPhoneNumber = (EditText) findViewById(R.id.phone_number_et);
        etPassword = (EditText) findViewById(R.id.password_et);
        etConfirmPassword = (EditText) findViewById(R.id.confirm_et);

        etFirstname.addTextChangedListener(new CustomTextWatcher(layoutFirstname));
        etLastname.addTextChangedListener(new CustomTextWatcher(layoutLastname));
        etEmail.addTextChangedListener(new CustomTextWatcher(layoutEmail));
        etPhoneNumber.addTextChangedListener(new CustomTextWatcher(layoutPhoneNumber));
        etPassword.addTextChangedListener(new CustomTextWatcher(layoutPassword));
        etConfirmPassword.addTextChangedListener(new CustomTextWatcher(layoutConfirmPassword));
    }

    /**
     * Validate user credentials.
     * @param userInfo
     */
    protected void validateCredentials(UserInformation userInfo){
        // New account mangarer initializing
        mNewAccount = new UserManager(getApplicationContext());
        // Password and phone number patterns for validation
        Pattern patternPasswordLength = Pattern.compile(".{8,16}$");
        Pattern patternPasswordFormat = Pattern.compile("^[a-z A-Z](?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,16}$");
        Pattern patternPhoneNumber = Pattern.compile("^[789]\\d{9}$");
        /*Initilizing mProceedWithUpdate as true,
                changed to false if any field is invalid*/
        mProceedWithSignup = true;
        layoutFirstname.setError(null);
        layoutLastname.setError(null);
        layoutEmail.setError(null);
        layoutPhoneNumber.setError(null);
        layoutPassword.setError(null);
        layoutConfirmPassword.setError(null);
        // Validating the edit text fields
        if (TextUtils.isEmpty(userInfo.getmFirstName())) {
            layoutFirstname.setError(getString(R.string.error_first_name_empty));
            mProceedWithSignup = false;
        }
        if (TextUtils.isEmpty(userInfo.getmLastName())) {
            layoutLastname.setError(getString(R.string.error_last_name_empty));
            mProceedWithSignup = false;
        }
        if (TextUtils.isEmpty(userInfo.getmEmail())) {
            layoutEmail.setError(getString(R.string.error_email_empty));
            mProceedWithSignup = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(userInfo.getmEmail()).matches()) {
            layoutEmail.setError(getString(R.string.error_email_invalid));
            mProceedWithSignup = false;
        }
        if (TextUtils.isEmpty(userInfo.getmPhoneNumber())) {
            layoutPhoneNumber.setError(getString(R.string.error_phone_number_empty));
            mProceedWithSignup = false;
        } else if (!patternPhoneNumber.matcher(userInfo.getmPhoneNumber()).matches()) {
            layoutPhoneNumber.setError(getString(R.string.error_phone_number_invalid));
            mProceedWithSignup = false;
        }
                /*Validating the password field value with
                a previously defined regular expression*/
        if (TextUtils.isEmpty(userInfo.getmPassword())) {
            layoutPassword.setError(getString(R.string.error_password_empty));
            mProceedWithSignup = false;
        } else if (!patternPasswordLength.matcher(userInfo.getmPassword()).matches()) {
            layoutPassword.setError(getString(R.string.error_password_length_invalid));
            mProceedWithSignup = false;
        } else if (!patternPasswordFormat.matcher(userInfo.getmPassword()).matches()) {
            layoutPassword.setError(getString(R.string.error_password_wrong_format));
            mProceedWithSignup = false;
        }
        if (TextUtils.isEmpty(userInfo.getmConfirmPassword())) {
            layoutConfirmPassword.setError(getString(R.string.error_password_empty));
            mProceedWithSignup = false;
        } else if (!userInfo.getmPassword().equals(userInfo.getmConfirmPassword())) {
            layoutConfirmPassword.setError(getString(R.string.error_confirm_pass_wrong));
            mProceedWithSignup = false;
        }
    }

    /**
     * Create a new account if all the constraints are satisfied.
     * Creates a verifyLogin mSession for new user.
     * Add the new account details to shared preference.
     * @param userInfo
     */
    protected void createNewAccount(UserInformation userInfo){
        mNewAccount.createNewAccount(userInfo);
        mNewAccount.createUserLoginSession(userInfo.getmEmail());
        final Intent loginIntent = new Intent(getBaseContext(),UserhomeActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
}

