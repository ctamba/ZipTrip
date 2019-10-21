package com.example.ziptrip;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ProfilePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        final Button editProfileBtn = (Button) findViewById(R.id.editProfileBtn);
        final TextView name = (TextView) findViewById(R.id.profileName);
        final TextView emailAddress = (TextView) findViewById(R.id.emailAddress);
        final TextView phoneNum = (TextView) findViewById(R.id.phoneNum);
        final TextView password = (TextView) findViewById(R.id.password);

        boolean test = true; // making changes to retry Git testttt

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), editProfile.class);
                String profileName = name.getText().toString();
                String email = emailAddress.getText().toString();
                String phone = phoneNum.getText().toString();
                String pass = password.getText().toString();
                Bundle extras = new Bundle();
                extras.putString("profile_name", profileName);
                extras.putString("email", email);
                extras.putString("phone", phone);
                extras.putString("pass", pass);
                intent.putExtras(extras);
                startActivity(intent);
            }
        });

    }
}