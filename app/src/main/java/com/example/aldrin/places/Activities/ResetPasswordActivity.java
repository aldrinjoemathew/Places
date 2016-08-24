package com.example.aldrin.places.Activities;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.aldrin.places.AccountManagement.UserManager;
import com.example.aldrin.places.R;

/**
 * Activity to reset user's password.
 */
public class ResetPasswordActivity extends AppCompatActivity {

    private Button btnResetPassword;
    private Toolbar pageToolbar;
    private EditText etEmail;
    private TextInputLayout tilEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        pageToolbar = (Toolbar)findViewById(R.id.toolbar_layout);
        setSupportActionBar(pageToolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setDisplayShowHomeEnabled(true);
        pageToolbar.getNavigationIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        btnResetPassword = (Button)findViewById(R.id.button_reset_pw);
        btnResetPassword.setOnClickListener(resetPassword);
    }

    Button.OnClickListener resetPassword = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            tilEmail = (TextInputLayout) findViewById(R.id.email_layout);
            etEmail = (EditText) findViewById(R.id.email_et);
            if (etEmail.getText().toString().equals(""))
                tilEmail.setError(getString(R.string.error_email_empty));
            else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString()).matches()) {
                tilEmail.setError(getString(R.string.error_email_invalid));
            } else {
                tilEmail.setError(null);
                resetPassword();
            }
        }
    };

    private void resetPassword(){
        UserManager userManager;
        String emailID = etEmail.getText().toString();
        userManager = new UserManager(this);
        if(userManager.checkUserExists(emailID)) {
            userManager.resetPassword(emailID);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.success)
                    .setMessage(R.string.password_changed_dialog_message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Go back to loginactivity on clicking OK
                            finish();
                        }
                    })
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.error)
                    .setMessage(R.string.error_user_not_exists)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing
                        }
                    })
                    .setIcon(R.drawable.ic_error_black)
                    .show();
        }
    }
}
