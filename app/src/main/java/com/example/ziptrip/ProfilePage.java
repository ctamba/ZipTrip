package com.example.ziptrip;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfilePage extends AppCompatActivity {

    String phone = "Jhane";
    String TAG = "test";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        final Button editProfileBtn = (Button) findViewById(R.id.editProfileBtn);
        final TextView name = (TextView) findViewById(R.id.profileName);
        final TextView emailAddress = (TextView) findViewById(R.id.emailAddress);
        final TextView phoneNum = (TextView) findViewById(R.id.phoneNum);
        final TextView password = (TextView) findViewById(R.id.password);


        boolean test = true;



        Intent profileIntent = getIntent();
        Bundle profileIntentExtras = profileIntent.getExtras();

        String email = profileIntentExtras.getString("email");


        DocumentReference profileInfo = db.collection("users").document(email);
        profileInfo.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot userInfo =  task.getResult();

                phone = userInfo.getString("phone");
                Log.i(TAG, "Document found: " + userInfo.getData());
            }
        });



        emailAddress.setText(email);
        phoneNum.setText(phone);

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
