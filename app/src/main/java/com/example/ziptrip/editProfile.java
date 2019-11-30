package com.example.ziptrip;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class editProfile extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String TAG = "editProfile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Intent intent = getIntent();
        final Bundle extras = intent.getExtras();
        final String username = extras.getString("username");

        final EditText nameField = (EditText) findViewById(R.id.nameField);
        final EditText emailField = (EditText) findViewById(R.id.emailField);
        final EditText phoneField = (EditText) findViewById(R.id.phoneField);
        final EditText passwordField = (EditText) findViewById(R.id.passwordField);
        final Button updateProfileBtn = (Button) findViewById(R.id.updateProfileBtn);

        final String profileName = extras.getString("profile_name");
        String email = extras.getString("email");
        final String phone = extras.getString("phone");
        String password = extras.getString("pass");

        nameField.setText(profileName);
        emailField.setText(email);
        phoneField.setText(phone);
        passwordField.setText(password);

        updateProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Split the user name
                String[] name = nameField.getText().toString().split(" ");
                String firstname = name[0];
                String lastname = name[1];

                // Send data to database
                Map<String, Object> user = new HashMap<>();
                user.put("firstname", firstname);
                user.put("lastname", lastname);
                user.put("email", emailField.getText().toString());
                user.put("password", passwordField.getText().toString());
                user.put("phone", phoneField.getText().toString());

                // Add a new document with a generated ID (can customize later)
                db.collection("users").document(username)
                        .update(user)
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
                editProfile.this.finish();
            }
        });
    }

}
