package com.example.ziptrip;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateAccountActivity extends AppCompatActivity {

    // Initialize database
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    EditText fNameInput, lNameInput, emailInput, passwdInput, validatePasswdInput, phoneInput, usernameInput;
    TextView verifyEmail, verifyPassword;
    Button createAccountBtn;
    String TAG = "CreateAccount";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        fNameInput = (EditText)findViewById(R.id.fNameInput);
        lNameInput = (EditText)findViewById(R.id.lNameInput);
        emailInput = (EditText)findViewById(R.id.emailInput);
        passwdInput = (EditText)findViewById(R.id.passwdInput);
        validatePasswdInput = (EditText)findViewById(R.id.checkPasswdInput);
        phoneInput = (EditText)findViewById(R.id.phoneInput);
        usernameInput = (EditText)findViewById(R.id.usernameInput);
        verifyPassword = (TextView) findViewById(R.id.passwdNotMatchTv);
        verifyEmail = (TextView)findViewById(R.id.invalidEmailTv);
        createAccountBtn = (Button)findViewById(R.id.createAccountBtn);
        createAccountBtn.setEnabled(false);

        // Add text watchers
        fNameInput.addTextChangedListener(watcher);
        lNameInput.addTextChangedListener(watcher);
        emailInput.addTextChangedListener(watcher);
        passwdInput.addTextChangedListener(watcher);
        validatePasswdInput.addTextChangedListener(watcher);
        phoneInput.addTextChangedListener(watcher);
        usernameInput.addTextChangedListener(watcher);

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
                // Create empty array to append to trips
                List<String> emptyTrips = new ArrayList<>();

                // Send data to database
                Map<String, Object> user = new HashMap<>();
                user.put("firstname", fNameInput.getText().toString());
                user.put("lastname", lNameInput.getText().toString());
                user.put("email", emailInput.getText().toString());
                user.put("password", passwdInput.getText().toString());
                user.put("phone", phoneInput.getText().toString());
                user.put("username", usernameInput.getText().toString());
                user.put("trips", emptyTrips);

                // Add a new document with a generated ID (can customize later)
                db.collection("users").document(usernameInput.getText().toString())
                        .set(user)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully written!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error writing document", e);
                            }
                        });

                Intent dashboardIntent = new Intent(getApplicationContext(), DashboardActivity.class);
                dashboardIntent.putExtra("username", usernameInput.getText().toString());
                startActivity(dashboardIntent);
                finish();
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

    private final TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        { }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {}
        @Override
        public void afterTextChanged(Editable s) {
            if (fNameInput.getText().toString().length() == 0 || lNameInput.getText().toString().length() == 0 ||
                    emailInput.getText().toString().length() == 0 || passwdInput.getText().toString().length() == 0 ||
                    validatePasswdInput.toString().trim().length() == 0 || phoneInput.getText().toString().length() == 0 ||
                    usernameInput.getText().toString().length() == 0) {
                createAccountBtn.setEnabled(false);
            } else {
                createAccountBtn.setEnabled(true);
            }
        }
    };
}
