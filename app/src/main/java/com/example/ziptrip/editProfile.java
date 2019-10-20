package com.example.ziptrip;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

public class editProfile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        final EditText nameField = (EditText) findViewById(R.id.nameField);
        final EditText emailField = (EditText) findViewById(R.id.emailField);
        final EditText phoneField = (EditText) findViewById(R.id.phoneField);
        final EditText passwordField = (EditText) findViewById(R.id.passwordField);

        String profileName = extras.getString("profile_name");
        String email = extras.getString("email");
        String phone = extras.getString("phone");
        String password = extras.getString("pass");

        nameField.setText(profileName);
        emailField.setText(email);
        phoneField.setText(phone);
        passwordField.setText(password);
    }

}
