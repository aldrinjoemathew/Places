package com.example.aldrin.places.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
 * Activity to change password.
 */
public class ChangePasswordActivity extends AppCompatActivity {

    private UserManager mUserAccount;
    private Boolean mProceedWithUpdate;
    private Boolean mPasswordChanged;
    private String mPasscode;
    private Context mCurrentContext;
    private Button btnSubmit;
    private EditText etEmail;
    private EditText etPasscode;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private TextInputLayout layoutEmail;
    private TextInputLayout layoutPasscode;
    private TextInputLayout layoutPassword;
    private TextInputLayout layoutConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        mCurrentContext = this;
        mPasscode = getIntent().getStringExtra(getString(R.string.passcode));
        bindXmlFields();
        btnSubmit.setOnClickListener(submit);
    }

    /**
     * Check if the input credentials are valid.
     * if valid change password to a new password.
     * Finish the activty on success.
     */
    Button.OnClickListener submit = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            UserInformation newUser = new UserInformation();
            newUser.setmEmail(etEmail.getText().toString());
            newUser.setmPassword(etPassword.getText().toString());
            newUser.setmConfirmPassword(etConfirmPassword.getText().toString());
            String passcode = etPasscode.getText().toString();
            validateCredentials(newUser, passcode);
            if (mProceedWithUpdate) {
               mPasswordChanged = mUserAccount.changePassword(newUser.getmEmail(),
                       passcode, newUser.getmPassword());
                if (mPasswordChanged) {
                    new AlertDialog.Builder(mCurrentContext)
                            .setTitle(R.string.success)
                            .setMessage(R.string.password_changed)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Go back to loginactivity on clicking OK
                                    finish();
                                }
                            })
                            .show();
                } else {
                    layoutPasscode.setError(getString(R.string.wrong_passcode));
                    mProceedWithUpdate = false;
                }
            }
        }
    };

    /**
     * Bind XML fields with Java code.
     */
    private void bindXmlFields(){
        btnSubmit = (Button)findViewById(R.id.button_submit);
        layoutEmail = (TextInputLayout) findViewById(R.id.email_layout);
        layoutPasscode = (TextInputLayout) findViewById(R.id.passcode_layout);
        layoutPassword = (TextInputLayout) findViewById(R.id.password_layout);
        layoutConfirmPassword = (TextInputLayout) findViewById(R.id.confirm_layout);
        etEmail = (EditText) findViewById(R.id.email_et);
        etPasscode = (EditText) findViewById(R.id.passcode_et);
        etPassword = (EditText) findViewById(R.id.password_et);
        etConfirmPassword = (EditText) findViewById(R.id.confirm_et);
        etPasscode.setText(mPasscode);

        etEmail.addTextChangedListener(new CustomTextWatcher(layoutEmail));
        etPasscode.addTextChangedListener(new CustomTextWatcher(layoutPasscode));
        etPassword.addTextChangedListener(new CustomTextWatcher(layoutPassword));
        etConfirmPassword.addTextChangedListener(new CustomTextWatcher(layoutConfirmPassword));
    }

    /**
     * Validate user credentials.
     * @param userInfo
     * @param passcode
     */
    protected void validateCredentials(UserInformation userInfo, String passcode){
        mUserAccount = new UserManager(getApplicationContext());
        Pattern patternPasswordLength = Pattern.compile(".{8,16}$");
        Pattern patternPasswordFormat = Pattern.compile("^[a-z A-Z](?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,16}$");
        /*Initilizing mProceedWithUpdate as true,
                changed to false if any field is invalid*/
        mProceedWithUpdate = true;
        layoutEmail.setError(null);
        layoutPasscode.setError(null);
        layoutPassword.setError(null);
        layoutConfirmPassword.setError(null);
        // Validating the edit text fields
        if (TextUtils.isEmpty(userInfo.getmEmail())) {
            layoutEmail.setError(getString(R.string.error_email_empty));
            mProceedWithUpdate = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(userInfo.getmEmail()).matches()) {
            layoutEmail.setError(getString(R.string.error_email_invalid));
            mProceedWithUpdate = false;
        } else if (!mUserAccount.checkUserExists(userInfo.getmEmail())) {
            layoutEmail.setError(getString(R.string.error_user_not_exists));
            mProceedWithUpdate = false;
        }
                /*Validating the password field value with
                a previously defined regular expression*/
        if (TextUtils.isEmpty(passcode)) {
            layoutPasscode.setError(getString(R.string.error_password_empty));
            mProceedWithUpdate = false;
        }
        if (TextUtils.isEmpty(userInfo.getmPassword())) {
            layoutPassword.setError(getString(R.string.error_password_empty));
            mProceedWithUpdate = false;
        } else if (!patternPasswordLength.matcher(userInfo.getmPassword()).matches()) {
            layoutPassword.setError(getString(R.string.error_password_length_invalid));
            mProceedWithUpdate = false;
        } else if (!patternPasswordFormat.matcher(userInfo.getmPassword()).matches()) {
            layoutPassword.setError(getString(R.string.error_password_wrong_format));
            mProceedWithUpdate = false;
        }
        if (TextUtils.isEmpty(userInfo.getmConfirmPassword())) {
            layoutConfirmPassword.setError(getString(R.string.error_password_empty));
            mProceedWithUpdate = false;
        } else if (!userInfo.getmPassword().equals(userInfo.getmConfirmPassword())) {
            layoutConfirmPassword.setError(getString(R.string.error_confirm_pass_wrong));
            mProceedWithUpdate = false;
        }
    }
}
