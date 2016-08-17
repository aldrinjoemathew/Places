package com.example.aldrin.places.CustomClasses;

import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;

/**
 * To set the error text gone on text changed inside edit text.
 */
public class CustomTextWatcher implements TextWatcher {
    private TextInputLayout errorLayout;

    public CustomTextWatcher(TextInputLayout e) {
        errorLayout = e;
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void afterTextChanged(Editable s) {
        errorLayout.setError(null);
    }
}