package com.example.aldrin.places.Activities;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.aldrin.places.AccountManagement.UserManager;
import com.example.aldrin.places.CustomClasses.CustomTextWatcher;
import com.example.aldrin.places.R;

/**
 * Activity to perform login operation.
 * Provide user options to signup or reset password.
 */
public class LoginActivity extends AppCompatActivity {
    public EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnSignup;
    private TextView tvForgotPassword;
    private TextInputLayout layoutPassword;
    private TextInputLayout layoutEmail;
    private UserManager mUserManager;
    private Context mCurrentContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUserManager = new UserManager(getApplicationContext());
        bindXmlFields();
        btnLogin.setOnClickListener(verifyLogin);
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resetPasswordIntent = new Intent(mCurrentContext,ResetPasswordActivity.class);
                startActivity(resetPasswordIntent);
            }
        });
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signupIntent = new Intent(mCurrentContext,SignupActivity.class);
                startActivity(signupIntent);
            }
        });
    }

    /**
     * Verify login credentials.
     */
    Button.OnClickListener verifyLogin = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();
            layoutEmail.setError(null);
            layoutPassword.setError(null);

            if (TextUtils.isEmpty(email))
                layoutEmail.setError(getString(R.string.error_email_empty));
            else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
                layoutEmail.setError(getString(R.string.error_email_invalid));
            else if (mUserManager.checkUserExists(email))
                loginToAccount(email, password);
            else
                layoutEmail.setError(getString(R.string.error_user_not_exists));
        }
    };

    /**
     * Bind XML fields with Java code.
     */
    private void bindXmlFields() {
        btnLogin = (Button) findViewById(R.id.button_login);
        btnSignup = (Button) findViewById(R.id.button_signup);
        tvForgotPassword = (TextView) findViewById(R.id.forgot_pw);
        etEmail = (EditText) findViewById(R.id.email_et);
        etPassword = (EditText) findViewById(R.id.password_et);
        layoutPassword = (TextInputLayout) findViewById(R.id.password_layout);
        layoutEmail = (TextInputLayout) findViewById(R.id.email_layout);
        etEmail.addTextChangedListener(new CustomTextWatcher(layoutEmail));
        etPassword.addTextChangedListener(new CustomTextWatcher(layoutPassword));
    }

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
            layoutPassword.setErrorEnabled(true);
            layoutPassword.setError(getString(R.string.error_password_wrong));
        }
    }
}
