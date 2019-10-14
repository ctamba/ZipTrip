package com.example.ziptrip;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableRow;

public class CreateAccountActivity extends AppCompatActivity {

    EditText emailInput, passwdInput, validatePasswdInput, phoneInput;
    TableRow verifyEmail, verifyPassword;
    Button createAccountBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        emailInput = (EditText)findViewById(R.id.emailInput);
        passwdInput = (EditText)findViewById(R.id.passwdInput);
        validatePasswdInput = (EditText)findViewById(R.id.checkPasswdInput);
        phoneInput = (EditText)findViewById(R.id.phoneInput);
        verifyPassword = (TableRow)findViewById(R.id.verifyPasswdRow);
        verifyEmail = (TableRow)findViewById(R.id.verifyEmailRow);
        createAccountBtn = (Button)findViewById(R.id.createAccountBtn);

        // Formatting
        PhoneNumberUtils.formatNumber(phoneInput.getText().toString());

        validatePasswdInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // no action
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // no action
            }

            @Override
            public void afterTextChanged(Editable editable) {
                setPasswordValidationText(editable.toString());
            }
        });

        emailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // no action
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // no action
            }

            @Override
            public void afterTextChanged(Editable editable) {
                setEmailValidationText(editable.toString());
            }
        });

        createAccountBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent dashboardIntent = new Intent(getApplicationContext(), DashboardActivity.class);
                startActivity(dashboardIntent);
            }
        });


    }

    private void setEmailValidationText(String email){
        if(isEmailValid(email)){
            verifyEmail.setVisibility(View.INVISIBLE);
        }
        else{
            verifyEmail.setVisibility(View.VISIBLE);
        }
    }

    private void setPasswordValidationText(String password){
        if(!isPasswordValid(password)){
            verifyPassword.setVisibility(View.VISIBLE);
        }
        else{
            verifyPassword.setVisibility(View.INVISIBLE);
        }
    }

    private boolean isPasswordValid(String password){
        if(passwdInput.getText().toString().equals(password)){
            return true;
        }
        return false;
    }

    private boolean isEmailValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return false;
        }
    }
}
