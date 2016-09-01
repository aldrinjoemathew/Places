package com.example.aldrin.places.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.aldrin.places.helpers.UserManager;
import com.example.aldrin.places.helpers.CustomTextWatcher;
import com.example.aldrin.places.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Activity to perform login operation.
 * Provide user options to signup or reset password.
 */
public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.et_email)
    EditText etEmail;
    @BindView(R.id.et_password)
    EditText etPassword;
    @BindView(R.id.tv_forgot_pw)
    TextView tvForgotPassword;
    @BindView(R.id.til_email)
    TextInputLayout tilEmail;
    @BindView(R.id.til_password)
    TextInputLayout tilPassword;
    @BindView(R.id.btn_login)
    Button btnLogin;
    @BindView(R.id.btn_signup)
    Button btnSignup;

    private UserManager mUserManager;
    private Context mCurrentContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        mUserManager = new UserManager(getApplicationContext());
        etEmail.addTextChangedListener(new CustomTextWatcher(tilEmail));
        etPassword.addTextChangedListener(new CustomTextWatcher(tilPassword));
        btnLogin.setOnClickListener(verifyLogin);
        tvForgotPassword.setOnClickListener(resetPassword);
        btnSignup.setOnClickListener(signup);
    }

    /**
     * Verify login credentials.
     */
    Button.OnClickListener verifyLogin = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();
            tilEmail.setError(null);
            tilPassword.setError(null);
            if (TextUtils.isEmpty(email))
                tilEmail.setError(getString(R.string.error_email_empty));
            else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
                tilEmail.setError(getString(R.string.error_email_invalid));
            else if (mUserManager.checkUserExists(email))
                loginToAccount(email, password);
            else
                tilEmail.setError(getString(R.string.error_user_not_exists));
        }
    };

    Button.OnClickListener resetPassword = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent resetPasswordIntent = new Intent(mCurrentContext,ResetPasswordActivity.class);
            startActivity(resetPasswordIntent);
        }
    };

    Button.OnClickListener signup = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent signupIntent = new Intent(mCurrentContext,SignupActivity.class);
            startActivity(signupIntent);
        }
    };

    /**
     * Performs login to user account.
     * Creates a new user login session.
     * @param email
     * @param password
     */
    protected void loginToAccount(String email, String password){
        final Intent loginIntent = new Intent(getBaseContext(),UserhomeActivity.class);
        Boolean proceedToLogin = mUserManager.validateUser(email, password);
        if (proceedToLogin) {
            mUserManager.createUserLoginSession(email);
            startActivity(loginIntent);
            finish();
        } else {
            tilPassword.setErrorEnabled(true);
            tilPassword.setError(getString(R.string.error_password_wrong));
        }
    }
}
